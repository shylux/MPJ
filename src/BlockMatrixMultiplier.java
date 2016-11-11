
import mpi.MPI;
import org.apache.commons.lang3.StringUtils;

import java.util.Random;

/**
 * @author ps
 *
 */
public class BlockMatrixMultiplier extends Thread{
    private static int M;
    private static int N;
    private static int MULT;
    private static int [] a;
    private static int [] b;
    private static int [] c;
    static private boolean initiated;
    // instance variables
    private int i,j,k;
    //	public  BlockMatrixMultiplier2(int n, int mult) {
//		if (n % mult != 0) throw new RuntimeException("'n' must dividable by 'mult'");
//		N = n;
//		MULT = mult;
//		M = N/mult;
//		a = new int[N*N];
//		b = new int[N*N];
//		c = new int[N*N];
//	}
    public  BlockMatrixMultiplier(int [] a, int [] b, int n, int mult) {
        if (a.length != n*n) throw new RuntimeException("a should have dimension N*N!");
        if (a.length != b.length) throw new RuntimeException("a,b must have equal length!" );
        if (n % mult != 0) throw new RuntimeException("'n' must dividable by 'mult'");
        N = n;
        MULT = mult;
        M = N/mult;
        this.a = a;
        this.b = b;
        c = new int[N*N];
    }

    private  BlockMatrixMultiplier(int i, int j, int k) {
        // the run method of this instance
        // should calculates part of the result C[i][j]
        this.i =i;
        this.j =j;
        this.k =k;
    }

    public int [] calculate(){
        // create the threads:
        Thread [] ts = new Thread[MULT*MULT*MULT];
        for(int i=0;i<MULT;i++)
            for (int j=0;j<MULT;j++)
                for(int k=0;k<MULT;k++){
                    Thread t = new BlockMatrixMultiplier(i,j,k);
                    ts[i*MULT*MULT+j*MULT+k] = t;
                    t.start();
                }
        for(int i=0;i<MULT*MULT*MULT;i++)
            try {
                ts[i].join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        return c;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getId());
        for (int n=0;n<M;n++)
            for (int m=0;m<M;m++)
                //[Cij]nm (block i,j) element n,m
                // = sum over l [Aik]nl*[Bkj]lm
                for (int l=0;l<M;l++)
                    c[(i*M+n)*N+j*M+m] += a[(i*M+n)*N+k*M+l]*
                            b[(k*M+l)*N+j*M+m];
    }

    public static void printArr(int[][] arr) {
        for (int ix = 0; ix < arr.length; ix++) {
            for (int iy = 0; iy < arr.length; iy++) {
                System.out.print(StringUtils.leftPad("" + arr[ix][iy], 5, ' '));
            }
            System.out.println();
        }
    }


    public static void main(String[] args) {
        MPI.Init(args);

        int size = MPI.COMM_WORLD.Size();
        Address me = new Address(MPI.COMM_WORLD.Rank(), MPI.COMM_WORLD.Size());
        System.out.println("Hi from process "+me);

        if (me.mpiNr == 0) {
            Random rand = new Random(645);
            boolean multithread = false;
            int N = 1000;

            int[][] a = new int[N][N];
            int[][] b = new int[N][N];
            for (int ix = 0; ix < N; ix++) {
                for (int iy = 0; iy < N; iy++) {
                    a[ix][iy] = rand.nextInt(10);
                    b[ix][iy] = rand.nextInt(10);
                }
            }
            printArr(a);
        }
        long time = System.nanoTime();


//        int [] a = new int [N*N];
//        int [] b = new int [N*N];
//        for (int i=0;i<N*N;i++) {
//            a[i] = rand.nextInt(10);
//            b[i] = rand.nextInt(10);
//        }
//        BlockMatrixMultiplier bl = new BlockMatrixMultiplier(a,b,N,2);
//        long time = System.nanoTime();
//        if (multithread) bl.calculate();
//        else{
//            // one thread solution:
//            for (int i=0;i<N;i++) {
//                for (int j=0;j<N;j++) {
//                    c[i*N+j]=0;
//                    for (int k=0;k<N;k++) {
//                        c[i*N+j] += a[i*N+k]*b[k*N+j];
//                    }
//                }
//            }
//        }
        //System.out.println((System.nanoTime()-time)*1e-9);
    }
}