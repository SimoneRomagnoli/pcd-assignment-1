package controller;

import model.*;
import java.io.IOException;
import java.util.List;

public class Controller {

    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private final Model model;

    private boolean firstStart;

    private Master master;

    public Controller(Model model) {
        this.firstStart = true;
        this.model = model;
    }

    public void notifyStart(final String pdfDirectoryName, final String ignoredWordsFileName, final String limitWords) throws IOException {
        if(this.firstStart) {
            this.master = new Master(this.model);
            this.model.setArgs(pdfDirectoryName, ignoredWordsFileName, limitWords);
            this.master.start();
            this.model.createWorkers(AVAILABLE_PROCESSORS);
            this.firstStart = false;
        }
        this.model.start();
    }

    public void notifyStop() {
        this.model.stop();
    }

}
