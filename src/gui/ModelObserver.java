package gui;

import java.util.Map;

public interface ModelObserver {

	void modelUpdated(Map<String, Integer> occurrences);
}
