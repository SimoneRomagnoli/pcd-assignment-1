package controller;

import gui.GUI;
import model.*;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

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

        PDFMergerUtility merger = new PDFMergerUtility();
        for (File f : Objects.requireNonNull(pdfDir.listFiles())) {
            merger.addSource(f);
        }
        merger.setDestinationFileName("merge.pdf");
        merger.mergeDocuments(null);
        File mergedFiles = new File("merge.pdf");

        System.out.println("Merged");

        final RawPagesMonitor rawPagesMonitor = new RawPagesMonitor(mergedFiles);
        final StrippedPagesMonitor strippedPagesMonitor = new StrippedPagesMonitor();
        final OccurrencesMonitor occurrencesMonitor = new OccurrencesMonitor(this.words);

        final OccurrencesCounter occurrencesCounter = new OccurrencesCounter(strippedPagesMonitor, occurrencesMonitor, ignoredWords);
        occurrencesCounter.start();

        List<Stripper> strippers = new ArrayList<>();
        int threads = Runtime.getRuntime().availableProcessors();
        for(int i = 0; i < threads; i++) {
            final Stripper stripper = new Stripper(rawPagesMonitor, strippedPagesMonitor);
            strippers.add(stripper);
            stripper.start();
        }

        while(true) {
            this.gui.pushResults(occurrencesMonitor.getOccurrences());
        }
    }

}
