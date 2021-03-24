package model;

/**
 * Master thread of the program:
 * it updates the model until the computation is finished
 * and waits if the program is stopped.
 */
public class Master extends Thread {
    final private Model model;

    public Master(Model model){
        this.model = model;
    }

    public void run(){
        final long start = System.currentTimeMillis();
        while (!this.model.getState().isFinished()) {
            try {
                if (!this.model.getState().isStopped()) {
                    model.update();
                }
                Thread.sleep(50);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Total time: "+(System.currentTimeMillis()-start)+" ms.");
    }
}
