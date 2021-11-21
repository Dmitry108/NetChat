package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
////    private Server server;
////    private Socket socket;
////    private DataInputStream in;
    private DataOutputStream out;

    private final String END = "/end";

    public ClientHandler(Server server, Socket socket) {
////        this.server = server;
////        this.socket = socket;

        try {
           DataInputStream in = new DataInputStream(socket.getInputStream());
           out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true){
                        String str = in.readUTF();
                        if (str.equals(END)) {
                            sendMessage(END);
                            server.describe(this);
                            System.out.println("Client disconnected");
                            break;
                        }
                        System.out.println(str);
                        sendMessage("echo: " + str);
                        server.broadcast(str);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) throws IOException {
        out.writeUTF(message);
    }
}