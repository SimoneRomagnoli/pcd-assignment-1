package controller;

import gui.GUI;
import model.OccurrencesMonitor;
import model.Worker;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;

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
        OccurrencesMonitor monitor = new OccurrencesMonitor();

        File pdfDir = new File(directory);
        List<String> ignoredWords = new ArrayList<>();
        ignoredWords.addAll(Files.readAllLines(Path.of(ignored)));

        List<String> wordsBuffer = new ArrayList<>();

        //final long readStart = System.currentTimeMillis();
        //System.out.print("Time for reading pdf: ");

        for (File f : Objects.requireNonNull(pdfDir.listFiles())) {
            final long readStart = System.currentTimeMillis();
            System.out.print("Time for input-reading: ");
            PDDocument document = PDDocument.load(f);
            AccessPermission ap = document.getCurrentAccessPermission();
            if (!ap.canExtractContent()) {
                throw new IOException("You do not have permission to extract text");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());
            String myText = stripper.getText(document);
            System.out.println(System.currentTimeMillis() - readStart + " ms");

            //this is a critical section
            //List<String> text = Arrays.stream(stripper.getText(document).split("\\s+|(?=\\p{Punct})|(?<=\\p{Punct})")).collect(Collectors.toList());
            wordsBuffer.addAll(Arrays.stream(myText.split("\\s+|(?=\\p{Punct})|(?<=\\p{Punct})")).collect(Collectors.toList()));
            document.close();
        }

        //System.out.println(System.currentTimeMillis() - readStart + " ms");

        Worker w1 = new Worker(0, monitor, wordsBuffer.subList(0, wordsBuffer.size()/2), ignoredWords);
        Worker w2 = new Worker(1, monitor, wordsBuffer.subList(wordsBuffer.size()/2+1, wordsBuffer.size()-1), ignoredWords);

        w1.start();
        w2.start();

        w1.join();
        w2.join();

        Map<String, Integer> occurrences = monitor.getOccurrences();

        this.gui.pushResults(
                        occurrences
                        .keySet()
                        .stream()
                        .sorted((a, b) -> occurrences.get(b) - occurrences.get(a))
                        .limit(words)
                        .collect(Collectors.toMap(k -> k,k -> occurrences.get(k)))
        );


        //System.out.println(occurrences.keySet().stream().sorted((a, b) -> occurrences.get(b) - occurrences.get(a)).limit(words).collect(Collectors.toList()));

    }

}
