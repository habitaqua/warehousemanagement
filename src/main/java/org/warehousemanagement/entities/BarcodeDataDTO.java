package org.warehousemanagement.entities;

import com.itextpdf.kernel.pdf.PdfDocument;
import lombok.Builder;
import lombok.Value;

@Value
public class BarcodeDataDTO {

    String valueToEncode;
    String altText;

    PdfDocument pdfDocument;

    @Builder
    private BarcodeDataDTO(String valueToEncode, String altText, PdfDocument pdfDocument) {
        this.valueToEncode = valueToEncode;
        this.altText = altText;
        this.pdfDocument = pdfDocument;
    }
}
