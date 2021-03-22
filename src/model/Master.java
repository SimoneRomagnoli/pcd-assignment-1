package model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Master extends Thread {

    private final RawPagesMonitor rawPagesMonitor;
    private final String directoryPath;
    private final List<Stripper> workers;


    public Master(RawPagesMonitor rawPagesMonitor, String directoryPath, List<Stripper> workers) {
        this.rawPagesMonitor = rawPagesMonitor;
        this.directoryPath = directoryPath;
        this.workers = workers;
    }

    @Override
    public void run() {
        try {
            this.workers.forEach(Stripper::start);
            File directory = new File(this.directoryPath);
            for (File f : Objects.requireNonNull(directory.listFiles())) {
                PDDocument document = PDDocument.load(f);
                AccessPermission ap = document.getCurrentAccessPermission();
                if (!ap.canExtractContent()) {
                    throw new IOException("You do not have permission to extract text");
                }
                rawPagesMonitor.setDocument(document, document.getNumberOfPages()/workers.size());
                this.workers.forEach(Stripper::strip);
            }
            this.workers.forEach(Stripper::terminate);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
