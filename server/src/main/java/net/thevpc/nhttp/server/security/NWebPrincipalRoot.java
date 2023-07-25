package net.thevpc.nhttp.server.security;

public class NWebPrincipalRoot implements NWebPrincipal {
    @Override
    public String getId() {
        return "<root>";
    }

    @Override
    public String getUserName() {
        return "root";
    }

    @Override
    public boolean isAdmin() {
        return true;
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public <T> T getUser() {
        return null;
    }
}
