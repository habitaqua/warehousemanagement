package org.habitbev.warehousemanagement.service;

import com.google.common.base.Preconditions;
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
import org.habitbev.warehousemanagement.entities.BarcodeDataDTO;
import org.habitbev.warehousemanagement.entities.SKUBarcodesGenerationRequest;
import org.habitbev.warehousemanagement.entities.UniqueProductIdsGenerationRequest;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.exceptions.NonRetriableException;
import org.habitbev.warehousemanagement.entities.inventory.InventoryAddRequest;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;
import org.habitbev.warehousemanagement.entities.inventory.inventorystatus.Production;
import org.habitbev.warehousemanagement.entities.sku.SKU;
import org.habitbev.warehousemanagement.entities.sku.SKUDTO;
import org.habitbev.warehousemanagement.helpers.BarcodesPersistor;
import org.habitbev.warehousemanagement.helpers.idgenerators.ProductIdGenerator;
import org.habitbev.warehousemanagement.helpers.validators.WarehouseActionValidatorChain;
import org.habitbev.warehousemanagement.utils.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static org.habitbev.warehousemanagement.helpers.validators.WarehouseAction.SKU_BARCODE_GENERATION;

@Slf4j
public class SKUBulkBarcodesCreationService {

    private static final String DELIMITER = "-";

    WarehouseActionValidatorChain warehouseActionValidatorChain;
    InventoryService inventoryService;
    Clock clock;
    BarcodesPersistor barcodesPersistor;
    ProductIdGenerator<UniqueProductIdsGenerationRequest> productIdGenerator;
    String filePath;


    @Inject
    public SKUBulkBarcodesCreationService(Clock clock, @Named("s3BarcodesPersistor") BarcodesPersistor barcodesPersistor,
                                          @Named("productionTimeBasedUniqueProductIdGenerator") ProductIdGenerator<UniqueProductIdsGenerationRequest> productIdGenerator,
                                          InventoryService inventoryService, WarehouseActionValidatorChain warehouseActionValidatorChain, @Named("barcodesLocalFilePath") String filePath) {
        this.clock = clock;
        this.barcodesPersistor = barcodesPersistor;
        this.productIdGenerator = productIdGenerator;
        this.inventoryService = inventoryService;
        this.filePath = filePath;

        this.warehouseActionValidatorChain = warehouseActionValidatorChain;
    }

    public String generate(SKUBarcodesGenerationRequest request) {
        try {
            Preconditions.checkArgument(request != null, "SKUBarcodesGenerationRequest cannot be null");
            WarehouseActionValidationRequest warehouseActionValidationRequest = WarehouseActionValidationRequest.builder().skuCode(request.getSkuCode())
                    .warehouseId(request.getWarehouseId()).companyId(request.getCompanyId()).warehouseAction(SKU_BARCODE_GENERATION).build();
            WarehouseValidatedEntities warehouseValidatedEntities = warehouseActionValidatorChain.execute(warehouseActionValidationRequest);
            SKUDTO skuDTO = warehouseValidatedEntities.getSkuDTO();
            String skuCategory = skuDTO.getSkuCategory();
            String skuType = skuDTO.getSkuType();
            String skuCode = skuDTO.getSkuCode();
            long productionTime = clock.millis();

            UniqueProductIdsGenerationRequest productIdsRequestDTO = UniqueProductIdsGenerationRequest.builder()
                    .skuCategory(skuCategory).skuCode(skuCode).skuType(skuType)
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
            List<BarcodeDataDTO> barcodesContent = createBarcodesDTO(successfulProductIds, skuDTO, pdfDoc);

            Document doc = new Document(pdfDoc);
            doc.setTextAlignment(TextAlignment.CENTER);
            barcodesContent.forEach(barcodeDataDTO -> {
                doc.add(addBarcodeToPdf(barcodeDataDTO));
                doc.add(new Paragraph());

            });
            doc.close();

            return barcodesPersistor.persistBarcodeFile(filePath);
        } catch (FileNotFoundException e) {
            throw new NonRetriableException(e);
        }
    }


    private List<BarcodeDataDTO> createBarcodesDTO(List<String> uniqueIds, SKUDTO skudto,
                                                   PdfDocument pdfDoc) {
        List<BarcodeDataDTO> barcodeDataDTOS =
                uniqueIds.stream().map(id -> BarcodeDataDTO.builder().valueToEncode(id).pdfDocument(pdfDoc)
                        .altText(skudto.getSkuCode()).build()).collect(Collectors.toList());

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

