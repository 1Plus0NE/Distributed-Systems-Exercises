import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class Barrier implements Runnable{

    private final int nThreads;
    private int arrived;
    private final ReentrantLock barrierLock;
    private final Condition allArrived;

    public Barrier (int N){
        this.nThreads = N;
        this.arrived = 0;
        this.barrierLock = new ReentrantLock();
        this.allArrived = this.barrierLock.newCondition();
    }
    
    void await() throws InterruptedException{
        this.barrierLock.lock();
        try{
            this.arrived++;
            System.out.println("Joining barrier ...\n Number of threads: " + this.arrived + "\nMax threads: " + this.nThreads);
            if(this.arrived < this.nThreads){
                while(this.arrived < this.nThreads){
                    System.out.println("Waiting for threads...");
                    this.allArrived.await();
                }
                System.out.println("I'm free!");
            }
            else{
                System.out.println("Reached the max number of threads... Realeasing Threads...");
                this.allArrived.signalAll();
            }

        } finally{
            this.barrierLock.unlock();
        }
    }

    public void run() {
        try {
            this.await();
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}