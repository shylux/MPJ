package mattresscompany;

import company.IWork;
import java.util.LinkedList;

/**
 * Created by lukas on 12.11.15.
 */
public class MattressTesting implements IWork {
    LinkedList<Integer> mattresses;
    Integer bestMattressScore;

    public MattressTesting() {
        bestMattressScore = -1;
        mattresses = new LinkedList<>();
    }
    public MattressTesting(int mattressCount, int maxSleepDuration) {
        this();
        for (int i = 0; i < mattressCount; i++)
            mattresses.add((int) (Math.random() * maxSleepDuration));
    }

    @Override
    public void doSomeWork() {
        Integer nextMattress = mattresses.remove();
        try {
            Thread.sleep(nextMattress);
            Integer mattressScore = nextMattress;
            System.out.println("Tested "+nextMattress);
            if (mattressScore > bestMattressScore) bestMattressScore = mattressScore;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isFinished() {
        return mattresses.size() == 0;
    }

    @Override
    public void addMoreWork(IWork newWork) {
        MattressTesting mattWork = (MattressTesting) newWork;
        mattresses.addAll(mattWork.mattresses);
        if (bestMattressScore < mattWork.bestMattressScore) bestMattressScore = mattWork.bestMattressScore;
    }

    @Override
    public IWork splitWork() {
        MattressTesting newList = new MattressTesting();
        newList.bestMattressScore = bestMattressScore;
        // Move half of the mattresses to the other list
        int mattressCount = mattresses.size();
        for (int i = 0; i < mattressCount/2; i++)
            newList.mattresses.add(mattresses.remove());

        return newList;
    }

    @Override
    public IWork empty() {
        return new MattressTesting();
    }
}
