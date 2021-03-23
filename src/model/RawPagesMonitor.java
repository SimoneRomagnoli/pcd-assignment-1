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
    private int threadID;
    private boolean documentFinished;

    /*
         Monitor that handle only one PDF at a time,
         dividing equally between the workers the number of pages
     */


    public RawPagesMonitor() throws IOException {
        this.document = Optional.empty();
        this.stripper = new PDFTextStripper();
        this.stripper.setSortByPosition(true);
        this.workload = START;
        this.threadID = START;
    }

    public synchronized void setDocument(final PDDocument doc, final int workload) throws InterruptedException {
        while(this.document.isPresent()) {
            System.out.println("[MASTER]: in wait");
            wait();
            System.out.println("[MASTER]: awoken from wait");
        }
        System.out.println("[MASTER]: deposit of a new document");
        this.document = Optional.of(doc);
        this.workload = workload;
        this.threadID = START;
        notifyAll();
    }

    public synchronized String getText() throws IOException, InterruptedException {
        while(this.document.isEmpty() && !documentFinished) {
            System.out.println("[WORKER]: in wait");
            wait();
            if(documentFinished) System.out.println("[WORKER]: awoken from wait");
        }
        //I need to process the last pdf until is empty
        if(!(document.isEmpty() && documentFinished)) {
            PDDocument doc = document.get();
            final int firstPage = workload * threadID + 1;
            this.stripper.setStartPage(firstPage);
            threadID++;
            final int lastPage = workload * threadID;
            if (lastPage >= doc.getNumberOfPages()) {
                System.out.println("One document elaborated");
                this.stripper.setEndPage(doc.getNumberOfPages());
                this.document = Optional.empty();
                //In order to awake the master
                notifyAll();

            } else {
                this.stripper.setEndPage(lastPage);
            }
            return this.stripper.getText(doc);
        }
        return null;
    }

    // Method called by the model when the PDF are finished.
    // Set the flag documentFinished and awake all the threads from their wait, letting them
    // finish the getText method (returning null).
    public synchronized void documentsFinished(){
        this.documentFinished = true;
        notifyAll();
    }

}
