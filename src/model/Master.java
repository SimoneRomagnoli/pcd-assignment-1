package model;

public class Master extends Thread {
    private Model model;

    public Master(Model model){
        this.model = model;
    }

    public void run(){
        while (!this.model.getState().isFinished()) {
            try {
                // Always update the model while not STOPPED
                if (!this.model.getState().isStopped()) {
                    model.update();
                }
                Thread.sleep(50);
            } catch (Exception ex) {
            }

        }
    }
}
