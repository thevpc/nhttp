package net.thevpc.nhttp.server.error;

public class NWebErrorCode {
    private String code;
    private String[] params;

    public NWebErrorCode(String code, String... params) {
        this.code = code;
        this.params = params;
    }

    public String getCode() {
        return code;
    }

    public String[] getParams() {
        return params;
    }
}
