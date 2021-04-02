package model;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 *  Monitor that handles only one PDF at a time:
 *  each worker takes a partition of the current document
 *  so they can split (almost equally) the workload of the computation.
 */
public class PdfMonitor {

    private static final int ZERO = 0;

    private Queue<PDDocument> documents;
    private boolean documentsFinished;

    public PdfMonitor() throws IOException {
        this.documents = new ArrayDeque<>();
        this.documentsFinished = false;
    }

    /**
     *
     * Method called by the master thread of the application:
     * sets a new document for the workers that can take it and
     * do their work.
     *
     *
     * @throws InterruptedException
     */
    public synchronized void setDocument(final PDDocument doc, final boolean lastDocument) throws InterruptedException, IOException {
        this.documentsFinished = lastDocument;

        final Splitter splitter = new Splitter();
        splitter.setMemoryUsageSetting(MemoryUsageSetting.setupTempFileOnly());
        splitter.setSplitAtPage(doc.getNumberOfPages() / Model.AVAILABLE_PROCESSORS + 1);
        documents.addAll(splitter.split(doc));
        notifyAll();
    }

    /**
     *
     * Method called by the worker threads of the application:
     * each of them takes a part of the current document
     * and "strips" the text from the PDDocument variable.
     *
     * @return a string with the wanted text or an empty optional if the computation is finished
     * @throws IOException
     * @throws InterruptedException
     */
    public synchronized PDDocument getDocument() throws IOException, InterruptedException {
        while(documents.isEmpty() && !documentsFinished) {
            wait();
        }

        //If the document is not present then the computation is finished
        if(!documents.isEmpty()) {
            return documents.poll();
        } else {
            return null;
        }
    }

    public synchronized Optional<PDDocument> getText() throws IOException, InterruptedException {
        while(documents.isEmpty() && !documentsFinished) {
            wait();
        }

        //If the document is not present then the computation is finished
        if(!documents.isEmpty()) {
            return Optional.of(documents.poll());
        } else {
            return Optional.empty();
        }
    }

}