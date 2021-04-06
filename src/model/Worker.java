package model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Worker thread:
 * it takes a pdf, extract the text with the "strip"
 * and complete the computation by doing "split", "filter", and "count" tasks
 */
public class Worker extends Thread {

    private static final String REGEX = "[^a-zA-Z0-9]";

    private final Model model;

    private final PdfMonitor pdfMonitor;
    private final OccurrencesMonitor occurrencesMonitor;
    private final StateMonitor stateMonitor;
    private final ElaboratedWordsMonitor wordsMonitor;
    private final List<String> ignoredWords;
    private final PDFTextStripper stripper;

    public Worker(final Model model, PdfMonitor rawPagesMonitor, OccurrencesMonitor occurrencesMonitor, StateMonitor stateMonitor, ElaboratedWordsMonitor wordsMonitor, List<String> ignoredWords) throws IOException {
        this.model = model;
        this.pdfMonitor = rawPagesMonitor;
        this.occurrencesMonitor = occurrencesMonitor;
        this.stateMonitor = stateMonitor;
        this.wordsMonitor = wordsMonitor;
        this.ignoredWords = new ArrayList<>(ignoredWords);
        this.stripper = new PDFTextStripper();
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
                    final Optional<PDDocument> doc =  pdfMonitor.getDocument();
                    if (doc.isPresent()) {
                        /*
                        In order to update the gui more frequently, the amount of "work" is divided by 10.
                         */
                        int pages = doc.get().getNumberOfPages();
                        int chunks = pages < 20 ? 1 : 10;
                        int chunkSize = pages / chunks;
                        for (int i = 0; i < chunks; i++) {
                            while(this.stateMonitor.isStopped()) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            this.stripper.setStartPage(i*chunkSize + 1);
                            this.stripper.setEndPage(Math.min((i+1)*chunkSize, pages));

                            String text = this.stripper.getText(doc.get());
                            String[] splittedText = split(text);
                            Map<String, Integer> occurrences = count(filter(splittedText));
                            this.occurrencesMonitor.writeOccurrence(occurrences);
                            this.wordsMonitor.add(splittedText.length);
                            this.model.notifyObservers();
                        }
                        doc.get().close();
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
