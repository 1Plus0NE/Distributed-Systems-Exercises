import java.util.ArrayList;
import java.util.List;

public class ex2{
    
    public static void main(String[] args){

        final var n_threads = 10;
        final var n_deposits = 5;
        final var value = 200;
        final var expected_savings = n_threads * n_deposits * value;

        BankS1 bank = new BankS1();
        
        List<Thread> threads = new ArrayList<>();

        for(int i = 0; i < n_threads; i++){
            Thread t = new Thread(() ->{ // for each created thread we are going to do N deposits with a specific value
                for(int j = 0; j < n_deposits; j++){
                    bank.deposit(value);
                }
            });
            threads.add(t);
            t.start();
        }

        for(Thread t: threads){
            try{
                t.join(); // wait for remaining threads
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Deposits made successfully!\n");
        System.out.println("Expected Savings: " + expected_savings); 
        System.out.println("Actual Savings: " + bank.balance());
    }
}
