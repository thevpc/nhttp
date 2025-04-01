package net.thevpc.nhttp.server.api;

public interface NWebTokenEncoder {
    String encode(NWebToken token, NWebCallContext context);

    NWebToken decode(String token, NWebCallContext context);
}
