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
    private boolean allPageConsumed;

    public RawPagesMonitor() throws IOException {
        this.document = Optional.empty();
        this.stripper = new PDFTextStripper();
        this.stripper.setSortByPosition(true);
        this.workload = START;
        this.threadID = START;
        this.allPageConsumed = false;
    }

    public synchronized void setDocument(final PDDocument doc, final int workload) throws InterruptedException {
        // Il rawPages gestisce un documento alla volta
        while(this.document.isPresent()) {
            wait();
        }
        this.allPageConsumed = false;
        this.document = Optional.of(doc);
        this.workload = workload;
        this.threadID = START;
        notifyAll();
    }

    @SuppressWarnings("unchecked")
    public synchronized String getText() throws IOException, InterruptedException {
        while(this.document.isEmpty()) {
            wait();
        }
        PDDocument doc = document.get();
        final int firstPage = workload * threadID + 1;
        this.stripper.setStartPage(firstPage);
        // Perche' ogni thread lavora su una sottoporzione del documento
        threadID++;
        // ad esempio se workload = 10 , il thread i-esimo lavora sulll'insieme di pagine (10*i - 10*(i+1))
        final int lastPage = workload * threadID;
        // se sono l'ultimo thread a lavorare sul documento
        if(lastPage >= doc.getNumberOfPages()) {
            this.stripper.setEndPage(doc.getNumberOfPages());
            this.document = Optional.empty();
            System.out.println("[WORKER]: I am finishing a PDF");
            // sveglio il master che potrebbe essere in attesa di depositare una nuova pagina
            this.allPageConsumed = true;
            notifyAll();

        } else {
            this.stripper.setEndPage(lastPage);
        }
        return this.stripper.getText(doc);
    }

    public synchronized boolean allPagesConsumed() {
        return this.allPageConsumed;
    }

}
