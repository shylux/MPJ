package numbershifter;

import com.rits.cloning.Cloner;
import company.IWork;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import mpi.MPI;
import numbershifter.NumShifter.InvalidActionException;

/**
 * Created by lukas on 12.11.15.
 */
public class NSWorkingBranch implements IWork {
    NumShifter rootState;
    int maxSearchDepth;
    int cancelSearchDepth = 21;

    int shortestSolution = -1;
    List<Direction> solutionPath;

    ArrayList<Direction> workingPath;
    NumShifter workingNumShifter;
    ArrayList<ArrayList<Direction>> undiscoveredPaths;

    public NSWorkingBranch(NumShifter _rootState, int _maxSearchDepth) {
        rootState = _rootState;
        cancelSearchDepth = _maxSearchDepth;
        maxSearchDepth = 3;
        workingPath = new ArrayList<>();
        workingNumShifter = (NumShifter) rootState.clone();
        undiscoveredPaths = new ArrayList<>();
        undiscoveredPaths.add(new ArrayList<>(Arrays.asList(Direction.values())));
    }

    @Override
    public void doSomeWork() {
        int expandLevel = workingPath.size();

        if (undiscoveredPaths.get(expandLevel).size() == 0) {// we checked all branches
            undiscoveredPaths.remove(expandLevel);
            Direction undoStep = workingPath.remove(expandLevel - 1);
            try {
                workingNumShifter.shift(undoStep.opposite());
            } catch (InvalidActionException e) { throw new Error(); }
            return;
        }
        Direction expandDirection = undiscoveredPaths.get(expandLevel).remove(0);

        if (expandLevel == maxSearchDepth) return; // we would make the expandLevel+1 step which exceeds maxSearchDepth
        if (shortestSolution >= 0 && expandLevel-1 == shortestSolution) return; // the solution in this branch will not be better

        try {
            workingNumShifter.shift(expandDirection);
        } catch (InvalidActionException e) { return; } // dont expand an invalid direction

        ArrayList<Direction> undiscoveredDirections = new ArrayList<>(Arrays.asList(Direction.values()));
        undiscoveredDirections.remove(expandDirection.opposite());
        workingPath.add(expandDirection);
        undiscoveredPaths.add(undiscoveredDirections);

        if (workingNumShifter.isSolved()) {
            solutionPath = new ArrayList<>(workingPath);
            shortestSolution = solutionPath.size();
        }
    }

    @Override
    public boolean isFinished() {
        if (undiscoveredPaths.size() == 1 && undiscoveredPaths.get(0).size() == 0) {
            if (MPI.COMM_WORLD.Rank() == 0 && maxSearchDepth < cancelSearchDepth && shortestSolution == -1) {
                maxSearchDepth++;
                System.out.println("Search limit increased to "+maxSearchDepth);
                undiscoveredPaths.get(0).addAll(new ArrayList<>(Arrays.asList(Direction.values())));
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void addMoreWork(IWork work) {
        Cloner c = new Cloner();
        NSWorkingBranch w = c.deepClone((NSWorkingBranch) work);
        if (w.shortestSolution != -1 && (shortestSolution == -1 || shortestSolution > w.shortestSolution)) {
            shortestSolution = w.shortestSolution;
            solutionPath = w.solutionPath;
        }
        rootState = w.rootState;
        workingPath = w.workingPath;
        undiscoveredPaths = w.undiscoveredPaths;
        workingNumShifter = w.workingNumShifter;
        maxSearchDepth = w.maxSearchDepth;
    }

    @Override
    public IWork splitWork() {
        Cloner c = new Cloner();
        NSWorkingBranch w = c.deepClone(this);
        // Split the undiscovered paths
        boolean mine = true; // one for me, one for you, one for me, ...
        for (int i = 0; i < undiscoveredPaths.size(); i++) {
            // specify what work we give away
            ArrayList<Direction> arr = new ArrayList<>();
            for (int y = undiscoveredPaths.get(i).size()-1; y >= 0; y--) {
                if (!mine) {
                    arr.add(undiscoveredPaths.get(i).remove(y));
                }
                mine = !mine;
            }
            w.undiscoveredPaths.set(i, arr);
        }
        return w;
    }

    @Override
    public IWork empty() {
        NSWorkingBranch b = new NSWorkingBranch(rootState, maxSearchDepth);
        b.undiscoveredPaths.set(0, new ArrayList<>());
        return b;
    }
}