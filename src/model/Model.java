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

    private final List<ModelObserver> observers;
    private final Queue<File> documents;
    private final List<String> ignoredWords;
    private int limitWords;

    List<Worker> workers;

    private final PdfMonitor pdfMonitor;
    private final StateMonitor stateMonitor;

    private final OccurrencesMonitor occurrencesMonitor;

    public Model() throws IOException {
        this.observers = new ArrayList<>();
        this.ignoredWords = new ArrayList<>();
        this.pdfMonitor = new PdfMonitor();
        this.occurrencesMonitor = new OccurrencesMonitor();
        this.stateMonitor = new StateMonitor();
        this.workers = new ArrayList<>();
        this.documents = new ArrayDeque<>();
    }

    /**
     * Method called only by the master thread:
     * it handles the monitors in order to manage the workers
     * and their work.
     * @throws InterruptedException
     */
    public void update() throws InterruptedException {
        //If there are still documents to process,
        //then take one of them and put it in the pdfMonitor,
        //else wait for the workers to end their computation
        if(!documents.isEmpty()) {
            try {
                File f = documents.poll();
                PDDocument document = PDDocument.load(f);
                AccessPermission ap = document.getCurrentAccessPermission();
                if (!ap.canExtractContent()) {
                    throw new IOException("You do not have permission to extract text");
                }
                System.out.println("Processing file: " + f.getName());
                int workload = document.getNumberOfPages() % workers.size() == 0
                        ? document.getNumberOfPages() / workers.size()
                        : document.getNumberOfPages() / workers.size() + 1;

                this.pdfMonitor.setDocument(document, workload, documents.isEmpty());

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            for(Worker w : workers){
                w.join();
            }
        }
        notifyObservers();
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
                workers.add(new Worker(this.pdfMonitor, this.occurrencesMonitor, this.stateMonitor, this.ignoredWords));
            }
            this.workers.forEach(Worker::start);
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

    public void addObserver(ModelObserver obs){
        observers.add(obs);
    }

    private void notifyObservers() {
        for (ModelObserver obs: observers) {
            Map<String, Integer> occurrences = occurrencesMonitor.getOccurrences();
            obs.modelUpdated(occurrences
                    .keySet()
                    .stream()
                    .sorted((a, b) -> occurrences.get(b) - occurrences.get(a))
                    .limit(this.limitWords)
                    .collect(Collectors.toMap(k -> k, occurrences::get)));
        }
    }

}
