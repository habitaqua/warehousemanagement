package org.warehousemanagement.service.barcodes;

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
import org.warehousemanagement.entities.BarcodeDataDTO;
import org.warehousemanagement.entities.SKUBarcodesGenerationRequestDTO;
import org.warehousemanagement.idgenerators.ProductIdGenerator;
import org.warehousemanagement.utils.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class SKUBulkBarcodesCreationService {

    private static final String ALT_TEXT_DELIMITER = "-";
    Clock clock;
    BarcodesPersistor barcodesPersistor;

    ProductIdGenerator<SKUBarcodesGenerationRequestDTO> productIdGenerator;

    @Inject
    public SKUBulkBarcodesCreationService(Clock clock, @Named("s3BarcodesPersistor") BarcodesPersistor barcodesPersistor
            , ProductIdGenerator<SKUBarcodesGenerationRequestDTO> productIdGenerator) {
        this.clock = clock;
        this.barcodesPersistor = barcodesPersistor;
        this.productIdGenerator = productIdGenerator;
    }

    public String createBarcodesInBulk(SKUBarcodesGenerationRequestDTO skuBarcodesGenerationRequestDTO)
            throws FileNotFoundException {

        List<String> generatedIds = productIdGenerator.generate(skuBarcodesGenerationRequestDTO);
        final String filePath = "/tmp/barcodes.pdf";
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(filePath));
        List<BarcodeDataDTO> barcodesContent = createBarcodesDTO(skuBarcodesGenerationRequestDTO, generatedIds, pdfDoc);

        Document doc = new Document(pdfDoc);
        doc.setTextAlignment(TextAlignment.CENTER);
        barcodesContent.forEach(barcodeDataDTO -> {
            doc.add(addBarcodeToPdf(barcodeDataDTO));
            doc.add(new Paragraph());

        });
        doc.close();
        return barcodesPersistor.persistBarcodeFile(filePath);
    }

    private List<BarcodeDataDTO> createBarcodesDTO(SKUBarcodesGenerationRequestDTO request, List<String> uniqueIds,
                                                   PdfDocument pdfDoc) {
        List<BarcodeDataDTO> barcodeDataDTOS =
        uniqueIds.stream().map(id->BarcodeDataDTO.builder().valueToEncode(id).pdfDocument(pdfDoc)
                .altText(String.join(ALT_TEXT_DELIMITER,request.getSkuCategory().getValue(),
                        request.getSkuType().getValue())).build()).collect(Collectors.toList());

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

