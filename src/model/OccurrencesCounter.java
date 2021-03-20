package model;

import gui.GUI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OccurrencesCounter extends Thread {

    private static final String REGEX = "\\s+|(?=\\p{Punct})|(?<=\\p{Punct})";

    private final StrippedPagesMonitor pagesMonitor;
    private final OccurrencesMonitor occurrencesMonitor;
    private final List<String> ignoredWords;

    private ConsumerState state;

    public OccurrencesCounter(StrippedPagesMonitor pagesMonitor, OccurrencesMonitor occurrencesMonitor, List<String> ignoredWords) {
        this.pagesMonitor = pagesMonitor;
        this.occurrencesMonitor = occurrencesMonitor;
        this.ignoredWords = ignoredWords;

        this.state = ConsumerState.CONSUMING;
    }

    @Override
    public void run() {
        while(this.state.equals(ConsumerState.CONSUMING)) {
            try {
                //pagesMonitor.consumePages().stream().forEach(p -> count(filter(split(p))));

                List<String> pages = pagesMonitor.consumePages();
                for(String page:pages) {
                    count(filter(split(page)));
                }
                wait(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
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
        occurrencesMonitor.writeOccurrence(occurrences);
    }

}
