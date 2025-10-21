import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Reusable_Barrier implements Runnable{

    private final int nThreads;
    private int arrived;
    private int generation;
    private final ReentrantLock barrierLock;
    private final Condition allArrived;

    public Reusable_Barrier (int N){
        this.nThreads = N;
        this.arrived = 0;
        this.generation = 0;
        this.barrierLock = new ReentrantLock();
        this.allArrived = this.barrierLock.newCondition();
    }
    
    void await() throws InterruptedException{
        this.barrierLock.lock();
        try{
            int myGeneration = this.generation;
            this.arrived++;
            System.out.println("Joining barrier ...\n Number of threads: " + this.arrived + "\nMax threads: " + this.nThreads);
            if(this.arrived < this.nThreads){
                while(this.generation == myGeneration){
                    System.out.println("Waiting for threads...");
                    this.allArrived.await();
                }
                System.out.println("Signaled... My Epoch: " + myGeneration + "; Current Epoch: " + this.generation);
            }
            else{
                System.out.println("Reached the max number of threads... Realeasing Threads...");
                this.generation++;
                this.arrived = 0;
                this.allArrived.signalAll();
            }

        } finally{
            this.barrierLock.unlock();
        }
    }

    @Override
    public void run() {
        try {
            this.await();
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}