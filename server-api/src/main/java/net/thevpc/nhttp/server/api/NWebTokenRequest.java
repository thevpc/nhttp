package net.thevpc.nhttp.server.api;

import net.thevpc.nuts.util.NCopiable;

public class NWebTokenRequest implements NCopiable, Cloneable {
    private NWebUser user;
    private NWebTokenType type;
    private String realm;
    private String apiKey;

    @Override
    public NWebTokenRequest copy() {
        return (NWebTokenRequest) clone();
    }

    @Override
    protected NWebTokenRequest clone() {
        try {
            return (NWebTokenRequest) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getRealm() {
        return realm;
    }

    public NWebTokenRequest setRealm(String realm) {
        this.realm = realm;
        return this;
    }

    public String getApiKey() {
        return apiKey;
    }

    public NWebTokenRequest setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public NWebUser getUser() {
        return user;
    }

    public NWebTokenRequest setUser(NWebUser user) {
        this.user = user;
        return this;
    }

    public NWebTokenType getType() {
        return type;
    }

    public NWebTokenRequest setType(NWebTokenType type) {
        this.type = type;
        return this;
    }
}
