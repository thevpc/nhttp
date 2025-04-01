package net.thevpc.nhttp.server.api;

public interface NWebUserResolver {
    NWebUser loadUser(String userId);

    NWebUser loadUserAndAuthenticate(NAuthenticationRequest request);
}
