import mpi.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class TreeLeader {

    static int rank, size;
    // message types
    static final int IS_CANDIDATE=0;
    static final int IS_LEADER=1;

    // nb. of processes (i.e nodes of the graph)
    static final int N = 10;
    // incidence list of our graph
    //
    //   0    1
    //   |    |
    // 2-3-4  5-6-7
    //   |    |
    //   8    9
    //
    static final int incList[][] = {
            {3},  		// 0
            {5},  		// 1
            {3},  		// 2
            {0,2,4,8}, 	// 3
            {3},  		// 4
            {1,6,9},	// 5
            {5,7},  	// 6
            {6},  		// 7
            {3},  		// 8
            {5}}; 		// 9
    static int [] neighbours;
    static int timeCap = 10;

    public static int findLeader() {
        int currentLeader = rank;

        Set<Integer> neigbourNodes = new HashSet<>();
        for (int node: incList[rank]) neigbourNodes.add(node);

        Set<Integer> receivedNodes = new HashSet<>();

        int waitingNodeCount = incList[rank].length;
        int[] buffer = new int[1];
        while (waitingNodeCount > 1) {
            Status s = MPI.COMM_WORLD.Recv(buffer, 0, 1, MPI.INT, MPI.ANY_SOURCE, IS_CANDIDATE);
            println("Received: "+buffer[0]+" at "+rank);
            if (buffer[0] < currentLeader) currentLeader = buffer[0];
            waitingNodeCount--;
            println("waitingNodeCount: "+waitingNodeCount+" at "+rank);
        }

        Set<Integer> destinationSet = new HashSet<>(neigbourNodes);
        destinationSet.removeAll(receivedNodes);
        Integer destination = (Integer)destinationSet.toArray()[0];

        buffer[0] = currentLeader;
        println("Sending: "+currentLeader+" from "+rank);
        MPI.COMM_WORLD.Isend(buffer, 0, 1, MPI.INT, destination.intValue(), IS_CANDIDATE);

        // wait for last node
        println("Waiting for leader at "+rank);
        MPI.COMM_WORLD.Recv(buffer, 0, 1, MPI.INT, MPI.ANY_SOURCE, IS_CANDIDATE);
        println("Got leader "+buffer[0]+" at "+rank);
        // write leader in buffer
        if (buffer[0] < currentLeader) currentLeader = buffer[0];
        buffer[0] = currentLeader;
        println("Leader is: "+currentLeader+" at "+rank);
        Iterator<Integer> iter = neigbourNodes.iterator();
        while (iter.hasNext()) {
            Integer node = iter.next();
            MPI.COMM_WORLD.Isend(buffer, 0, 1, MPI.INT, node.intValue(), IS_CANDIDATE);
        }

        return currentLeader;
    }

    private static void println(Object o) {
        //System.out.println(o.toString());
    }

    public static void main(String[] args) {
        MPI.Init(args);
        size = MPI.COMM_WORLD.Size();
        rank = MPI.COMM_WORLD.Rank();
        neighbours = incList[rank]; // our edges in the tree graph
        if (size != N) System.out.println("run with -n "+N);
        else {
            int leader = findLeader();
            System.out.println("******rank "+rank+", leader: "+leader);
        }
        MPI.Finalize();
    }
}