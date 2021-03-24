package gui;

import java.util.Map;

/**
 * Useful to update the view when the model is updated.
 */
public interface ModelObserver {
	void modelUpdated(Map<String, Integer> occurrences);
}
