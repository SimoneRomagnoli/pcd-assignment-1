package model;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Worker extends Thread {

    private static final String REGEX = "\\s+|(?=\\p{Punct})|(?<=\\p{Punct})";

    private final RawPagesMonitor rawPagesMonitor;
    private final OccurrencesMonitor occurrencesMonitor;
    private final StateMonitor stateMonitor;
    private final List<String> ignoredWords;

    public Worker(RawPagesMonitor rawPagesMonitor, OccurrencesMonitor occurrencesMonitor, StateMonitor stateMonitor, List<String> ignoredWords) throws IOException {
        this.rawPagesMonitor = rawPagesMonitor;
        this.occurrencesMonitor = occurrencesMonitor;
        this.stateMonitor = stateMonitor;
        this.ignoredWords = new ArrayList<>(ignoredWords);
    }

//    @Override
//    public void run() {
//        // The worker terminates when the state is "WaitingForTermination" and all the pages in the RawPagesMonitor are elaborated.
//        while(!(this.stateMonitor.isWaitingForTermination() && this.rawPagesMonitor.allPagesConsumed())) {
//            if(this.stateMonitor.isStopped()) {
//                try {
//                        Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                try {
//                        final String text = rawPagesMonitor.getText();
//                        count(filter(split(text)));
//                } catch (IOException | InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

//    }

    public void run() {
//        // The worker terminates when the state is "WaitingForTermination" and all the pages in the RawPagesMonitor are elaborated.
        while (!(this.stateMonitor.isWaitingForTermination())) {
            try {
                    final String text = rawPagesMonitor.getText();
                    if(text!= null) count(filter(split(text)));
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("[WORKER]: terminated");
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
}
