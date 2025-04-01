package net.thevpc.nhttp.server.impl.jwt;

public class JwtHeader {
    private String alg;
    private String typ;

    public String getAlg() {
        return alg;
    }

    public JwtHeader setAlg(String alg) {
        this.alg = alg;
        return this;
    }

    public String getTyp() {
        return typ;
    }

    public JwtHeader setTyp(String typ) {
        this.typ = typ;
        return this;
    }
}
