package net.thevpc.nhttp.server.api;

public class NLoginResult {
    private String userId;
    private String userName;
    private String accessToken;
    private String refreshToken;
    private long accessTokenLastValidityTime;
    private long refreshTokenLastValidityTime;

    public String getUserId() {
        return userId;
    }

    public NLoginResult setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public NLoginResult setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public NLoginResult setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public NLoginResult setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public long getAccessTokenLastValidityTime() {
        return accessTokenLastValidityTime;
    }

    public NLoginResult setAccessExpiryTime(long accessTokenLastValidityTime) {
        this.accessTokenLastValidityTime = accessTokenLastValidityTime;
        return this;
    }

    public long getRefreshTokenLastValidityTime() {
        return refreshTokenLastValidityTime;
    }

    public NLoginResult setRefreshExpiryTime(long refreshTokenLastValidityTime) {
        this.refreshTokenLastValidityTime = refreshTokenLastValidityTime;
        return this;
    }
}
