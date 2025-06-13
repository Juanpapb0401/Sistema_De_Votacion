package sistemaVotacion;

import java.io.FileNotFoundException;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;

public class SistemaVotacion {
    public static void main(String[] args) throws FileNotFoundException {
        Communicator communicator = Util.initialize(args);
        Controller controller = new Controller();
        controller.startUI();
    }
}