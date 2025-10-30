package login;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LogInModel {

    public boolean attemptLogin(String username, String password) {
        try (Socket socket = new Socket("localhost", 5000);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("LOGIN");
            out.flush();  // Ensure the server processes request immediately
            out.writeObject(username);
            out.flush();
            out.writeObject(password);
            out.flush();

            String response = (String) in.readObject();

            System.out.println("Server Response: " + response); // Debugging

            return response.equalsIgnoreCase("Job Finder Logged In Successfully!") ||
                    response.equalsIgnoreCase("Employer Logged In Successfully!");

        } catch (IOException | ClassNotFoundException e) {
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(null, "Error: Could not connect to the server.\n" + e.getMessage(),
                            "Connection Error", JOptionPane.ERROR_MESSAGE)
            );
            e.printStackTrace();
            return false;
        }
    }
}
