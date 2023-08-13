package net.thevpc.nhttp.server.api;

public interface NWebUserResolver {
    NWebToken parseToken(String token);
    NWebUser loadUser(NWebToken token);
}
