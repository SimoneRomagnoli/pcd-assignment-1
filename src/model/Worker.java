package model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Worker thread:
 * it takes a portion of a pdf
 * and completes the "split", "filter", and "count" tasks
 */
public class Worker extends Thread {

    private static final String REGEX = "[^a-zA-Z0-9]";

    private final Model model;

    private final PdfMonitor pdfMonitor;
    private final OccurrencesMonitor occurrencesMonitor;
    private final StateMonitor stateMonitor;
    private final ElaboratedWordsMonitor wordsMonitor;
    private final List<String> ignoredWords;
    private final PDFTextStripper stripper;

    public Worker(final Model model, PdfMonitor rawPagesMonitor, OccurrencesMonitor occurrencesMonitor, StateMonitor stateMonitor, ElaboratedWordsMonitor wordsMonitor, List<String> ignoredWords) throws IOException {
        this.model = model;
        this.pdfMonitor = rawPagesMonitor;
        this.occurrencesMonitor = occurrencesMonitor;
        this.stateMonitor = stateMonitor;
        this.wordsMonitor = wordsMonitor;
        this.ignoredWords = new ArrayList<>(ignoredWords);
        this.stripper = new PDFTextStripper();
    }

    /**
     * Main computation of a worker thread:
     * if the program is stopped,
     * then wait,
     * else complete the needed tasks.
     */
    @Override
    public void run() {
        while (!(this.stateMonitor.isFinished())) {
            if (this.stateMonitor.isStopped()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    final Optional<PDDocument> document =  pdfMonitor.getDocument();
                    if (document.isPresent()) {
                        String text = this.stripper.getText(document.get());
                        document.get().close();
                        String[] splittedText = split(text);
                        Map<String, Integer> occurrences = count(filter(splittedText));

                        //Update occurrences monitor with found occurrences in pdf
                        this.occurrencesMonitor.writeOccurrence(occurrences);

                        //Update words monitor with number of words in pdf
                        this.wordsMonitor.add(splittedText.length);

                        //The worker updates the view in order to make it more responsive
                        this.model.notifyObservers();
                    } else {
                        stateMonitor.finish();
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String[] split(String page) {
        return page.split(REGEX);
    }

    private List<String> filter(String[] splittedText) {
        return Arrays.stream(splittedText).filter(w -> !this.ignoredWords.contains(w)).collect(Collectors.toList());
    }

    private Map<String, Integer> count(List<String> words) {
        Map<String, Integer> occurrences = new HashMap<>();
        for(String word: words) {
            if(occurrences.containsKey(word)) {
                occurrences.replace(word, occurrences.get(word)+1);
            } else {
                occurrences.put(word, 1);
            }
        }
        return occurrences;
    }
}
