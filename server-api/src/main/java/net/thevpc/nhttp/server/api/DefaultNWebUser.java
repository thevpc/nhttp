package net.thevpc.nhttp.server.api;

public class DefaultNWebUser implements NWebUser {
    private String id;
    private String userName;
    private String password;

    public DefaultNWebUser(String id, String userName) {
        this.id = id;
        this.userName = userName;
    }

    public DefaultNWebUser(String id, String userName, String password) {
        this.id = id;
        this.userName = userName;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getUserId() {
        return id;
    }

    @Override
    public String getUserName() {
        return userName;
    }
}
