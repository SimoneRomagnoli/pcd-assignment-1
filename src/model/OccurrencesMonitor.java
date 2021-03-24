package model;

import java.util.HashMap;
import java.util.Map;

/**
 * Monitor that keeps the count of occurrences of words.
 */
public class OccurrencesMonitor {

    private Map<String, Integer> occurrences;

    public OccurrencesMonitor() {
        this.occurrences = new HashMap<>();
    }

    public synchronized void writeOccurrence(Map<String, Integer> occ) {
        occ.keySet().forEach(k -> {
            if(occurrences.containsKey(k)) {
                occurrences.replace(k, this.occurrences.get(k)+occ.get(k));
            } else {
                occurrences.put(k, occ.get(k));
            }
        });
    }

    public synchronized Map<String, Integer> getOccurrences() {
        return this.occurrences;
    }
}
