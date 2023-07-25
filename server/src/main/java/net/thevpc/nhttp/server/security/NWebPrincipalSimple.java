package net.thevpc.nhttp.server.security;

public class NWebPrincipalSimple implements NWebPrincipal {
    private NWebUser user;
    private boolean root;

    public NWebPrincipalSimple(NWebUser user) {
        this.user = user;
        this.root = false;
    }
    public NWebPrincipalSimple(NWebUser user, boolean root) {
        this.user = user;
        this.root = root;
    }

    @Override
    public <T> T getUser() {
        return (T) user;
    }

    public boolean isAdmin() {
        return root;
    }

    @Override
    public String getId() {
        return user.getId();
    }

    @Override
    public String getUserName() {
        return user.getUserName();
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }
}
