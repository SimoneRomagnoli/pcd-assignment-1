package model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class RawPagesMonitor {

    private static final int EMPTY = 0;

    private Optional<PDDocument> document;
    private final PDFTextStripper stripper;
    private int workload;
    private int consumed;

    public RawPagesMonitor() throws IOException {
        this.document = Optional.empty();
        this.stripper = new PDFTextStripper();
        this.stripper.setSortByPosition(true);
        this.workload = EMPTY;
        this.consumed = EMPTY;
    }

    public synchronized void setDocument(final PDDocument doc, final int workload) throws InterruptedException {
        while(this.document.isPresent()) {
            wait();
        }
        this.document = Optional.of(doc);
        this.workload = workload;
        this.consumed = EMPTY;
        notifyAll();
    }

    public synchronized String getText() {
        return "";
    }

}
