package org.warehousenamagement.service.barcodes;

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
import org.warehousenamagement.entities.SKUBarcodeDataDTO;
import org.warehousenamagement.entities.SKUBarcodesGenerationRequestDTO;
import org.warehousenamagement.sao.InventoryDbSAO;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
public class SKUBulkBarcodesCreationService {

    public static final String ALT_TEXT_DELIMITER = "-";
    public static final String BARCODE_INFO_DELIMITER = "<%>";
    Clock clock;
    BarcodesPersistor barcodesPersistor;
    InventoryDbSAO InventoryDbSAO;

    @Inject
    public SKUBulkBarcodesCreationService(Clock clock, @Named("s3BarcodesPersistor") BarcodesPersistor barcodesPersistor,
            InventoryDbSAO InventoryDbSAO) {
        this.clock = clock;
        this.barcodesPersistor = barcodesPersistor;
        this.InventoryDbSAO = InventoryDbSAO;
    }

    public String createBarcodesInBulk(SKUBarcodesGenerationRequestDTO skuBarcodesGenerationRequestDTO)
            throws FileNotFoundException {
        List<SKUBarcodeDataDTO> barcodesContent = createBarcodesContent(skuBarcodesGenerationRequestDTO);
        final String filePath = "/tmp/barcodes.pdf";
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(filePath));
        Document doc = new Document(pdfDoc);
        doc.setTextAlignment(TextAlignment.CENTER);
        barcodesContent.forEach(barcodeDataDTO -> {
            doc.add(addBarcodeToPdf(barcodeDataDTO, pdfDoc));
            doc.add(new Paragraph());

        });
        doc.close();
        return barcodesPersistor.persistBarcodeFile(filePath);
    }

    private List<SKUBarcodeDataDTO> createBarcodesContent(SKUBarcodesGenerationRequestDTO request) {
        int quantity = request.getQuantity();
        List<SKUBarcodeDataDTO> barcodeDataDTOS = new ArrayList<>();
        IntStream.range(0, quantity).forEach(iteration ->
                barcodeDataDTOS.add(SKUBarcodeDataDTO.builder().skuId(UUID.randomUUID().toString())
                        .skuCategory(request.getSkuCategory())
                        .skuType(request.getSkuType()).build()));
        return barcodeDataDTOS;
    }

    private Paragraph addBarcodeToPdf(SKUBarcodeDataDTO skuBarcodeDataDTO,
            PdfDocument pdfDoc) {
        String skuCategory = skuBarcodeDataDTO.getSkuCategory().name();
        String skuType = skuBarcodeDataDTO.getSkuType().name();
        String skuId = skuBarcodeDataDTO.getSkuId();

        String valueToEncode = String.join(ALT_TEXT_DELIMITER,skuId, skuCategory, skuType);
        Barcode128 barcode = new Barcode128(pdfDoc);
        barcode.setAltText(String.join(ALT_TEXT_DELIMITER, skuCategory, skuType));
        barcode.setCodeType(Barcode128.CODE128);
        barcode.setCode(valueToEncode);
        PdfFormXObject barcodeObject = barcode.createFormXObject(null, null, pdfDoc);
        Paragraph paragraph = new Paragraph().add(new Image(barcodeObject));
        return paragraph;
    }
}

