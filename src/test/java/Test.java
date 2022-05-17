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

import java.io.File;
import java.io.FileNotFoundException;

public class Test {

    public static void main(String[] args) throws FileNotFoundException {

        final String dest = "./User/moduludu/barcodes-ean.pdf";
        File file = new File(dest);
        file.getParentFile().mkdirs();

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(dest));
        Document doc = new Document(pdfDoc);
        doc.setTextAlignment(TextAlignment.CENTER);

        for (int i = 0; i < 1200; i++) {
            doc.add(createBarcode(String.valueOf(i), pdfDoc));
            doc.add(new Paragraph().add("500 ML"));

        }
        doc.close();


       /* Barcode128 code128 = new Barcode128(pdfDoc);
        code128.setCode("12345XX789XXX");
        code128.setCodeType(Barcode128.CODE128);*/
        //PdfFormXObject xObject = code128.createFormXObject(ColorConstants.BLACK, ColorConstants.BLACK, pdfDoc);


        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
      /*  try {


           *//* BitMatrix bitMatrix = multiFormatWriter.encode("inputString", BarcodeFormat.CODE_128, 180, 40);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ImageIO.write(bufferedImage, "jpg", new File("Users\\moduludu\\image-moduludu.png"));
*//*        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private static Paragraph createBarcode(String code, PdfDocument pdfDoc) {
        BarcodeEAN barcode = new BarcodeEAN(pdfDoc);
        barcode.setAltText(RandomStringUtils.randomAlphanumeric(20));
        barcode.setCodeType(BarcodeEAN.EAN8);
        barcode.setCode(code);
        PdfFormXObject barcodeObject = barcode.createFormXObject(pdfDoc);

        Paragraph p = new Paragraph().add(new Image(barcodeObject));
        return p;
    }
}



