import controller.Controller;
import gui.View;
import model.Master;
import model.Model;

import java.io.IOException;

public class SequentialWordCounter {

    public static void main(String[] args) throws IOException {
        final Model model = new Model();
        final Controller controller = new Controller(model);
        final View view = new View(controller);
        model.addObserver(view);

        final Master master = new Master(model);
        master.start();
    }


}
