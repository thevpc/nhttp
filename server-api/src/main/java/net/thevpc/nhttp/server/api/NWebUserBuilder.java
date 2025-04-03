package net.thevpc.nhttp.server.api;

public class NWebUserBuilder {
    private String userId;
    private String userName;
    private String password;

    public String getUserId() {
        return userId;
    }

    public NWebUserBuilder setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public NWebUserBuilder setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public NWebUserBuilder setPassword(String password) {
        this.password = password;
        return this;
    }
    public NWebUser build() {
        return new DefaultNWebUser(userId, userName, password);
    }
}
