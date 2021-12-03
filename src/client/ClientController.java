package client;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Observable;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    @FXML public MenuItem exitMenuItem;
    @FXML public TextArea messagesTextArea;
    @FXML public TextField messageTextField;
    @FXML public Button sendButton;
    @FXML public TextField loginTextField;
    @FXML public PasswordField passwordTextField;
    @FXML public Button authButton;
    @FXML public HBox authPanel;
    @FXML public HBox messagePanel;
    @FXML public ListView<String> clientListView;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private boolean isAuth;
    private String nickname;

    private void setAuth(boolean isAuth) {
        this.isAuth = isAuth;
        messagePanel.setManaged(isAuth);
        messagePanel.setVisible(isAuth);
        authPanel.setManaged(!isAuth);
        authPanel.setVisible(!isAuth);
        clientListView.setManaged(isAuth);
        clientListView.setVisible(isAuth);
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

    private final EventHandler<ActionEvent> onExit = event -> {
        if (socket != null && !socket.isClosed()) {
            try {
                out.writeUTF(Const.END);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    };

    private final EventHandler<MouseEvent> onList = event -> {
        String nickname = ((ListView<String>) event.getSource()).getSelectionModel()
                .getSelectedItem();
        String message = messageTextField.getText();
        messageTextField.setText(String.format("%s %s %s", Const.PRIVATE, nickname, message));
        messageTextField.requestFocus();
        messageTextField.end();
    };

    private final EventHandler<ActionEvent> onAuth = event -> {
        String login = loginTextField.getText();
        String password = passwordTextField.getText();
        if (login.equals("")) {
            loginTextField.requestFocus();
        } else if (password.equals("")) {
            passwordTextField.requestFocus();
        } else {
            try {
                if (socket == null || socket.isClosed()) connect(Const.IP, Const.PORT);
                out.writeUTF(String.format("%s %s %s", Const.AUTH, login, password));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private final Runnable onReceive = new Runnable() {
        @Override
        public void run() {
            try {
                while (!isAuth) {
                    String str = in.readUTF();
                    if (str.startsWith(Const.AUTH_OK)) {
                        nickname = str.split(" ")[1];
                        setAuth(true);
                    } else if (str.startsWith(Const.END)) {
                        break;
                    } else {
                        messagesTextArea.appendText(str + "\n");
                    }
                }
                while (isAuth) {
                    String str = in.readUTF();
                    if (str.equals(Const.END)) {
                        setAuth(false);
                    } else if (str.startsWith(Const.CHANGE_LIST)) {
                        String[] nicknames = str.split(" ");
                        Platform.runLater(() -> {
                            clientListView.getItems().clear();
                            for (int i = 1; i < nicknames.length; i++) {
                                clientListView.getItems().add(nicknames[i]);
                            }
                        });
                    } else if (str.startsWith(Const.REMOVE)) {
                        String nickname = str.split(" ")[1];
                        Platform.runLater(() -> {
                            for (String item : clientListView.getItems()) {
                                if (item.equals(nickname)) {
                                    clientListView.getItems().remove(item);
                                    break;
                                }
                            }
                        });
                    } else {
                        messagesTextArea.appendText(str + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }
    };

    private final EventHandler<ActionEvent> onSend = event -> {
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
        clientListView.setOnMouseClicked(onList);
        exitMenuItem.setOnAction(onExit);
        setAuth(false);
    }
}