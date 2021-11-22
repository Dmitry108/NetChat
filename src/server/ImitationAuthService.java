package server;

import java.util.ArrayList;
import java.util.List;

public class ImitationAuthService implements IAuthService {
    private class User {
        private String login;
        private String password;
        private String nickname;

        public User(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    private List<User> users = new ArrayList<>();

    public ImitationAuthService() {
        for (int i = 1; i <= 10; i++) {
            users.add(new User("" + i, "" + i, "Bob" + i));
        }
    }

    @Override
    public String getNickByLoginPassword(String login, String password) {
        for (User user : users) {
            if (user.login.equals(login) && user.password.equals(password)) {
                return user.nickname;
            }
        }
        return null;
    }
}
