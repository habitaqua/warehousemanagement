package org.warehousemanagement.service.barcodes;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.warehousemanagement.dao.InventoryDAO;
import org.warehousemanagement.entities.BarcodeDataDTO;
import org.warehousemanagement.entities.SKUBarcodesGenerationRequest;
import org.warehousemanagement.entities.UniqueProductIdsGenerationRequest;
import org.warehousemanagement.entities.exceptions.NonRetriableException;
import org.warehousemanagement.entities.exceptions.RetriableException;
import org.warehousemanagement.entities.inventory.InventoryAddRequest;
import org.warehousemanagement.entities.inventory.inventorystatus.Production;
import org.warehousemanagement.entities.sku.SKU;
import org.warehousemanagement.helpers.idgenerators.ProductIdGenerator;
import org.warehousemanagement.service.SKUService;
import org.warehousemanagement.utils.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
public class SKUBulkBarcodesCreationService {

    private static final String DELIMITER = "-";
    private static final int BATCH_SAVE_SIZE = 25;
    SKUService skuService;
    InventoryDAO inventoryDAO;
    ExecutorService executorService;
    Clock clock;
    BarcodesPersistor barcodesPersistor;
    ProductIdGenerator<UniqueProductIdsGenerationRequest> productIdGenerator;
    String filePath;


    @Inject
    public SKUBulkBarcodesCreationService(Clock clock, @Named("s3BarcodesPersistor") BarcodesPersistor barcodesPersistor,
                                          ProductIdGenerator<UniqueProductIdsGenerationRequest> productIdGenerator, InventoryDAO inventoryDAO,
                                          SKUService skuService, String filePath, ExecutorService executorService) {
        this.clock = clock;
        this.barcodesPersistor = barcodesPersistor;
        this.productIdGenerator = productIdGenerator;
        this.inventoryDAO = inventoryDAO;
        this.filePath = filePath;
        this.executorService = executorService;
        this.skuService = skuService;
    }

    public String generate(SKUBarcodesGenerationRequest request) {
        try {
            String companyId = request.getCompanyId();
            String skuCode = request.getSkuCode();
            SKU sku = skuService.get(companyId, skuCode);
            long productionTime = clock.millis();
            String skuCategory = sku.getSkuCategory();
            String skuType = sku.getSkuType();
            UniqueProductIdsGenerationRequest productIdsRequestDTO = UniqueProductIdsGenerationRequest.builder()
                    .skuCategory(skuCategory).skuCode(sku.getSkuCode()).skuType(skuType)
                    .warehouseId(request.getWarehouseId()).companyId(request.getCompanyId()).productionTime(productionTime)
                    .quantity(request.getQuantity()).build();
            List<String> generatedProductIds = productIdGenerator.generate(productIdsRequestDTO);

            List<String> successfulProductIds = addProductIdsToInventory(productIdsRequestDTO, generatedProductIds);

            File file = new File(filePath);
            file.getParentFile().mkdirs();

            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(filePath));
            List<BarcodeDataDTO> barcodesContent = createBarcodesDTO(successfulProductIds, skuCategory, skuType, pdfDoc);

            Document doc = new Document(pdfDoc);
            doc.setTextAlignment(TextAlignment.CENTER);
            barcodesContent.forEach(barcodeDataDTO -> {
                doc.add(addBarcodeToPdf(barcodeDataDTO));
                doc.add(new Paragraph());

            });
            doc.close();

            return barcodesPersistor.persistBarcodeFile(filePath);
        } catch (InterruptedException e) {
            throw new RetriableException(e);
        } catch (ExecutionException e) {
            throw new RetriableException(e);
        } catch (FileNotFoundException e) {
            throw new NonRetriableException(e);
        }
    }

    private List<String> addProductIdsToInventory(UniqueProductIdsGenerationRequest idsRequestDTO, List<String> generatedIds) throws InterruptedException, ExecutionException {
        List<CompletableFuture<List<String>>> completableFutures = Lists.partition(generatedIds, BATCH_SAVE_SIZE).stream()
                .map(subIds -> CompletableFuture.supplyAsync(() -> {
                    InventoryAddRequest inventoryAddRequest = InventoryAddRequest.builder().uniqueProductIds(subIds)
                            .inventoryStatus(new Production()).productionTime(idsRequestDTO.getProductionTime())
                            .skuCategory(idsRequestDTO.getSkuCategory()).skuCode(idsRequestDTO.getSkuCode())
                            .skuType(idsRequestDTO.getSkuType()).warehouseId(idsRequestDTO.getWarehouseId())
                            .companyId(idsRequestDTO.getCompanyId()).build();
                    return inventoryDAO.add(inventoryAddRequest);
                }, executorService)).collect(Collectors.toList());

        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();
        List<String> successfulProductIds = new ArrayList<>();
        for (CompletableFuture<List<String>> completableFuture : completableFutures) {
            successfulProductIds.addAll(completableFuture.get());
        }
        return successfulProductIds;
    }

    private List<BarcodeDataDTO> createBarcodesDTO(List<String> uniqueIds, String skuCategory, String skuType,
                                                   PdfDocument pdfDoc) {
        List<BarcodeDataDTO> barcodeDataDTOS =
                uniqueIds.stream().map(id -> BarcodeDataDTO.builder().valueToEncode(id).pdfDocument(pdfDoc)
                        .altText(String.join(DELIMITER, skuCategory, skuType)).build()).collect(Collectors.toList());

        return barcodeDataDTOS;
    }

    private Paragraph addBarcodeToPdf(BarcodeDataDTO barcodeDataDTO) {

        Barcode128 barcode128 = Utilities.createBarcode128(barcodeDataDTO);
        PdfFormXObject barcodeObject = barcode128.createFormXObject(null, null,
                barcodeDataDTO.getPdfDocument());
        Paragraph paragraph = new Paragraph().add(new Image(barcodeObject));
        return paragraph;
    }
}

