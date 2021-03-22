package model;

import gui.ModelObserver;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Model {

    public static final String START = "START";
    public static final String PAUSE = "PAUSE";
    public static final String STOP = "STOP";

    private List<ModelObserver> observers;
    private File pdfDirectory;
    private List<String> ignoredWords;
    private int limitWords;

    private final RawPagesMonitor rawPagesMonitor;
    private final StateMonitor stateMonitor;

    private OccurrencesMonitor occurrencesMonitor;

    public Model() throws IOException {
        this.observers = new ArrayList<>();
        this.ignoredWords = new ArrayList<>();
        this.rawPagesMonitor = new RawPagesMonitor();
        this.occurrencesMonitor = new OccurrencesMonitor();
        this.stateMonitor = new StateMonitor();
    }

    public synchronized void update(String event){
        switch (event) {
            case START:
                this.stateMonitor.start();
                break;
            case PAUSE:
                this.stateMonitor.pause();
                break;
            case STOP:
                this.stateMonitor.stop();
                break;
        }
        notifyObservers();
    }

    public synchronized StateMonitor getState() {
        return this.stateMonitor;
    }

    public void addObserver(ModelObserver obs){
        observers.add(obs);
    }

    private void notifyObservers(){
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
        this.pdfDirectory = new File(pdfDirectoryName);
        this.ignoredWords.addAll(Files.readAllLines(Path.of(ignoredWordsFileName)));
        this.limitWords = Integer.parseInt(limitWords);
    }

    public void execute() throws IOException {
        if(this.stateMonitor.isWorking()) {
            List<Worker> workers = new ArrayList<>();
            int threads = Runtime.getRuntime().availableProcessors();
            for (int i = 0; i < threads; i++) {
                final Worker stripper = new Worker(rawPagesMonitor, occurrencesMonitor, stateMonitor, ignoredWords);
                workers.add(stripper);
            }
            System.out.println("Threads: " + threads + ".");

            final long start = System.currentTimeMillis();
            try {
                workers.forEach(Worker::start);
                for (File f : Objects.requireNonNull(this.pdfDirectory.listFiles())) {
                    PDDocument document = PDDocument.load(f);
                    AccessPermission ap = document.getCurrentAccessPermission();
                    if (!ap.canExtractContent()) {
                        throw new IOException("You do not have permission to extract text");
                    }
                    System.out.println("File: " + f.getName() + " with " + document.getNumberOfPages() + " pages.");
                    int workload = document.getNumberOfPages() % workers.size() == 0
                            ? document.getNumberOfPages() / workers.size()
                            : document.getNumberOfPages() / workers.size() + 1;

                    this.rawPagesMonitor.setDocument(document, workload);
                }
                this.stateMonitor.finish();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            //System.out.println("Total time: " + (System.currentTimeMillis() - start) + " ms.");
        }
        notifyObservers();
    }
}
