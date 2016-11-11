package company;

import java.io.Serializable;

/**
 * Created by lukas on 16.11.15.
 */
public interface IWork extends Serializable {
    void doSomeWork();
    boolean isFinished();
    void addMoreWork(IWork work);
    IWork splitWork();
    IWork empty();
}
