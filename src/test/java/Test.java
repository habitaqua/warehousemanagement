import com.google.zxing.MultiFormatWriter;
//import com.itextpdf.text.pdf.BarcodeEAN;
import com.itextpdf.barcodes.BarcodeEAN;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;

public class Test {
    private static final char PAD_CHAR = '0';
    private static final int PAD_SIZE = 4;
    public static void main(String[] args)  {


        for (int i = 41; i < 41 + 80; i++) {
            String incremental_number = StringUtils.leftPad(String.valueOf(i), PAD_SIZE, PAD_CHAR);
            System.out.println("\"BL500MLW-1655224251790-" + incremental_number+"\",");
        }
    }
}



