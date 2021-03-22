package controller;

import gui.GUI;
import model.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Controller {

    public static final Controller CONTROLLER = new Controller();

    private String directory;
    private String ignored;
    private int words;
    private GUI gui;

    private Controller() {
    }

    public void setDirectory(final String d) {
        this.directory = d;
    }

    public void setIgnored(final String i) {
        this.ignored = i;
    }

    public void setWords(final int w) {
        this.words = w;
    }

    public void setGui(final GUI gui) {
        this.gui = gui;
    }

    public void startComputation() throws IOException, InterruptedException {

        File pdfDir = new File(directory);
        List<String> ignoredWords = new ArrayList<>();
        ignoredWords.addAll(Files.readAllLines(Path.of(ignored)));

        // il documento non lo chiudiamo mai e da warning
        final RawPagesMonitor rawPagesMonitor = new RawPagesMonitor();
        final OccurrencesMonitor occurrencesMonitor = new OccurrencesMonitor(this.words);

        List<Worker> workers = new ArrayList<>();
        int threads = Runtime.getRuntime().availableProcessors();
        for(int i = 0; i < threads; i++) {
            final Worker stripper = new Worker(rawPagesMonitor, occurrencesMonitor, ignoredWords);
            workers.add(stripper);
        }
        System.out.println("Threads: "+threads+".");

        final long start = System.currentTimeMillis();
        try {
            workers.forEach(Worker::start);
            for (File f : Objects.requireNonNull(pdfDir.listFiles())) {
                PDDocument document = PDDocument.load(f);
                AccessPermission ap = document.getCurrentAccessPermission();
                if (!ap.canExtractContent()) {
                    throw new IOException("You do not have permission to extract text");
                }
                System.out.println("File: "+f.getName()+" with "+document.getNumberOfPages()+" pages.");
                int workload = document.getNumberOfPages() % workers.size() == 0 ? document.getNumberOfPages()/workers.size() : document.getNumberOfPages()/workers.size() + 1;
                rawPagesMonitor.setDocument(document, workload);
                workers.forEach(Worker::strip);
            }
            workers.forEach(Worker::terminate);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


        // Questo busy waiting orrendo, ma almeno funziona...
        while(!workers.stream().allMatch(Worker::hasFinished)){};
        System.out.println("Total time: "+(System.currentTimeMillis()-start)+" ms.");
        this.gui.pushResults(occurrencesMonitor.getOccurrences());


//        while(true) {
//            if(strippers.stream().allMatch(Stripper::hasFinished)) {
//
//                // Qui il programma dovrebbe terminare, invece rimane all'interno del ciclo all'infinito
//            }
//            this.gui.pushResults(occurrencesMonitor.getOccurrences());
//        }
    }

}
