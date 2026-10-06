package Server.Model;

import Server.Controller.AuthenticationController;

public class User {
    
    private String userId;
    private String username;
    private String passwordHash;

    public User(String userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = AuthenticationController.hashPassword(password);
    }

    public boolean login(String Username, String Password) {

        return false;
    }
    
}
