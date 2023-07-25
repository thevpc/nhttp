package net.thevpc.nhttp.server.security;

public class NWebPrincipalAnonymous implements NWebPrincipal {
    @Override
    public String getId() {
        return "<anonymous>";
    }

    @Override
    public String getUserName() {
        return "anonymous";
    }

    @Override
    public boolean isAdmin() {
        return false;
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }

    @Override
    public <T> T getUser() {
        return null;
    }
}
