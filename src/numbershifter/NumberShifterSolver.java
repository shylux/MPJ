package numbershifter;

import com.rits.cloning.Cloner;
import company.MPJWorker;
import mpi.MPI;

/**
 * Created by lukas on 18.11.15.
 */
public class NumberShifterSolver {
    public static void main(String[] args) {
        MPI.Init(args);

        Cloner c = new Cloner();
        NumShifter task = new NumShifter(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 0});
        MPJWorker<NSWorkingBranch> worker = new MPJWorker<>();

        NSWorkingBranch theproblem = null;
        NSWorkingBranch result = null;
        int searchDepth = 30;

        theproblem = new NSWorkingBranch(c.deepClone(task), searchDepth);
        if (MPI.COMM_WORLD.Rank() == 0) System.out.println("Search with depth: "+searchDepth);
        result = worker.startProject(theproblem);

        //while (!theproblem.isFinished()) theproblem.doSomeWork();
        //NSWorkingBranch b = (NSWorkingBranch) theproblem.splitWork();
        if (MPI.COMM_WORLD.Rank() == 0) {
            System.out.println("Best solution length: " + result.shortestSolution);
            for (Direction d : result.solutionPath) System.out.print(" " + d);
            System.out.println();
            System.out.println(worker.work.rootState);
        }
    }

}
