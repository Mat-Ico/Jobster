package jobfinder.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketManager {
    private Socket socket;
    private PrintWriter  streamWtr;
    private BufferedReader streamRdr;

    public SocketManager(String host, int port) throws IOException {
        socket = new Socket(host, port);
        streamWtr = new PrintWriter(socket.getOutputStream(), true);
        streamRdr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void sendMessage(String message) {
        streamWtr.println(message);
    }

    public String receiveMessage() throws IOException {
        return streamRdr.readLine();
    }
    public void close() {
        try {
            if (streamWtr != null) {
                streamWtr.close();
            }
            if (streamRdr != null) {
                streamRdr.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
