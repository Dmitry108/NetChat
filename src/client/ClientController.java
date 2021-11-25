package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    @FXML public MenuItem exitMenu;
    @FXML public TextArea messagesTextArea;
    @FXML public TextField messageTextField;
    @FXML public Button sendButton;
    @FXML public TextField loginTextField;
    @FXML public PasswordField passwordTextField;
    @FXML public Button authButton;
    @FXML public HBox authPanel;
    @FXML public HBox messagePanel;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final String IP = "localhost";
    private final int PORT = 11111;

    private final String END = "/end";
    private final String AUTH = "/auth";
    private final String AUTH_OK = "/authOk";

    //    private boolean isAuth;
    private String nickname;

    private void setAuth(boolean isAuth) {
//        this.isAuth = isAuth;
        messagePanel.setManaged(isAuth);
        messagePanel.setVisible(isAuth);
        authPanel.setManaged(!isAuth);
        authPanel.setVisible(!isAuth);
        if (!isAuth) {
            nickname = null;
            setTitle("Chat");
        } else {
            loginTextField.clear();
            passwordTextField.clear();
            messagesTextArea.clear();
            setTitle(nickname);
        }
    }

    private void setTitle(String title) {
        Platform.runLater(() -> {
            ((Stage) authButton.getScene().getWindow()).setTitle(title);
        });
    }

    private EventHandler<ActionEvent> onExit = event -> {
        if (socket != null && !socket.isClosed()) {
            try {
                out.writeUTF(END);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    };

    private EventHandler<ActionEvent> onAuth = event -> {
        String login = loginTextField.getText();
        String password = passwordTextField.getText();
        if (login.equals("")) {
            loginTextField.requestFocus();
        } else if (password.equals("")) {
            passwordTextField.requestFocus();
        } else {
            try {
                if (socket == null || socket.isClosed()) connect(IP, PORT);
                out.writeUTF(String.format("%s %s %s", AUTH, login, password));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable onReceive = new Runnable() {
        @Override
        public void run() {
            try {
                String str;
                while (true) {
                    str = in.readUTF();
                    if (str.startsWith(AUTH_OK)) {
                        nickname = str.split(" ")[1];
                        setAuth(true);
                        break;
                    }
                    messagesTextArea.appendText(str + "\n");
                }
                while (true) {
                    str = in.readUTF();
                    if (str.equals(END)) {
                        setAuth(false);
                        break;
                    }
                    messagesTextArea.appendText(str + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
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

    private void connect(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        new Thread(onReceive).start();
    }

    private void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loginTextField.setOnAction(onAuth);
        passwordTextField.setOnAction(onAuth);
        authButton.setOnAction(onAuth);
        sendButton.setOnAction(onSend);
        messageTextField.setOnAction(onSend);
        Platform.runLater(() -> {
            Stage stage = (Stage) messagesTextArea.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                onExit.handle(new ActionEvent());
            });
        });
        exitMenu.setOnAction(onExit);
        setAuth(false);
    }
}