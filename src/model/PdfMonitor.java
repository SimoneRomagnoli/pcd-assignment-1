package model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.util.Optional;

/**
 *  Monitor that handles only one PDF at a time:
 *  each worker takes a partition of the current document
 *  so they can split (almost equally) the workload of the computation.
 */
public class PdfMonitor {

    private static final int ZERO = 0;

    private Optional<PDDocument> document;
    private final PDFTextStripper stripper;
    private int workload;
    private int threadID;
    private boolean documentsFinished;

    public PdfMonitor() throws IOException {
        this.document = Optional.empty();
        this.stripper = new PDFTextStripper();
        this.stripper.setSortByPosition(true);
        this.workload = ZERO;
        this.threadID = ZERO;
    }

    /**
     *
     * Method called by the master thread of the application:
     * sets a new document for the workers that can take it and
     * do their work.
     *
     * @param doc
     * @param workload
     * @param lastDocument
     * @throws InterruptedException
     */
    public synchronized void setDocument(final PDDocument doc, final int workload, final boolean lastDocument) throws InterruptedException {
        while(this.document.isPresent()) {
            wait();
        }
        this.document = Optional.of(doc);
        this.workload = workload;
        this.threadID = ZERO;
        this.documentsFinished = lastDocument;
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
    public synchronized Optional<String> getText() throws IOException, InterruptedException {
        //Workers exit this cycle either if a document is present,
        //or there are no documents left (the last one has already been processed).
        while(this.document.isEmpty() && !documentsFinished) {
            wait();
        }

        //If the document is not present then the computation is finished
        if(document.isPresent()) {
            PDDocument doc = document.get();
            final int firstPage = workload * threadID + 1;
            this.stripper.setStartPage(firstPage);
            threadID++;
            final int lastPage = workload * threadID;

            //The last worker needs to remove the previous document
            //in order to let the master give the new one
            if (lastPage >= doc.getNumberOfPages()) {
                this.stripper.setEndPage(doc.getNumberOfPages());
                this.document = Optional.empty();
                notifyAll();
            } else {
                this.stripper.setEndPage(lastPage);
            }
            return Optional.of(this.stripper.getText(doc));
        } else {
            return Optional.empty();
        }
    }

}