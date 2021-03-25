package model;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Worker thread:
 * it takes a portion of a pdf
 * and completes the "split", "filter", and "count" tasks
 */
public class Worker extends Thread {

    private static final String REGEX = "\\s+|(?=\\p{Punct})|(?<=\\p{Punct})";

    private final PdfMonitor pdfMonitor;
    private final OccurrencesMonitor occurrencesMonitor;
    private final StateMonitor stateMonitor;
    private final ElaboratedWordsMonitor wordsMonitor;
    private final List<String> ignoredWords;

    public Worker(PdfMonitor rawPagesMonitor, OccurrencesMonitor occurrencesMonitor, StateMonitor stateMonitor, ElaboratedWordsMonitor wordsMonitor, List<String> ignoredWords) throws IOException {
        this.pdfMonitor = rawPagesMonitor;
        this.occurrencesMonitor = occurrencesMonitor;
        this.stateMonitor = stateMonitor;
        this.wordsMonitor = wordsMonitor;
        this.ignoredWords = new ArrayList<>(ignoredWords);
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
                    final Optional<String> text = pdfMonitor.getStripper();
                    if (text.isPresent()) {
                        String[] splittedText = split(text.get());
                        Map<String, Integer> occurrences = count(filter(splittedText));
                        this.occurrencesMonitor.writeOccurrence(occurrences);
                        this.wordsMonitor.add(splittedText.length);
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
