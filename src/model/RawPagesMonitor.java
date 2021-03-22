package model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.util.Optional;

public class RawPagesMonitor {

    private static final int START = 0;

    private Optional<PDDocument> document;
    private final PDFTextStripper stripper;
    private int workload;
    private int consumed;

    public RawPagesMonitor() throws IOException {
        this.document = Optional.empty();
        this.stripper = new PDFTextStripper();
        this.stripper.setSortByPosition(true);
        this.workload = START;
        this.consumed = START;
    }

    public synchronized void setDocument(final PDDocument doc, final int workload) throws InterruptedException {
        while(this.document.isPresent()) {
            wait();
        }
        this.document = Optional.of(doc);
        this.workload = workload;
        this.consumed = START;
        notifyAll();
    }

    public synchronized String getText() throws IOException, InterruptedException {
        while(this.document.isEmpty()) {
            wait();
        }
        PDDocument doc = document.get();
        final int firstPage = workload * consumed + 1;
        this.stripper.setStartPage(firstPage);
        consumed++;
        final int lastPage = workload * consumed;
        if(lastPage >= doc.getNumberOfPages()) {
            this.stripper.setEndPage(doc.getNumberOfPages());
            this.document = Optional.empty();
            notifyAll();
        } else {
            this.stripper.setEndPage(lastPage);
        }
        return this.stripper.getText(doc);
    }

}
