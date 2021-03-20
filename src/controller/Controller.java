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
        long programStart = System.currentTimeMillis();

        File pdfDir = new File(directory);
        List<String> ignoredWords = new ArrayList<>();
        ignoredWords.addAll(Files.readAllLines(Path.of(ignored)));

        long stripTime = 0;
        long splitTime = 0;
        long filterTime = 0;
        long countTime = 0;


        Map<String, Integer> occ = new HashMap<>();

        for (File f : Objects.requireNonNull(pdfDir.listFiles())) {
            PDDocument document = PDDocument.load(f);
            AccessPermission ap = document.getCurrentAccessPermission();
            if (!ap.canExtractContent()) {
                throw new IOException("You do not have permission to extract text");
            }
            for(int page = 1; page <= document.getNumberOfPages(); page++) {
                //STRIP
                long stripStart = System.currentTimeMillis();
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true);
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String text = stripper.getText(document);
                stripTime += System.currentTimeMillis()-stripStart;

                //SPLIT
                long splitStart = System.currentTimeMillis();
                String[] splittedText = text.split("\\s+|(?=\\p{Punct})|(?<=\\p{Punct})");
                splitTime += System.currentTimeMillis()-splitStart;

                //FILTER
                long filterStart = System.currentTimeMillis();
                List<String> filteredText = Arrays.stream(splittedText).filter(w -> !ignoredWords.contains(w)).collect(Collectors.toList());
                filterTime += System.currentTimeMillis()-filterStart;

                //COUNT
                long countStart = System.currentTimeMillis();
                List<String> wordsBuffer = new ArrayList<>();
                wordsBuffer.addAll(filteredText);
                for(String word: wordsBuffer) {
                    if(occ.containsKey(word)) {
                        occ.replace(word, occ.get(word)+1);
                    } else {
                        occ.put(word, 1);
                    }
                }
                countTime += System.currentTimeMillis()-countStart;
            }
            document.close();
        }

        System.out.println("Program finished; here are the times:");
        System.out.println("Strip time: "+stripTime+" ms.");
        System.out.println("Split time: "+splitTime+" ms.");
        System.out.println("Filter time: "+filterTime+" ms.");
        System.out.println("Count time: "+countTime+" ms.");

        //final long readStart = System.currentTimeMillis();
        //System.out.print("Time for reading pdf: ");

        /*
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
        */

        long orderStart = System.currentTimeMillis();
        Map<String, Integer> orderedOccurrences = occ
                .keySet()
                .stream()
                .sorted((a, b) -> occ.get(b) - occ.get(a))
                .limit(words)
                .collect(Collectors.toMap(k -> k,k -> occ.get(k)));
        System.out.println("Order time: "+(System.currentTimeMillis()-orderStart)+" ms.");
        System.out.println("Program time: "+(System.currentTimeMillis()-programStart)+" ms.");

        this.gui.pushResults(orderedOccurrences);
        //System.out.println(occurrences.keySet().stream().sorted((a, b) -> occurrences.get(b) - occurrences.get(a)).limit(words).collect(Collectors.toList()));

    }

}
