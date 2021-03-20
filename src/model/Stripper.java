package model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;

public class Stripper extends Thread {

    private final RawPagesMonitor rawPagesMonitor;
    private final StrippedPagesMonitor strippedPagesMonitor;
    private final PDFTextStripper stripper;
    private StripperState state;

    public Stripper(RawPagesMonitor rawPagesMonitor, StrippedPagesMonitor strippedPagesMonitor) throws IOException {
        this.rawPagesMonitor = rawPagesMonitor;
        this.strippedPagesMonitor = strippedPagesMonitor;
        this.stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        this.state = StripperState.STRIPPING;
    }

    @Override
    public void run() {
        while(!this.state.equals(StripperState.FINISHED)) {
            if(this.state.equals(StripperState.STRIPPING)) {
                try {
                    Page page = rawPagesMonitor.getPageNumber();
                    PDDocument document = PDDocument.load(page.getFile());
                    if(page.getPageNumber() <= document.getNumberOfPages()) {
                        this.stripper.setStartPage(page.getPageNumber());
                        this.stripper.setEndPage(page.getPageNumber());
                        String text = this.stripper.getText(document);
                        strippedPagesMonitor.producePage(text);
                        document.close();
                    } else {
                        this.state = StripperState.FINISHED;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
