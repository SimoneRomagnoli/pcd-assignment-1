package model;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Worker extends Thread {

    private static final String REGEX = "\\s+|(?=\\p{Punct})|(?<=\\p{Punct})";

    private final RawPagesMonitor rawPagesMonitor;
    private final OccurrencesMonitor occurrencesMonitor;
    private final List<String> ignoredWords;

    private WorkerState state;


    public Worker(RawPagesMonitor rawPagesMonitor, OccurrencesMonitor occurrencesMonitor, List<String> ignoredWords) throws IOException {
        this.rawPagesMonitor = rawPagesMonitor;
        this.occurrencesMonitor = occurrencesMonitor;
        this.ignoredWords = new ArrayList<>(ignoredWords);
        this.state = WorkerState.WAITING;
    }

    @Override
    public void run() {
        while(!this.state.equals(WorkerState.FINISHED)) {
            if(this.state.equals(WorkerState.STRIPPING)) {
                try {
                    synchronized (this) {
                        final String text = rawPagesMonitor.getText();
                        count(filter(split(text)));
                        this.state = WorkerState.WAITING;
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    synchronized (this) {
                        wait(10);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean hasFinished() {
        return this.state.equals(WorkerState.FINISHED);
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

    public synchronized void terminate() {
        this.state = WorkerState.FINISHED;
    }

    public synchronized void strip() {
        this.state = WorkerState.STRIPPING;
    }
}
