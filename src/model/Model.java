package model;

import gui.ModelObserver;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class representing the model of the program:
 * here is nested the entire logic of the program.
 */
public class Model {

    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private final List<ModelObserver> observers;
    private final List<File> documents;
    private final List<String> ignoredWords;
    private int limitWords;

    List<Worker> workers;

    private final PdfMonitor pdfMonitor;
    private final StateMonitor stateMonitor;
    private final ElaboratedWordsMonitor wordsMonitor;

    private final OccurrencesMonitor occurrencesMonitor;

    public Model() throws IOException {
        this.observers = new ArrayList<>();
        this.ignoredWords = new ArrayList<>();
        this.pdfMonitor = new PdfMonitor();
        this.occurrencesMonitor = new OccurrencesMonitor();
        this.stateMonitor = new StateMonitor();
        this.wordsMonitor = new ElaboratedWordsMonitor();
        this.workers = new ArrayList<>();
        this.documents = new ArrayList<>();
    }

    /**
     * Method called only by the master thread:
     * it handles the monitors in order to manage the workers
     * and their work.
     * @throws InterruptedException
     */
    public void update() throws InterruptedException, IOException {
        final long start = System.currentTimeMillis();

        List<PDDocument> pdfs = documents.stream().map(f-> {
            try {
                PDDocument document = PDDocument.load(f);
                AccessPermission ap = document.getCurrentAccessPermission();
                if (!ap.canExtractContent()) {
                    throw new IOException("You do not have permission to extract text");
                }
                System.out.println("Processing file: " + f.getName());
                return document;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList());

        this.pdfMonitor.setDocuments(pdfs);

        System.out.println("time for loading pdfs : " + (System.currentTimeMillis() - start) + " ms.");

        this.createWorkers(AVAILABLE_PROCESSORS);

        for(Worker w : workers){
            w.join();
        }
    }

    /**
     * Setting main arguments of the program
     * when the computation starts.
     *
     * @param pdfDirectoryName
     * @param ignoredWordsFileName
     * @param limitWords
     * @throws IOException
     */
    public void setArgs(final String pdfDirectoryName, final String ignoredWordsFileName, final String limitWords) throws IOException {
        File pdfDirectory = new File(pdfDirectoryName);
        this.documents.addAll(Arrays.asList(Objects.requireNonNull(pdfDirectory.listFiles())));
        this.ignoredWords.addAll(Files.readAllLines(Path.of(ignoredWordsFileName)));
        this.limitWords = Integer.parseInt(limitWords);
    }

    /**
     * Method called by the controller to create
     * workers knowing the amount of available processors.
     *
     * @param n
     * @throws IOException
     */
    public void createWorkers(final int n) throws IOException {
        if(this.workers.isEmpty()) {
            for (int i = 0; i < n; i++) {
                workers.add(new Worker(this, this.pdfMonitor, this.occurrencesMonitor, this.stateMonitor, this.wordsMonitor, this.ignoredWords));
            }
            this.workers.forEach(Worker::start);
        }
    }

    /**
     * Notify the GUI sending the current values for most used words
     * and number of elaborated words
     */
    public void notifyObservers() {
        for (ModelObserver obs: observers) {
            Map<String, Integer> occurrences = this.occurrencesMonitor.getOccurrences();
            obs.modelUpdated(this.wordsMonitor.getElaboratedWords(),
                    occurrences.isEmpty()
                            ? Optional.empty()
                            : Optional.of(occurrences
                            .keySet()
                            .stream()
                            .sorted((a, b) -> occurrences.get(b) - occurrences.get(a))
                            .limit(this.limitWords)
                            .collect(Collectors.toMap(k -> k, occurrences::get))
                    ));
        }
    }

    public StateMonitor getState() {
        return this.stateMonitor;
    }

    public void start() {
        this.stateMonitor.start();
    }

    public void stop() {
        this.stateMonitor.stop();
    }

    public void addObserver(ModelObserver obs){ observers.add(obs); }

}
