package org.habitbev.warehousemanagement.service;

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
import org.habitbev.warehousemanagement.entities.SKUBarcodesGenerationRequest;
import org.habitbev.warehousemanagement.entities.BarcodeDataDTO;
import org.habitbev.warehousemanagement.entities.UniqueProductIdsGenerationRequest;
import org.habitbev.warehousemanagement.entities.exceptions.NonRetriableException;
import org.habitbev.warehousemanagement.entities.inventory.InventoryAddRequest;
import org.habitbev.warehousemanagement.entities.inventory.inventorystatus.Production;
import org.habitbev.warehousemanagement.entities.sku.SKU;
import org.habitbev.warehousemanagement.helpers.BarcodesPersistor;
import org.habitbev.warehousemanagement.helpers.idgenerators.ProductIdGenerator;
import org.habitbev.warehousemanagement.service.InventoryService;
import org.habitbev.warehousemanagement.service.SKUService;
import org.habitbev.warehousemanagement.utils.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
public class SKUBulkBarcodesCreationService {

    private static final String DELIMITER = "-";
    private static final int BATCH_SAVE_SIZE = 25;
    SKUService skuService;
    InventoryService inventoryService;
    ExecutorService executorService;
    Clock clock;
    BarcodesPersistor barcodesPersistor;
    ProductIdGenerator<UniqueProductIdsGenerationRequest> productIdGenerator;
    String filePath;


    @Inject
    public SKUBulkBarcodesCreationService(Clock clock, @Named("s3BarcodesPersistor") BarcodesPersistor barcodesPersistor,
                                          ProductIdGenerator<UniqueProductIdsGenerationRequest> productIdGenerator, InventoryService inventoryService,
                                          SKUService skuService, String filePath, ExecutorService executorService) {
        this.clock = clock;
        this.barcodesPersistor = barcodesPersistor;
        this.productIdGenerator = productIdGenerator;
        this.inventoryService = inventoryService;
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

            InventoryAddRequest inventoryAddRequest = InventoryAddRequest.builder().uniqueProductIds(generatedProductIds)
                    .inventoryStatus(new Production()).productionTime(productIdsRequestDTO.getProductionTime())
                    .skuCategory(productIdsRequestDTO.getSkuCategory()).skuCode(productIdsRequestDTO.getSkuCode())
                    .skuType(productIdsRequestDTO.getSkuType()).warehouseId(productIdsRequestDTO.getWarehouseId())
                    .companyId(productIdsRequestDTO.getCompanyId()).build();
            List<String> successfulProductIds = inventoryService.add(inventoryAddRequest);

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
        }catch (FileNotFoundException e) {
            throw new NonRetriableException(e);
        }
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

