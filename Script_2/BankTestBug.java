import java.util.Random;

public class BankTestBug {

    private static class Mover implements Runnable {
        private final BankS2 b;
        private final int accs;
        private final int iters;

        public Mover(BankS2 b, int accs, int iters) {
            this.b = b;
            this.accs = accs;
            this.iters = iters;
        }

        public void run() {
            Random rand = new Random();
            for (int m = 0; m < iters; m++) {
                int from = rand.nextInt(accs);
                int to = rand.nextInt(accs);
                if (from != to) {
                    // Força a exposição do erro:
                    // dá tempo a outras threads de intervir no meio da transferência
                    if (b.withdraw(from, 1)) {
                        try { Thread.sleep(1); } catch (InterruptedException e) {}
                        b.deposit(to, 1);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int ACCS = 10;
        int ITERS = 10000;
        BankS2 b = new BankS2(ACCS);

        // Inicializa cada conta com 1000
        for (int i = 0; i < ACCS; i++) {
            b.deposit(i, 1000);
        }

        int balance1 = b.totalBalance();
        System.out.println("Saldo inicial: " + balance1);

        // Criar várias threads
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Mover(b, ACCS, ITERS));
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        int balance2 = b.totalBalance();
        System.out.println("Saldo final: " + balance2);

        if (balance1 != balance2) {
            System.out.println("Erro exposto! O saldo total não se manteve.");
        } else {
            System.out.println("Test OK (mas pode falhar em execuções seguintes)");
        }
    }
}
