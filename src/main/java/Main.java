import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[]args) throws IOException {

        Scanner sc = new Scanner(System.in);
        boolean textWasFounded = false;
        ArrayList<String> list = new ArrayList<String>();

        System.out.println("Enter path to your pdf which should be processed");
        String path = sc.nextLine();


        try (PDDocument document = PDDocument.load(new File(path))) {

            document.getClass();
            //В случае если документ не зашифрован мы пытаемся получить все данные из него и записать их в
            //  массив со строками
            if (!document.isEncrypted()) {
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);
                PDFTextStripper tStripper = new PDFTextStripper();
                String pdfFileInText = tStripper.getText(document);
                String lines[]= pdfFileInText.split("\\r?\\n");

                for (String line : lines) {
                    list.add(line);
                }

            }

            //вводим строку которую нужно будет найти и если мы её находим в документе, то на её базе строим
            //  новый документ
            System.out.println("Enter key word which you want to find");
            String textToFind = sc.nextLine();

            for (String txt: list) {
                if(txt.contains(textToFind)){
                    textWasFounded = true;
                    break;
                }
            }

            //если мы нашли данную строку, то создаём вместе с ней документ
            if(textWasFounded){
                //создаём новый документ формата PDF
                PDDocument finalDoc = new PDDocument();

                //Добавляем первую страницу в finalDoc
                PDPage firstPage = document.getPages().get( 0 );
                COSDictionary firstPageDict = firstPage.getCOSObject();
                COSDictionary newFirstPageDict = new COSDictionary(firstPageDict);
                newFirstPageDict.removeItem(COSName.ANNOTS);
                PDPage newFirstPage = new PDPage(firstPageDict);
                finalDoc.addPage(newFirstPage);

                //создаём новую страницу с найденным текстом
                PDPage blankPage = new PDPage();
                PDPageContentStream contentStream = new PDPageContentStream(document, blankPage);
                contentStream.beginText();
                PDType0Font font = PDType0Font.load(finalDoc, new File("src/main/resources/TimesNewRoman.ttf"));
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(25, 500);
                String text = textToFind;
                contentStream.showText(text);
                contentStream.endText();
                finalDoc.addPage(blankPage);
                contentStream.close();

                //создание и добавление в новый документ последней страницы
                PDPage finalPage = document.getPages().get( document.getNumberOfPages()-1 );
                COSDictionary finalPageDict = finalPage.getCOSObject();
                COSDictionary newFinalPageDict = new COSDictionary(finalPageDict);
                newFinalPageDict.removeItem(COSName.ANNOTS);
                PDPage newFinalPage = new PDPage(finalPageDict);
                finalDoc.addPage(newFinalPage);

                //сохраняем получившийся документ
                finalDoc.save("FinalDoc.pdf");
                System.out.println("PDF created");
                finalDoc.close();
                document.close();
            }

            else{
                System.out.println("There is no such text in choosed pdf");
            }
        }
    }
}