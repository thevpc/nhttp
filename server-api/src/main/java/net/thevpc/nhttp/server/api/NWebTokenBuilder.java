package net.thevpc.nhttp.server.api;

public interface NWebTokenBuilder {
    NWebToken createToken(NWebTokenRequest request, NWebCallContext context);
}
