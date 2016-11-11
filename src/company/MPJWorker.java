package company;

import mpi.MPI;
import mpi.Request;
import mpi.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by lukas on 12.11.15.
 */
public class MPJWorker<T extends IWork> {
    Logger l;

    enum MsgType {
        WORK_REQ, WORK_RESP, TOKEN, FINISH, RESULT;
        int i() {return this.ordinal();}
    };
    enum Token {WHITE, BLACK, NONE};

    int size = MPI.COMM_WORLD.Size();
    int me;

    public T work;
    boolean isBlackWorker = false; // gets paid half
    Token token = Token.NONE;

    public T startProject(T _work) {
        me = MPI.COMM_WORLD.Rank();
        work = _work;

        // logger
        l = Logger.getLogger("Process"+me); // root logger
        l.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.CONFIG);
        handler.setFormatter(new MPJFormatter(me));
        //l.addHandler(handler);
        l.setLevel(Level.FINEST);

        if (me == 0) {
            token = Token.WHITE;
        } else {
            work = (T) work.empty(); // bigboss will distribute the work
        }

        double startTime = MPI.Wtime();
        l.info("Start new Project");

        while (!isProjectFinished()) { // prevent potential stack overflow
            if (!work.isFinished()) {
                workOnProject();
            } else {
                idle();
            }
        }

        double elapsedTime = MPI.Wtime() - startTime;
        if (me == 0) l.info(String.format("Work finished in %d sec.\n", (int)elapsedTime));

        if (workServeReq != null) workServeReq.finalize();
        if (workReq != null) workReq.finalize();
        if (tokenReceiver != null) tokenReceiver.finalize();
        if (finishListener != null) finishListener.finalize();

        return work;
    }

    private void workOnProject() {
        while (!work.isFinished()) {
            work.doSomeWork();
            l.fine("did some work.");
            serveWorkRequest();
            receiveToken();
        }
        passToken();
    }

    private void idle() {
        while (work.isFinished()) {
            if (checkFinished()) return;
            serveWorkRequest();
            passToken();
            requestWork();
            receiveToken();
        }
    }

    Request workServeReq;
    private void serveWorkRequest() {
        // Listen
        if (workServeReq == null)
            workServeReq = MPI.COMM_WORLD.Irecv(new int[]{}, 0, 0, MPI.INT, MPI.ANY_SOURCE, MsgType.WORK_REQ.i());
        Status status = workServeReq.Test();

        // Respond
        if (status != null) {
            l.finer(String.format("Got work request from %d!. Sending a split.", status.source));
            if (!work.isFinished()) // if we are finished we send an emty response
                isBlackWorker = true;
            workServeReq = null;
            IWork[] wr_buf = new IWork[]{work.splitWork()};
            MPI.COMM_WORLD.Isend(wr_buf, 0, 1, MPI.OBJECT, status.source, MsgType.WORK_RESP.i());
        }
    }


    Request workReq;
    int targetWorker = -1;
    IWork[] w_buf;
    private void requestWork() {
        // Request
        if (workReq == null) {
            targetWorker = getRandomCoworker();
            l.finer(String.format("Request work from %d!.", targetWorker));
            MPI.COMM_WORLD.Isend(new int[]{}, 0, 0, MPI.INT, targetWorker, MsgType.WORK_REQ.i());

            w_buf = new IWork[]{work.empty()};
            workReq = MPI.COMM_WORLD.Irecv(w_buf, 0, 1, MPI.OBJECT, targetWorker, MsgType.WORK_RESP.i());
        }
        Status status = workReq.Test();
        // Response
        if (status != null) {
            // get the response to the work request
            l.finer(String.format("Receive work from %d", targetWorker));
            work.addMoreWork(w_buf[0]);
            workReq = null;
        }
    }

    /**
     * Executed when work is done. If we possess the token we pass it to the next worker.
     * If we are black the token gets infected and turns black too.
     */
    private void passToken() {
        if (token == Token.NONE) return;
        if (isBlackWorker) {
            token = Token.BLACK;
            isBlackWorker = false;
        }
        l.fine(String.format("Pass token to %d.", (me + 1) % size));
        int[] t_buf = new int[]{token.ordinal()};
        MPI.COMM_WORLD.Send(t_buf, 0, 1, MPI.INT, (me+1)%size, MsgType.TOKEN.i());
        token = Token.NONE;
    }

    /**
     * Check if we got a token and store it.
     * Returns true if we got a token.
     */
    Request tokenReceiver;
    int[] t_buf;
    private void receiveToken() {
        if (tokenReceiver == null) {
            t_buf = new int[1];
            tokenReceiver = MPI.COMM_WORLD.Irecv(t_buf, 0, 1, MPI.INT, (me - 1 + size) % size, MsgType.TOKEN.i());
        }
        Status status = tokenReceiver.Test();
        if (status != null) {
            token = Token.values()[t_buf[0]];
            tokenReceiver = null;
            l.finer(String.format("Got token from %d.", (me - 1 + size) % size));

            if (me == 0) {
                if (token == Token.BLACK)
                    token = Token.WHITE;
                else
                    collectResults();
            }
        }
    }

    private int getRandomCoworker() {
        int coworker;
        do
            coworker = (int) (Math.random() * size);
        while (coworker == me);
        return coworker;
    }

    Request finishListener;
    private boolean checkFinished() {
        if (me == 0) return finished;

        if (finishListener == null)
            finishListener = MPI.COMM_WORLD.Irecv(new int[1], 0, 0, MPI.INT, 0, MsgType.FINISH.i());
        Status status = finishListener.Test();
        if (status != null) {
            IWork[] w_buf = new IWork[]{work};
            MPI.COMM_WORLD.Send(w_buf, 0, 1, MPI.OBJECT, 0, MsgType.RESULT.i());
            finished = true;
            return true;
        }
        return false;
    }
    private void collectResults() {
        for (int i = 1; i < size; i++)
            MPI.COMM_WORLD.Send(new int[1], 0, 0, MPI.INT, i, MsgType.FINISH.i());

        List<IWork> result_list = new ArrayList<>();
        IWork[] w_buf = new IWork[1];
        for (int i = 1; i < size; i++) {
            MPI.COMM_WORLD.Recv(w_buf, 0, 1, MPI.OBJECT, i, MsgType.RESULT.i());
            result_list.add(w_buf[0]);
        }
        for (IWork w: result_list)
            work.addMoreWork(w);

        finished = true;
    }
    boolean finished;
    private boolean isProjectFinished() {
        return finished;
    }


}
