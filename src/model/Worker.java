package model;

import model.OccurrencesMonitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Worker extends Thread {

    private final Map<String, Integer> localOccurrences;

    private final int id;
    private final OccurrencesMonitor monitor;
    private final List<String> words;
    private final List<String> ignored;

    public Worker(int id, OccurrencesMonitor monitor, List<String> words, List<String> ignored) {
        this.localOccurrences = new HashMap<>();

        this.id = id;
        this.monitor = monitor;
        this.words = words;
        this.ignored = ignored;
    }

    @Override
    public void run() {
        words.stream().filter(w -> !ignored.contains(w)).forEach(w -> {
            if(localOccurrences.containsKey(w)) {
                localOccurrences.replace(w, localOccurrences.get(w)+1);
            } else {
                localOccurrences.put(w, 1);
            }
        });
        monitor.writeOccurrence(localOccurrences);
    }
}
