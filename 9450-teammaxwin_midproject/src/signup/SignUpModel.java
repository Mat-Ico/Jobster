package signup;

import server.ServerImpl;
import server.ServerInterface;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;

public class SignUpModel {
    public String sendSignupRequest(String username, String password, String role,  ServerInterface server) throws RemoteException {
        server.signup(username, password, role);
        return "Signup successful";
    }
}