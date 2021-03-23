package model;

public class StateMonitor {

    enum State {
        WAITING, STARTED, STOPPED, PDF_FINISHED, COMPUTATION_FINISHED
    }

    private State state;
    private int deadWorkers;

    public StateMonitor() {
        this.state = State.WAITING;
        this.deadWorkers = 0;
    }

    public synchronized boolean isWorking() {
        return this.state.equals(State.STARTED);
    }

    public synchronized boolean isFinished() {
        return this.state == State.COMPUTATION_FINISHED;
    }
    public synchronized boolean areDocumentsTerminated() {
        return this.state == State.PDF_FINISHED;
    }

    public synchronized boolean isStopped() {
        return this.state.equals(State.STOPPED);
    }

    public synchronized void start() {
        this.state = State.STARTED;
    }

    public synchronized void stop() {
        this.state = State.STOPPED;
    }

    public synchronized void pdfTerminated() { this.state = State.PDF_FINISHED; }

    public synchronized void computationTerminated() { this.state = State.COMPUTATION_FINISHED; }

    public synchronized void workerDeath() { this.deadWorkers = this.deadWorkers + 1; }

    public synchronized int getDeadWorkers() { return this.deadWorkers; }
}
