import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Lock;

public class Main {

    private static Lock lockobj;
    private static int N;
    private static int numThreads;

    private static int[] A;
    private static int[] ans;


    private static ArrayList<Integer> blocks;

    public static void main(String[] args) throws Exception {
        N = Integer.parseInt(args[0]);
        numThreads = Integer.parseInt(args[1]);

        A = new int[N];
        ans = new int[N];


        generateArray();
        generateBlocks();

        long nanoStart = System.nanoTime();
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= numThreads; i++) {
            Thread t = new Thread(new Merger(i));
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
        buildAns();
        long nanoEnd = System.nanoTime();
        System.out.println(N+" , "+numThreads+" , "+(nanoEnd - nanoStart));

        verify();
    }

    private static void verify() throws Exception {


//        for (int i = 0; i < N; i++) {
//            System.out.println(ans[i]);
//        }
//        System.out.println();

        for (int i = 1; i < N; i++) {
            if (ans[i] < ans[i - 1]) {
                throw new Exception("Not sorted at index: " + i);
            }
        }


    }


    private static void buildAns() {
        int[] ind = new int[numThreads];
        for (int i = 0; i < numThreads; i++) {
            ind[i] = blocks.get(i);
        }
        int pick, val;
        for (int i = 0; i < N; i++) {
            pick = -1;
            val = Integer.MAX_VALUE;
            for (int j = 0; j < numThreads; j++) {
                if (ind[j] != blocks.get(j + 1)) {
                    if (A[ind[j]] < val) {
                        val = A[ind[j]];
                        pick = j;
                    }
                }
            }
            ans[i] = val;
            ind[pick]++;
        }
    }

    private static void generateBlocks() {
        int blockSize = N / numThreads;

        blocks = new ArrayList<>(numThreads + 1);
        blocks.add(0);
        for (int i = 1; i < numThreads; i++) {
            blocks.add(blockSize * i);
        }
        blocks.add(N);
    }

    private static void generateArray() {
        Random rand = new Random();
        for (int i = 0; i < N; i++) {
            A[i] = rand.nextInt();
        }
    }

    private static class Merger implements Runnable {

        private int start;  // inclusive
        private int end;    // exclusive
        private int[] temp;

        Merger(int no) {
            start = blocks.get(no - 1);
            end = blocks.get(no);
            temp = new int[end - start + 1];
        }

        @Override
        public void run() {
            mergeSort(start, end);
        }

        private void mergeSort(int start, int end) {
            if (start < (end - 1)) {
                int mid = (start + end) / 2;
                mergeSort(start, mid);
                mergeSort(mid, end);
                merge(start, mid, end);
            }
        }

        private void merge(int start, int mid, int end) {
            int len = end - start;
            int l = start, r = mid;
            for (int i = 0; i < len; i++) {
                if (l == mid) {
                    temp[i] = A[r++];
                } else if (r == end) {
                    temp[i] = A[l++];
                } else if (A[l] < A[r]) {
                    temp[i] = A[l++];
                } else {
                    temp[i] = A[r++];
                }
            }
            l = start;
            for (int i = 0; i < len; i++) {
                A[l++] = temp[i];
            }
        }
    }
}
