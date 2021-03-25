import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestSplit {

    @Test
    public void testSplit() throws IOException {
        PDDocument document = PDDocument.load(new File("res/x10-latest.pdf"));
        Splitter splitter = new Splitter();
        splitter.setSplitAtPage(100);
        List<PDDocument> docs = splitter.split(document);

        /*

        System.out.println("Big document page number is "+document.getNumberOfPages());
        System.out.println("Docs length is "+docs.size());
        for(int i = 0; i < docs.size(); i++) {
            docs.get(i).save(new File("res/subdocument"+i+".pdf"));
            System.out.println("Elem doc page number is "+docs.get(i).getNumberOfPages());
        }

         */

        for(PDDocument d:docs) {
            PDFTextStripper str = new PDFTextStripper();
            str.setSortByPosition(true);
            str.setStartPage(1);
            str.setEndPage(d.getNumberOfPages());
            String tmp = str.getText(d);
            //System.out.println(str.getText(d));
        }

    }

}
