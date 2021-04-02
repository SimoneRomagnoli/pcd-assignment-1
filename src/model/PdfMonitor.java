package model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

/**
 *  Monitor that handles only one PDF at a time:
 *  each worker takes a partition of the current document
 *  so they can split (almost equally) the workload of the computation.
 */
public class PdfMonitor {

    private static final int ZERO = 0;

    private Queue<PDDocument> documents;
    private Boolean documentsFinished = false;

    public PdfMonitor() throws IOException {
        this.documents = new ArrayDeque<>();
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
//    public synchronized void setDocuments(final List<PDDocument> docs) throws InterruptedException {
//        documents.addAll(docs);
//        notifyAll();
//    }
    public synchronized void setDocuments(final PDDocument doc, Boolean documentsFinished) throws InterruptedException {
        documents.add(doc);
        this.documentsFinished = documentsFinished;
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
    public synchronized Optional<PDDocument> getDocument() throws IOException, InterruptedException {
        //Workers exit this cycle either if a document is present,
        //or there are no documents left (the last one has already been processed).
        while(this.documents.isEmpty() && !documentsFinished) {
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