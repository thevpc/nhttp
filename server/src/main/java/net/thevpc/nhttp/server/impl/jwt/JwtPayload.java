package net.thevpc.nhttp.server.impl.jwt;

import net.thevpc.nhttp.server.api.NWebTokenType;

import java.util.ArrayList;
import java.util.List;

public class JwtPayload {
    private String iss;
    private String sub;
    private String name;
    private String aud;
    private long exp;
    private long iat;
    private long nbf;
    private String jti;
    private String role;
    private String[] permissions;
    private String department;
    private NWebTokenType tt;
    private String apiKey;
    private String realm;

    public String getRealm() {
        return realm;
    }

    public JwtPayload setRealm(String realm) {
        this.realm = realm;
        return this;
    }

    public String getApiKey() {
        return apiKey;
    }

    public JwtPayload setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public NWebTokenType getTt() {
        return tt;
    }

    public JwtPayload setTt(NWebTokenType tt) {
        this.tt = tt;
        return this;
    }

    public String getRole() {
        return role;
    }

    public JwtPayload setRole(String role) {
        this.role = role;
        return this;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public JwtPayload setPermissions(String[] permissions) {
        this.permissions = permissions;
        return this;
    }

    public String getDepartment() {
        return department;
    }

    public JwtPayload setDepartment(String department) {
        this.department = department;
        return this;
    }

    public String getName() {
        return name;
    }

    public JwtPayload setName(String name) {
        this.name = name;
        return this;
    }

    public String getIss() {
        return iss;
    }

    public JwtPayload setIss(String iss) {
        this.iss = iss;
        return this;
    }

    public String getSub() {
        return sub;
    }

    public JwtPayload setSub(String sub) {
        this.sub = sub;
        return this;
    }

    public String getAud() {
        return aud;
    }

    public JwtPayload setAud(String aud) {
        this.aud = aud;
        return this;
    }

    public long getExp() {
        return exp;
    }

    public JwtPayload setExp(long exp) {
        this.exp = exp;
        return this;
    }

    public long getIat() {
        return iat;
    }

    public JwtPayload setIat(long iat) {
        this.iat = iat;
        return this;
    }

    public long getNbf() {
        return nbf;
    }

    public JwtPayload setNbf(long nbf) {
        this.nbf = nbf;
        return this;
    }

    public String getJti() {
        return jti;
    }

    public JwtPayload setJti(String jti) {
        this.jti = jti;
        return this;
    }
}
