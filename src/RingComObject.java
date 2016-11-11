

import java.io.Serializable;

import mpi.*;

public class RingComObject {
    public static void main(String[] args) {

        int rank, size;
        MPI.Init(args);
        double startTime = MPI.Wtime();
        size = MPI.COMM_WORLD.Size();
        rank = MPI.COMM_WORLD.Rank();
        int[] recBuf = new int[1];
        int[] sndBuf = new int[1];
        sndBuf[0] = rank;

        if (size>1)

        MPI.COMM_WORLD.Allreduce(sndBuf, 0, recBuf, 0, 1, MPI.INT, MPI.SUM);
        System.out.println("rank: "+rank+" result: "+recBuf[0]);
        MPI.Finalize();
    }


    // Achtung: das class file muss an alle nodes versendet werden
    // mit der -l Option des p2pmpirun commands
    static class ObjectBuffer implements Serializable{
        String id;
        int val;
    }

}