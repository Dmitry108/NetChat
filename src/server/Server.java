package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


public class Server {
    private Vector<ClientHandler> clients;
    private IAuthService authService;

    private final int PORT = 11111;

    public Server() {
        clients = new Vector<>();
        authService = new ImitationAuthService();
    }

    private void start() {
        ServerSocket server = null;

        try {
            server = new ServerSocket(PORT);
            System.out.println("Server start");
            while (true) {
                Socket socket = server.accept();
                new ClientHandler(this, socket);
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

    public void subscribe(ClientHandler client) {
        clients.add(client);
        System.out.println("Client connected: " + client.getNickName());
    }

    public void describe(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client disconnected: " + client.getNickName());
    }

    public void broadcastMessage(ClientHandler sender, String message) throws IOException {
        message = String.format("[%s to all]: %s", sender.getNickName(), message);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void privateMessage(ClientHandler senderHandler, String receiverNickname, String message) throws IOException {
        message = String.format("[%s private to %s]: %s",
                senderHandler.getNickName(), receiverNickname, message);
        for (ClientHandler client : clients) {
            if (client.getNickName().equals(receiverNickname)) {
                client.sendMessage(message);
                senderHandler.sendMessage(message);
                return;
            }
        }
        senderHandler.sendMessage(receiverNickname + " is not available now");
    }

    public IAuthService getAuthService() {
        return authService;
    }

    public static void main(String[] args) {
        new Server().start();
    }
}