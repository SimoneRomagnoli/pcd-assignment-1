package model;

/**
 * Monitor that represents the state of the computation:
 * it can be started, stopped or finished.
 */
public class StateMonitor {

    enum State {
        STARTED, STOPPED, FINISHED
    }

    private State state;

    public StateMonitor() {
        this.state = State.STOPPED;
    }

    public synchronized boolean isFinished() {
        return this.state == State.FINISHED;
    }

    public synchronized boolean isStopped() {
        return this.state == State.STOPPED;
    }

    public synchronized void start(){
        this.state = State.STARTED;
    }

    public synchronized void stop(){
        this.state = State.STOPPED;
    }

    public synchronized void finish() {
        this.state = State.FINISHED;
    }
}