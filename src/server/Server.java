package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


public class Server {
    private final int PORT = 11111;

    private Vector<ClientHandler> clients;

    private void start() {
        ServerSocket server = null;
        clients = new Vector<>();
        try {
            server = new ServerSocket(PORT);
            System.out.println("Server start");
            while (true) {
                Socket socket = server.accept();
                clients.add(new ClientHandler(this, socket));
                System.out.println("Client connected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast(String message){
        for (ClientHandler client: clients) {
            client.sendMessage(message);
        }
    }

    public static void main(String[] args) {
        new Server().start();
    }
}