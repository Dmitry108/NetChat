package client;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
////    @FXML public MenuItem exitMenu;
    @FXML public TextArea messagesTextArea;
    @FXML public TextField messageTextField;
    @FXML public Button sendButton;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final String IP = "localhost";
    private final int PORT = 11111;

    private final String END = "/end";

    private Runnable onReceive = new Runnable() {
        @Override
        public void run() {
            try {
                    String str;
                    while (true){
                        str = in.readUTF();
                        if (str.equals(END)){
                            break;
                        }
                        messagesTextArea.appendText(str + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    disconnect();
                }
//                System.exit(0);
        }
    };

    private EventHandler<ActionEvent> onSend = event -> {
        try {
            String str = messageTextField.getText();
            out.writeUTF(str);
            messageTextField.clear();
            messageTextField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    private void connect(String ip, int port){
        try {
            socket = new Socket(ip, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnect(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sendButton.setOnAction(onSend);
        messageTextField.setOnAction(onSend);

        connect(IP, PORT);
        new Thread(onReceive).start();
    }
}