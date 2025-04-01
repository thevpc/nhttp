package net.thevpc.nhttp.server.api;

public class NWebToken {
    private String userId;
    private String userName;
    private long creationTime;
    private long expiryTime;
    private NWebTokenType type;
    private String apiKey;
    private String realm;

    public String getApiKey() {
        return apiKey;
    }

    public NWebToken setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public String getRealm() {
        return realm;
    }

    public NWebToken setRealm(String realm) {
        this.realm = realm;
        return this;
    }

    public NWebToken setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    public NWebTokenType getType() {
        return type;
    }

    public NWebToken setType(NWebTokenType type) {
        this.type = type;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public NWebToken setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public NWebToken setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public NWebToken setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

}
