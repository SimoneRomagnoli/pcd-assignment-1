package gui;

import controller.Controller;
import model.Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.stream.Collectors;

public class View extends JFrame implements ActionListener, ModelObserver {

    private static final int WIDTH = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2;
    private static final int HEIGHT = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2;

    public static final String START = "START";
    public static final String STOP = "STOP";

    private JLabel dirLabel;
    private JTextField pdfDirectory;
    private JLabel excLabel;
    private JTextField excludeWords;
    private JLabel wordsLabel;
    private JTextField numberOfWords;
    private JLabel resLabel;
    private JTextArea results;
    private JButton start;
    private JButton stop;

    private Controller controller;

    private int modUp = 0;

    public View(Controller controller) {
        super("View");

        this.createDirectoryInput();
        this.createExcludedInput();
        this.createWordsInput();
        this.createResultsOutput();
        this.createStartButton();
        this.createStopButton();

        this.setSize(WIDTH, HEIGHT);
        setResizable(false);
        this.setLayout(new BorderLayout());
        this.setVisible(true);

        this.controller = controller;
    }

    @Override
    public void modelUpdated(Map<String, Integer> occurrences) {
        modUp++;
        System.out.println("GUI notified "+modUp+" times.");
        String acc = "";
        for(String word:occurrences.keySet().stream().sorted((a, b) -> occurrences.get(b) - occurrences.get(a)).collect(Collectors.toList())) {
            acc += word+" - "+occurrences.get(word)+" times \n";
        }
        final String finalAcc = acc;
        SwingUtilities.invokeLater(() -> {
            this.results.setText(finalAcc);
        });
        System.out.println(finalAcc);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        try {
            Object source = ev.getSource();
            if (this.start.equals(source)) {
                this.controller.notifyStart(this.pdfDirectory.getText(), this.excludeWords.getText(), this.numberOfWords.getText());
                this.start.setEnabled(false);
                this.stop.setEnabled(true);
                this.pdfDirectory.setEnabled(false);
                this.excludeWords.setEnabled(false);
                this.numberOfWords.setEnabled(false);
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
        this.pdfDirectory.setText("./prova/");
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

    private void createWordsInput() {
        this.wordsLabel = new JLabel("Select a number of words:");
        this.wordsLabel.setBounds((int)(HEIGHT*0.1), (int)(HEIGHT*0.425), (int)(WIDTH*0.4), (int)(HEIGHT*0.1));
        this.numberOfWords = new JTextField(10);
        this.numberOfWords.setBounds((int)(HEIGHT*0.1), (int)(HEIGHT*0.5), (int)(WIDTH*0.4), (int)(HEIGHT*0.1));
        this.numberOfWords.setText("5");
        this.add(this.wordsLabel);
        this.add(this.numberOfWords);
    }

    private void createResultsOutput() {
        this.resLabel = new JLabel("Results:");
        this.resLabel.setBounds((int)(WIDTH*0.5), (int)(HEIGHT*0.025), (int)(WIDTH*0.5), (int)(HEIGHT*0.1));
        this.results = new JTextArea("");
        this.results.setBounds((int)(WIDTH*0.5), (int)(HEIGHT*0.1), (int)(WIDTH*0.35), (int)(HEIGHT*0.5));
        this.add(this.resLabel);
        this.add(this.results);

    }

    private void createStartButton() {
        this.start = new JButton(START);
        this.start.setBounds((int)(HEIGHT*0.1), (int)(HEIGHT*0.7), (int)(WIDTH*0.2), (int)(HEIGHT*0.1));
        this.start.addActionListener(this);
        this.add(this.start);
    }

    private void createStopButton() {
        this.stop = new JButton(STOP);
        this.stop.setBounds((int)(HEIGHT*0.6), (int)(HEIGHT*0.7), (int)(WIDTH*0.2), (int)(HEIGHT*0.1));
        this.stop.addActionListener(this);
        this.stop.setEnabled(false);
        this.add(this.stop);
    }
}
