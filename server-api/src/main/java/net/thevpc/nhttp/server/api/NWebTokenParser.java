package net.thevpc.nhttp.server.api;

public interface NWebTokenParser {
    NWebToken parseToken(String token);
}
