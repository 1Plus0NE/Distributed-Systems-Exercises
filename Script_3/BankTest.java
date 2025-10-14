import java.util.Random;

public class BankTest {

    private static class Mover implements Runnable {
        private BankS3 bank;
        private int nAccounts;

        public Mover(BankS3 bank, int nAccounts) {
            this.bank = bank;
            this.nAccounts = nAccounts;
        }

        public void run() {
            final int MOVES = 100000;
            Random rand = new Random();
            for (int i = 0; i < MOVES; i++) {
                int from = rand.nextInt(nAccounts);
                int to;
                do {
                    to = rand.nextInt(nAccounts);
                } while (to == from);
                bank.transfer(from, to, 1);
            }
        }
    }

    private static class Closer implements Runnable {
        private BankS3 bank;
        private int nAccounts;

        public Closer(BankS3 bank, int nAccounts) {
            this.bank = bank;
            this.nAccounts = nAccounts;
        }

        public void run() {
            Random rand = new Random();
            int closedSum = 0;
            for (int i = 0; i < nAccounts / 2; i++) {
                int accId = rand.nextInt(nAccounts);
                int balance = bank.closeAccount(accId);
                closedSum += balance;
                System.out.println("Closed account " + accId + " with balance: " + balance);
                try {
                    Thread.sleep(2); // brief pause to allow other threads to run
                } catch (InterruptedException e) {}
            }
            System.out.println("Total value from closed accounts: " + closedSum);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final int N = 10;
        BankS3 bank = new BankS3();
        int[] accounts = new int[N];

        for (int i = 0; i < N; i++)
            accounts[i] = bank.createAccount(1000);

        int before = bank.totalBalance(accounts);
        System.out.println("Total before mover: " + before);

        Thread t1 = new Thread(new Mover(bank, N));
        Thread t2 = new Thread(new Mover(bank, N));
        Thread closer = new Thread(new Closer(bank, N));

        // Start transfers first
        t1.start();
        t2.start();

        // Start account closures concurrently
        closer.start();

        // Wait for all to finish
        t1.join();
        t2.join();
        closer.join();

        int after = bank.totalBalance(accounts);
        System.out.println("Total after transfers and closures: " + after);
        System.out.println("Test completed.");
    }
}
