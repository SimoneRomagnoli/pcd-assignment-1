package gui;

import java.util.Map;
import java.util.Optional;

/**
 * Useful to update the view when the model is updated.
 */
public interface ModelObserver {
	void modelUpdated(Optional<Map<String, Integer>> occurrences);
}
