package model;

public class ElaboratedWordsMonitor {

    private int elaboratedWords;

    public ElaboratedWordsMonitor() {
        this.elaboratedWords = 0;
    }

    public synchronized void add(final int words) {
        this.elaboratedWords += words;
    }

    public synchronized int getElaboratedWords() {
        return this.elaboratedWords;
    }
}
