package net.thevpc.nhttp.server.api;

public interface NWebPrincipal {
    String getId();

    <T> T getUser();

    String getUserName();

    boolean isAdmin();

    boolean isAnonymous();
}
