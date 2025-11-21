import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

class ContactManager {
    private HashMap<String, Contact> contacts = new HashMap<>();
    private ReentrantLock contactLock = new ReentrantLock();

    // @TODO
    public void update(Contact c) {
        this.contactLock.lock();
        try {
            System.out.println("New Contact: " + c.name());
            this.contacts.put(c.name(),c);
        } finally {
            this.contactLock.unlock();
        }
    }

    // @TODO
    public ContactList getContacts() {
        contactLock.lock();
        try {
            ContactList list = new ContactList();
            for (Contact c : contacts.values()) {
                list.add(c);
            }
            return list;
        } finally {
            contactLock.unlock();
        }
    }
}

class ServerWorker implements Runnable {
    private Socket socket;
    private ContactManager manager;

    public ServerWorker(Socket socket, ContactManager manager) {
        this.socket = socket;
        this.manager = manager;
    }

    // @TODO
    @Override
    public void run() {
         try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            System.out.println("Sending contactList...");
            this.manager.getContacts().serialize(out);
            out.flush();

            System.out.println("Listening for contacts...");
            while (true){
                Contact contact = Contact.deserialize(in);
                if (contact == null)       
                    break;
                
                System.out.println("New contact found!");
                this.manager.update(contact);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                System.out.println("Lost connection with Client...");
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}



public class Server {

    public static void main (String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        ContactManager manager = new ContactManager();
        // example pre-population
        manager.update(new Contact("John", 20, 253123321, null, asList("john@mail.com")));
        manager.update(new Contact("Alice", 30, 253987654, "CompanyInc.", asList("alice.personal@mail.com", "alice.business@mail.com")));
        manager.update(new Contact("Bob", 40, 253123456, "Comp.Ld", asList("bob@mail.com", "bob.work@mail.com")));

        while (true) {
            Socket socket = serverSocket.accept();
            Thread worker = new Thread(new ServerWorker(socket, manager));
            worker.start();
        }
    }

}
