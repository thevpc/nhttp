package net.thevpc.nhttp.server.api;

public class NWebToken {
    private String userId;
    private String userLogin;
    private long creationTime;
    private long lastValidityTime;

    public String getUserId() {
        return userId;
    }

    public NWebToken setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public NWebToken setUserLogin(String userLogin) {
        this.userLogin = userLogin;
        return this;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public NWebToken setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public long getLastValidityTime() {
        return lastValidityTime;
    }

    public NWebToken setLastValidityTime(long lastValidityTime) {
        this.lastValidityTime = lastValidityTime;
        return this;
    }
}
