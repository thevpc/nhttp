package net.thevpc.nhttp.server.impl.jwt;

import net.thevpc.nhttp.server.api.NWebToken;

public class JwtToken extends NWebToken {
    private JwtHeader header = new JwtHeader();
    private JwtPayload payload = new JwtPayload();
    private String signature;

    public JwtToken() {
        getHeader().setAlg("HS256");
        getHeader().setTyp("JWT");
    }

    public JwtHeader getHeader() {
        return header;
    }

    public JwtToken setHeader(JwtHeader header) {
        this.header = header;
        return this;
    }

    public JwtPayload getPayload() {
        return payload;
    }

    public JwtToken setPayload(JwtPayload payload) {
        this.payload = payload;
        return this;
    }

    public String getSignature() {
        return signature;
    }

    public JwtToken setSignature(String signature) {
        this.signature = signature;
        return this;
    }
}
