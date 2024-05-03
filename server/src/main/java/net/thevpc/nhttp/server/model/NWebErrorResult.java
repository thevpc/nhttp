package net.thevpc.nhttp.server.model;

import net.thevpc.nuts.NDTO;

public class NWebErrorResult implements NDTO {
    private boolean error;
    private String message;
    private String code;
    private String[] params;

    public NWebErrorResult() {
    }

    public NWebErrorResult(String message) {
        this.error = true;
        this.message = message;
    }

    public boolean isError() {
        return error;
    }

    public NWebErrorResult setError(boolean error) {
        this.error = error;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public NWebErrorResult setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getCode() {
        return code;
    }

    public NWebErrorResult setCode(String code) {
        this.code = code;
        return this;
    }

    public String[] getParams() {
        return params;
    }

    public NWebErrorResult setParams(String[] params) {
        this.params = params;
        return this;
    }
}
