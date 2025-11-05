import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class EchoServer {

    private int totalCount = 0;
    private int totalSum = 0;
    private ReentrantLock lock = new ReentrantLock();

    public void addSum(int value){
        this.lock.lock();
        try {
            totalSum += value;
            totalCount++;
        } finally {
            this.lock.unlock();
        }
    }

    public int getAvg(){
        this.lock.lock();
        try {
            return this.totalSum / this.totalCount;
        } finally {
            this.lock.unlock();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException{
        EchoServer server = new EchoServer();
        try (ServerSocket ss = new ServerSocket(12345)) {
            System.out.println("Server listening on port 12345...");

            while (true) {
                Socket client = ss.accept();
                System.out.println("New client connected!");

                Thread t = new Thread(new ConnectionHandle(server, client));
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
