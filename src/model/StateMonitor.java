package model;

public class StateMonitor {

    enum State {
        ACTIVE, STOPPED, FINISHED, WAITING_FOR_TERMINATION
    }

    private State ss;

    public StateMonitor() {
        this.ss = State.STOPPED;
    }


    public synchronized boolean isFinished() {
        return this.ss == State.FINISHED;
    }

    public synchronized boolean isStopped() {
        return this.ss == State.STOPPED;
    }

    public synchronized boolean isWaitingForTermination() {
        return this.ss == State.WAITING_FOR_TERMINATION;
    }

    public synchronized void setActive(){
        this.ss = State.ACTIVE;
    }

    public synchronized void setStop(){
        this.ss = State.STOPPED;
    }

    public synchronized void setWaitingForTermination(){
        this.ss = State.WAITING_FOR_TERMINATION;
    }

    public synchronized void setFinish() {
        this.ss = State.FINISHED;
    }
}