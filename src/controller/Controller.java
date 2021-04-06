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

    public static final int N_CPU = Runtime.getRuntime().availableProcessors();
    public static final double U_CPU = 0.8;
    public static final double WAIT_COMPUTE_RATIO = 0.85;
    public static final int N_THREADS =  (int)(N_CPU * U_CPU * (1 + WAIT_COMPUTE_RATIO));

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
            this.model.createWorkersUpTo(N_THREADS);
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
