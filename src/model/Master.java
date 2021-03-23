package model;

public class Master extends Thread {
    private Model model;

    public Master(Model model){
        this.model = model;
    }

    public void run(){
        while (!this.model.getState().isFinished()) {
            try {
                if (this.model.getState().isWorking()) {
                    model.update();
                }
                Thread.sleep(50);
            } catch (Exception ex) {
            }

        }
    }
}
