package mattresscompany;

import company.IWork;
import company.MPJWorker;
import mpi.MPI;

/**
 * Created by lukas on 16.11.15.
 */
public class MattressTestingCEO {
    public static void main(String[] args) {
        MPI.Init(args);

        MattressTesting theProblem = new MattressTesting(10, 500);
        if (MPI.COMM_WORLD.Rank() == 0) {
            System.out.print("Testing those values: ");
            for (Integer mat : theProblem.mattresses) System.out.print(mat + ", ");
            System.out.println();
        }
        MPJWorker<MattressTesting> worker = new MPJWorker<>();
        MattressTesting result = worker.startProject(theProblem);
        if (MPI.COMM_WORLD.Rank() == 0) System.out.println("Best mattress sleep was "+result.bestMattressScore);
    }
}
