package model;

public class StateMonitor {

    enum State {
        WAITING, STARTED, PAUSED, STOPPED, FINISHED
    }

    private State state;

    public StateMonitor() {
        this.state = State.WAITING;
    }

    public synchronized boolean isWorking() {
        return this.state.equals(State.STARTED);
    }

    public synchronized boolean isFinished() {
        return this.state.equals(State.FINISHED);
    }

    public synchronized void start() {
        this.state = State.STARTED;
    }

    public synchronized void pause() {
        this.state = State.PAUSED;
    }

    public synchronized void stop() {
        this.state = State.STOPPED;
    }

    public synchronized void finish() {
        this.state = State.FINISHED;
    }
}
