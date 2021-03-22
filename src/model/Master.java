package model;


public class Master extends Thread {
    private Model model;

    public Master(Model model){
        this.model = model;
    }

    public void run(){
        while (!this.model.getState().isFinished()) {
            if(this.model.getState().isWorking()) {
                try {
                    model.execute();
                    Thread.sleep(50);
                } catch (Exception ex) {
                }
            }
        }
    }
}
