import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Agreement {

    private final int nThreads;
    private int arrived;
    private int phase;
    private int maxValue;
    private final ReentrantLock agreementLock;
    private final Condition allArrived; 

    public Agreement (int N) {
        this.nThreads = N;
        this.arrived = 0;
        this.phase = 0;
        this.maxValue = 0;
        this.agreementLock = new ReentrantLock();
        this.allArrived = this.agreementLock.newCondition();
    }

    int propose(int choice) throws InterruptedException{
        this.agreementLock.lock();
        try{
            int myPhase = this.phase;
            if(this.maxValue < choice){
                this.maxValue = choice;
            }
            this.arrived++;
            if(this.arrived < this.nThreads){
                while(this.phase == myPhase){
                    this.allArrived.await();
                }
            }
            else{
                this.phase++;
                this.arrived = 0;
                this.allArrived.signalAll();
            }
            return this.maxValue;
        } finally{
            this.agreementLock.unlock();
        }
    }
}