import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {

    public static Contact parseLine(String userInput) {
        String[] tokens = userInput.split(" ");

        if (tokens[3].equals("null")) tokens[3] = null;

        return new Contact(
                tokens[0],
                Integer.parseInt(tokens[1]),
                Long.parseLong(tokens[2]),
                tokens[3],
                new ArrayList<>(Arrays.asList(tokens).subList(4, tokens.length)));
    }


    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        ContactList list = ContactList.deserialize(in);
        System.out.println("Contact List");
        for (Contact contact : list)
            System.out.println(contact);
        
        System.out.println("\nInsert new Contacts\nFormat: Name Age PhoneNumber Company Emails");

        String userInput;
        while ((userInput = input.readLine()) != null) {
            Contact newContact = parseLine(userInput);
            System.out.println(newContact.toString());
            newContact.serialize(out);
            out.flush();
        }

        socket.close();
    }
}