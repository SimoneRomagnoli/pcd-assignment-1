package model;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class OccurrencesMonitor {

    private Map<String, Integer> occurrences = new HashMap<>();

    public OccurrencesMonitor() {
    }

    public synchronized void writeOccurrence(Map<String, Integer> occ) {
        occ.keySet().stream().forEach(k -> {
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
