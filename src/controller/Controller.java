package controller;

import gui.View;
import model.*;
import java.io.IOException;

/**
 * Controller of the application:
 * starts the computation creating master and worker threads;
 * links model and view.
 */
public class Controller {

    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private final Model model;

    private Master master;
    private boolean firstStart;

    public Controller(Model model) {
        this.firstStart = true;
        this.model = model;
    }

    /**
     * Method called when the start button is pushed.
     *
     * @param pdfDirectoryName
     * @param ignoredWordsFileName
     * @param limitWords
     * @throws IOException
     */
    public void notifyStart(final String pdfDirectoryName, final String ignoredWordsFileName, final String limitWords, final View view) throws IOException {
        if(this.firstStart) {
            this.master = new Master(this.model, view);
            this.model.setArgs(pdfDirectoryName, ignoredWordsFileName, limitWords);
            this.master.start();
            this.model.createWorkers(AVAILABLE_PROCESSORS);
            this.firstStart = false;
        }
        this.model.start();
    }

    /**
     * Method called when the stop button is pushed.
     */
    public void notifyStop() {
        this.model.stop();
    }

}
