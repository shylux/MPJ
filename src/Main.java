import company.MPJFormatter;
import company.MPJWorker;
import mpi.MPI;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
//        MPI.Init(args);
//        int me = MPI.COMM_WORLD.Rank();
//        int size = MPI.COMM_WORLD.Size();
//        System.out.printf("Hello from <%d>!\n", me);
//        MPI.Finalize();
        Logger l = Logger.getLogger("");
        l.setLevel(Level.INFO);
        l.getHandlers()[0].setFormatter(new MPJFormatter(3));
        l.info("test");
    }
}
