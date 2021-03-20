package gui;

import controller.Controller;
import model.OccurrencesCounter;
import model.OccurrencesMonitor;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class GUI {

    private static final int WIDTH = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2;
    private static final int HEIGHT = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2;

    private final JFrame frame;
    private JLabel dirLabel;
    private JTextField pdfDirectory;
    private JLabel excLabel;
    private JTextField excludeWords;
    private JLabel wordsLabel;
    private JTextField numberOfWords;
    private JLabel resLabel;
    private JTextArea results;

    private JButton start;

    public GUI() {
        this.frame = new JFrame("Assignment-1");

        this.createDirectoryInput();
        this.createExcludedInput();
        this.createWordsInput();
        this.createResultsOutput();
        this.createStartButton();

        frame.setSize(WIDTH, HEIGHT);
        frame.setLayout(null);
        frame.setVisible(true);

        Controller.CONTROLLER.setGui(this);
    }

    private void createDirectoryInput() {
        this.dirLabel = new JLabel("Select the directory containing your PDFs:");
        this.dirLabel.setBounds((int)(HEIGHT*0.1), (int)(HEIGHT*0.025), (int)(WIDTH*0.4), (int)(HEIGHT*0.1));
        this.pdfDirectory = new JTextField(10);
        this.pdfDirectory.setBounds((int)(HEIGHT*0.1), (int)(HEIGHT*0.1), (int)(WIDTH*0.4), (int)(HEIGHT*0.1));
        this.pdfDirectory.setText("./prova/");
        this.frame.add(this.dirLabel);
        this.frame.add(this.pdfDirectory);
    }

    private void createExcludedInput() {
        this.excLabel = new JLabel("Select the file containing excluded words:");
        this.excLabel.setBounds((int)(HEIGHT*0.1), (int)(HEIGHT*0.225), (int)(WIDTH*0.4), (int)(HEIGHT*0.1));
        this.excludeWords = new JTextField(10);
        this.excludeWords.setBounds((int)(HEIGHT*0.1), (int)(HEIGHT*0.3), (int)(WIDTH*0.4), (int)(HEIGHT*0.1));
        this.excludeWords.setText("./exclude.txt");
        this.frame.add(this.excLabel);
        this.frame.add(this.excludeWords);
    }

    private void createWordsInput() {
        this.wordsLabel = new JLabel("Select a number of words:");
        this.wordsLabel.setBounds((int)(HEIGHT*0.1), (int)(HEIGHT*0.425), (int)(WIDTH*0.4), (int)(HEIGHT*0.1));
        this.numberOfWords = new JTextField(10);
        this.numberOfWords.setBounds((int)(HEIGHT*0.1), (int)(HEIGHT*0.5), (int)(WIDTH*0.4), (int)(HEIGHT*0.1));
        this.numberOfWords.setText("5");
        this.frame.add(this.wordsLabel);
        this.frame.add(this.numberOfWords);
    }

    private void createResultsOutput() {
        this.resLabel = new JLabel("Results:");
        this.resLabel.setBounds((int)(WIDTH*0.5), (int)(HEIGHT*0.025), (int)(WIDTH*0.5), (int)(HEIGHT*0.1));
        this.results = new JTextArea("");
        this.results.setBounds((int)(WIDTH*0.5), (int)(HEIGHT*0.1), (int)(WIDTH*0.35), (int)(HEIGHT*0.5));
        this.frame.add(this.resLabel);
        this.frame.add(this.results);
    }

    private void createStartButton() {
        this.start = new JButton("Start");
        this.start.setBounds((int)(HEIGHT*0.1), (int)(HEIGHT*0.7), (int)(WIDTH*0.2), (int)(HEIGHT*0.1));
        this.start.addActionListener(e -> {
            Controller.CONTROLLER.setDirectory(this.pdfDirectory.getText());
            Controller.CONTROLLER.setIgnored(this.excludeWords.getText());
            Controller.CONTROLLER.setWords(Integer.parseInt(this.numberOfWords.getText()));
            try {
                Controller.CONTROLLER.startComputation();

            } catch (IOException | InterruptedException ioException) {
                ioException.printStackTrace();
            }
        });
        this.frame.add(this.start);
    }

    public void pushResults(Map<String, Integer> occurrences) {
        this.results.setText("");
        for(String word:occurrences.keySet().stream().sorted((a, b) -> occurrences.get(b) - occurrences.get(a)).collect(Collectors.toList())) {
            String t = this.results.getText();
            this.results.setText(t+word+" - "+occurrences.get(word)+" times \n");
        }
    }
}
