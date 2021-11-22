package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


public class Server {
    private Vector<ClientHandler> clients;
    private IAuthService authService;

    private final int PORT = 11111;




    public Server(){
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
                ClientHandler client = new ClientHandler(this, socket);
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

    public void subscribe(ClientHandler client){
        clients.add(client);
        System.out.println("Client connected: " + client.getNickName());
    }

    public void describe(ClientHandler client) {
        clients.remove(client);
    }

    public void broadcast(String message) throws IOException {
        for (ClientHandler client: clients) {
            client.sendMessage(message);
        }
    }

    public IAuthService getAuthService() {
        return authService;
    }
    public static void main(String[] args) {
        new Server().start();
    }
}