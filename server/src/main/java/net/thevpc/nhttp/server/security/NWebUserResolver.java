package net.thevpc.nhttp.server.security;

import net.thevpc.nhttp.server.model.NWebToken;

public interface NWebUserResolver {
    NWebToken parseToken(String token);
    NWebUser loadUser(NWebToken token);
}
