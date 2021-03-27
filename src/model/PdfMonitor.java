package model;

import org.apache.pdfbox.multipdf.PDFCloneUtility;
import org.apache.pdfbox.multipdf.PageExtractor;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.tools.PDFMerger;
import org.apache.pdfbox.tools.PDFSplit;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *  Monitor that handles only one PDF at a time:
 *  each worker takes a partition of the current document
 *  so they can split (almost equally) the workload of the computation.
 */
public class PdfMonitor {

    private boolean documentsFinished;
    private Queue<PDDocument> documents;

    public PdfMonitor() {
        this.documents = new LinkedList<>();
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
    public synchronized void setDocument(final PDDocument doc, final int workload, final boolean lastDocument) throws InterruptedException, IOException {
        while(!this.documents.isEmpty()) {
            wait();
        }
        this.documentsFinished = lastDocument;

        Splitter splitter = new Splitter();
        splitter.setSplitAtPage(workload);
        this.documents.addAll(splitter.split(doc));
        this.documents.forEach(d -> System.out.println("Partition of pdf page number is "+d.getNumberOfPages()));
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
    public synchronized Optional<String> getText() throws InterruptedException, IOException {
        //Workers exit this cycle either if a document is present,
        //or there are no documents left (the last one has already been processed).
        while(this.documents.isEmpty() && !documentsFinished) {
            wait();
        }

        if(!this.documents.isEmpty()) {
            PDDocument doc = documents.poll();
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setStartPage(1);
            stripper.setEndPage(doc.getNumberOfPages());
            if(this.documents.isEmpty()) {
                notifyAll();
            }
            return Optional.of(stripper.getText(doc));
        } else {
            return Optional.empty();
        }
    }

}
