package net.thevpc.nhttp.server.security;

public interface NWebPrincipal {
    String getId();

    <T> T getUser();

    String getUserName();

    boolean isAdmin();

    boolean isAnonymous();
}
