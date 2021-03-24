package gui;

import controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Responsive view of the program:
 * it updates the model (via events) through the controller;
 * it has no connection with the model,
 * but it is notified at every update.
 */
public class View extends JFrame implements ActionListener, ModelObserver {

    private static final int WIDTH = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2;
    private static final int HEIGHT = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2;

    private JLabel dirLabel;
    private JTextField pdfDirectory;
    private JLabel excLabel;
    private JTextField excludeWords;
    private JLabel limitLabel;
    private JTextField limitOfWords;
    private JLabel resLabel;
    private JTextArea results;
    private JLabel wordsLabel;
    private JTextField elaboratedWords;
    private JButton start;
    private JButton stop;

    private Controller controller;

    public View(Controller controller) {
        super("View");

        this.createDirectoryInput();
        this.createExcludedInput();
        this.createLimitWordsInput();
        this.createResultsOutput();
        this.createElaboratedWordsOutput();
        this.createStartButton();
        this.createStopButton();

        this.setSize(WIDTH, HEIGHT);
        setResizable(false);
        this.setLayout(new BorderLayout());
        this.setVisible(true);

        this.controller = controller;
    }

    @Override
    public void modelUpdated(final int words, final Optional<Map<String, Integer>> occ) {
        SwingUtilities.invokeLater(() -> {
            this.elaboratedWords.setText(""+words);
        });
        if(occ.isPresent()) {
            final Map<String, Integer> occurrences = occ.get();
            String acc = "";
            for (String word : occurrences.keySet().stream().sorted((a, b) -> occurrences.get(b) - occurrences.get(a)).collect(Collectors.toList())) {
                acc += word + " - " + occurrences.get(word) + " times \n";
            }
            final String finalAcc = acc;
            SwingUtilities.invokeLater(() -> {
                this.results.setText(finalAcc);
            });
        }
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        try {
            Object source = ev.getSource();
            if (this.start.equals(source)) {
                this.controller.notifyStart(this.pdfDirectory.getText(), this.excludeWords.getText(), this.limitOfWords.getText());
                this.start.setEnabled(false);
                this.stop.setEnabled(true);
                this.pdfDirectory.setEnabled(false);
                this.excludeWords.setEnabled(false);
                this.limitOfWords.setEnabled(false);
            }
            if (this.stop.equals(source)) {
                this.controller.notifyStop();
                this.stop.setEnabled(false);
                this.start.setEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDirectoryInput() {
        this.dirLabel = new JLabel("Select the directory containing your PDFs:");
        this.dirLabel.setBounds((int)(HEIGHT*0.1), (int)(HEIGHT*0.025), (int)(WIDTH*0.4), (int)(HEIGHT*0.1));
        this.pdfDirectory = new JTextField(10);
        this.pdfDirectory.setBounds((int)(HEIGHT*0.1), (int)(HEIGHT*0.1), (int)(WIDTH*0.4), (int)(HEIGHT*0.1));
        this.pdfDirectory.setText("./res/");
        this.add(this.dirLabel);
        this.add(this.pdfDirectory);
    }

    private void createExcludedInput() {
        this.excLabel = new JLabel("Select the file containing excluded words:");
        this.excLabel.setBounds((int)(HEIGHT*0.1), (int)(HEIGHT*0.225), (int)(WIDTH*0.4), (int)(HEIGHT*0.1));
        this.excludeWords = new JTextField(10);
        this.excludeWords.setBounds((int)(HEIGHT*0.1), (int)(HEIGHT*0.3), (int)(WIDTH*0.4), (int)(HEIGHT*0.1));
        this.excludeWords.setText("./exclude.txt");
        this.add(this.excLabel);
        this.add(this.excludeWords);
    }

    private void createLimitWordsInput() {
        this.limitLabel = new JLabel("Select a number of words:");
        this.limitLabel.setBounds((int)(HEIGHT*0.1), (int)(HEIGHT*0.425), (int)(WIDTH*0.4), (int)(HEIGHT*0.1));
        this.limitOfWords = new JTextField(10);
        this.limitOfWords.setBounds((int)(HEIGHT*0.1), (int)(HEIGHT*0.5), (int)(WIDTH*0.4), (int)(HEIGHT*0.1));
        this.limitOfWords.setText("5");
        this.add(this.limitLabel);
        this.add(this.limitOfWords);
    }

    private void createResultsOutput() {
        this.resLabel = new JLabel("Results:");
        this.resLabel.setBounds((int)(WIDTH*0.5), (int)(HEIGHT*0.025), (int)(WIDTH*0.5), (int)(HEIGHT*0.1));
        this.results = new JTextArea("");
        this.results.setBounds((int)(WIDTH*0.5), (int)(HEIGHT*0.1), (int)(WIDTH*0.35), (int)(HEIGHT*0.5));
        this.add(this.resLabel);
        this.add(this.results);
    }

    private void createElaboratedWordsOutput() {
        this.wordsLabel = new JLabel("Elaborated words:");
        this.wordsLabel.setBounds((int)(WIDTH*0.5), (int)(HEIGHT*0.625), (int)(WIDTH*0.5), (int)(HEIGHT*0.1));
        this.elaboratedWords = new JTextField(10);
        this.elaboratedWords.setBounds((int)(WIDTH*0.5), (int)(HEIGHT*0.7), (int)(WIDTH*0.35), (int)(HEIGHT*0.075));
        this.elaboratedWords.setText("0");
        this.elaboratedWords.setEnabled(false);
        this.add(this.wordsLabel);
        this.add(this.elaboratedWords);
    }

    private void createStartButton() {
        this.start = new JButton("Start");
        this.start.setBounds((int)(HEIGHT*0.1), (int)(HEIGHT*0.7), (int)(WIDTH*0.2), (int)(HEIGHT*0.1));
        this.start.addActionListener(this);
        this.add(this.start);
    }

    private void createStopButton() {
        this.stop = new JButton("Stop");
        this.stop.setBounds((int)(HEIGHT*0.46), (int)(HEIGHT*0.7), (int)(WIDTH*0.2), (int)(HEIGHT*0.1));
        this.stop.addActionListener(this);
        this.stop.setEnabled(false);
        this.add(this.stop);
    }
}
