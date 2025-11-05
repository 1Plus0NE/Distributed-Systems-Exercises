import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionHandle implements Runnable{

    private final EchoServer server;
    private final Socket client;

    public ConnectionHandle(EchoServer server, Socket client){
        this.server = server;
        this.client = client;
    }

    public void run(){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream());

            String line;
            int sum = 0;

            while ((line = in.readLine()) != null) {
                int value = Integer.parseInt(line);
                sum += value;
                server.addSum(value);

                out.println(sum);
                out.flush();
            }

            out.println(server.getAvg());
            out.flush();

            client.shutdownOutput();
            client.shutdownInput();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
