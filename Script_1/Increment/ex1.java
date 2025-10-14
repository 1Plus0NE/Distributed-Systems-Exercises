import java.util.ArrayList;
import java.util.List;

public class ex1{
    
    public static void main(String[] args){

        List<Thread> threads = new ArrayList<>();

        for(int i = 0; i < 10; i++){
            Thread t = new Thread(new Increment());
            threads.add(t); // adds created thread to the list
            t.start();
        }

        for(Thread t: threads){
            try{
                t.join(); // waits for remaining threads
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Fim");

    }
}
