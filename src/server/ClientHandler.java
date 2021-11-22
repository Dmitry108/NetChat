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
    private final String AUTH = "/auth";
    private final String AUTH_OK = "/authOk";
    private final String PRIVATE = "/private";

    private String nickName;

    public ClientHandler(Server server, Socket socket) {
////        this.server = server;
////        this.socket = socket;
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    String str;
                    while (true) {
                        str = in.readUTF();
                        if (str.startsWith(AUTH)) {
                            String[] strParsed = str.split(" ");
                            nickName = server.getAuthService()
                                    .getNickByLoginPassword(strParsed[1], strParsed[2]);
                            if (nickName != null) {
                                server.subscribe(this);
                                sendMessage(AUTH_OK + " " + nickName);
                                break;
                            } else {
                                sendMessage("Incorrect login or password");
                            }
                        }
                    }
                    while (true) {
                        str = in.readUTF();
                        if (str.equals(END)) {
                            sendMessage(END);
                            server.describe(this);
                            break;
                        } else if (str.startsWith(PRIVATE)) {
                            String[] token = str.split(" ", 3);
                            server.privateMessage(this, token[1], token[2]);
                        } else {
                            System.out.println(str);
//                            sendMessage("echo: " + str);
                            server.broadcastMessage(this, str);
                        }
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

    public String getNickName() {
        return nickName;
    }
}