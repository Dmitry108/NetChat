package server;

public interface IAuthService {
    String getNickByLoginPassword(String login, String password);
}
