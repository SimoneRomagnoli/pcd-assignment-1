package model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Stripper extends Thread {

    private static final String REGEX = "\\s+|(?=\\p{Punct})|(?<=\\p{Punct})";

    private final RawPagesMonitor rawPagesMonitor;
    private final OccurrencesMonitor occurrencesMonitor;
    private final List<String> ignoredWords;

    private StripperState state;


    public Stripper(RawPagesMonitor rawPagesMonitor, OccurrencesMonitor occurrencesMonitor, List<String> ignoredWords) throws IOException {
        this.rawPagesMonitor = rawPagesMonitor;
        this.occurrencesMonitor = occurrencesMonitor;
        this.ignoredWords = new ArrayList<>(ignoredWords);
        this.state = StripperState.STRIPPING;
    }

    @Override
    public void run() {
        while(!this.state.equals(StripperState.FINISHED)) {
            if(this.state.equals(StripperState.STRIPPING)) {
                try {
                    StripWrapper sw = rawPagesMonitor.getStripper();
                    String text = sw.getText();
                    count(filter(split(text)));
                    this.state = StripperState.WAITING;
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    wait(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean hasFinished() {
        return this.state.equals(StripperState.FINISHED);
    }

    private String[] split(String page) {
        return page.split(REGEX);
    }

    private List<String> filter(String[] splittedText) {
        return Arrays.stream(splittedText).filter(w -> !this.ignoredWords.contains(w)).collect(Collectors.toList());
    }

    private void count(List<String> words) {
        Map<String, Integer> occurrences = new HashMap<>();
        for(String word: words) {
            if(occurrences.containsKey(word)) {
                occurrences.replace(word, occurrences.get(word)+1);
            } else {
                occurrences.put(word, 1);
            }
        }
        this.occurrencesMonitor.writeOccurrence(occurrences);
    }

    public void terminate() {
        this.state = StripperState.FINISHED;
    }

    public void strip() {
        this.state = StripperState.STRIPPING;
    }
}
