package controller;

import model.*;
import java.io.IOException;

public class Controller {

    private final Model model;

    public Controller(Model model) {
        this.model = model;
    }

    public void setModelArgs(final String pdfDirectoryName, final String ignoredWordsFileName, final String limitWords) throws IOException {
        this.model.setArgs(pdfDirectoryName, ignoredWordsFileName, limitWords);
    }

    public void processEvent(String event) {
        try {
            new Thread(() -> {
                try {
                    Thread.sleep(50);
                    //model.start();
                    //model.stop();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }).start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
