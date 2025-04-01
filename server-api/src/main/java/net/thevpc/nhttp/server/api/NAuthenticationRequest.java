package net.thevpc.nhttp.server.api;

public class NAuthenticationRequest {
    private String userName;
    private String password;
    private String apiKey;
    private String realm;

    public String getUserName() {
        return userName;
    }

    public NAuthenticationRequest setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public NAuthenticationRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getApiKey() {
        return apiKey;
    }

    public NAuthenticationRequest setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public String getRealm() {
        return realm;
    }

    public NAuthenticationRequest setRealm(String realm) {
        this.realm = realm;
        return this;
    }
}
