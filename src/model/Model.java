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

public class Model {

    private List<ModelObserver> observers;
    private Queue<File> documents;
    private List<String> ignoredWords;
    private int limitWords;

    List<Worker> workers;

    private final RawPagesMonitor rawPagesMonitor;
    private final StateMonitor stateMonitor;

    private OccurrencesMonitor occurrencesMonitor;

    public Model() throws IOException {
        this.observers = new ArrayList<>();
        this.ignoredWords = new ArrayList<>();
        this.rawPagesMonitor = new RawPagesMonitor();
        this.occurrencesMonitor = new OccurrencesMonitor();
        this.stateMonitor = new StateMonitor();
        this.workers = new ArrayList<>();
        this.documents = new ArrayDeque<>();
    }

    public void update() throws InterruptedException {
        if(!documents.isEmpty()) {
            try {
                File f = documents.poll();
                PDDocument document = PDDocument.load(f);
                AccessPermission ap = document.getCurrentAccessPermission();
                if (!ap.canExtractContent()) {
                    throw new IOException("You do not have permission to extract text");
                }
                System.out.println("File: " + f.getName() + " with " + document.getNumberOfPages() + " pages.");
                int workload = document.getNumberOfPages() % workers.size() == 0
                        ? document.getNumberOfPages() / workers.size()
                        : document.getNumberOfPages() / workers.size() + 1;

                // in order to make both the instruction atomically
                synchronized(this){
                    // If the queue is empty I set the flag in order to await for worker's termination
                    if (documents.isEmpty()) {
                        this.stateMonitor.setWaitingForTermination();
                    }

                    this.rawPagesMonitor.setDocument(document, workload);
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {

            System.out.println("[MASTER]: waiting for dead workers");

            for(Worker w : workers){
                w.join();
            }

            stateMonitor.setFinish();

        }
        System.out.println("[MASTER]: I will now update the GUI");
        notifyObservers();
    }



    public StateMonitor getState() {
        return this.stateMonitor;
    }

    public void start() {
        this.stateMonitor.setActive();
    }

    public void stop() {
        this.stateMonitor.setStop();
    }

    public void addObserver(ModelObserver obs){
        observers.add(obs);
    }

    private synchronized void notifyObservers() {
        for (ModelObserver obs: observers) {
            Map<String, Integer> occurrences = occurrencesMonitor.getOccurrences();
            obs.modelUpdated(occurrences
                                .keySet()
                                .stream()
                                .sorted((a, b) -> occurrences.get(b) - occurrences.get(a))
                                .limit(this.limitWords)
                                .collect(Collectors.toMap(k -> k, k -> occurrences.get(k))));
        }
    }

    public void setArgs(final String pdfDirectoryName, final String ignoredWordsFileName, final String limitWords) throws IOException {
        File pdfDirectory = new File(pdfDirectoryName);
        this.documents.addAll(Arrays.asList(Objects.requireNonNull(pdfDirectory.listFiles())));
        this.ignoredWords.addAll(Files.readAllLines(Path.of(ignoredWordsFileName)));
        this.limitWords = Integer.parseInt(limitWords);
    }

    public void createWorkers(final int n) throws IOException {
        if(this.workers.isEmpty()) {
            for (int i = 0; i < n; i++) {
                workers.add(new Worker(this.rawPagesMonitor, this.occurrencesMonitor, this.stateMonitor, this.ignoredWords));
            }
            this.workers.forEach(Worker::start);
        }
    }

}
