import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        final int N = 10;
        List<Thread> threads = new ArrayList<>();
        Reusable_Barrier barrier = new Reusable_Barrier(N);

        for (int i = 0; i < N; i++) {
            Thread t = new Thread(barrier);
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            System.out.println("Joining Threads.");
            t.join();
        }

        System.out.println("End.");

    }
}