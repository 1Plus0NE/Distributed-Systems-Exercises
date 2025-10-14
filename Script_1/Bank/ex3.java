import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ex3{
    
    public static void main(String[] args){

        final var n_threads = 10;
        final var n_deposits = 5;
        final var value = 200;
        final var expected_savings = n_threads * n_deposits * value;

        ReentrantLock savingsLock = new ReentrantLock();
        BankS1 bank = new BankS1();
        List<Thread> threads = new ArrayList<>();

        for(int i = 0; i < n_threads; i++){
            Thread t = new Thread(() ->{
                savingsLock.lock(); // locks this critic section, this way there are no multiple threads in this same section
                try{
                    for(int j = 0; j < n_deposits; j++){
                        bank.deposit(value);
                    }
                }
                finally{
                    savingsLock.unlock(); // once done, unlocks
                }
            });
            threads.add(t);
            t.start();
        }

        for(Thread t: threads){
            try{
                t.join(); // waits for remaining threads
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Deposits made successfully!\n");
        System.out.println("Expected Savings: " + expected_savings); 
        System.out.println("Actual Savings: " + bank.balance());
    }
}
