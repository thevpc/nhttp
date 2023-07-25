package net.thevpc.nhttp.server.util;

import net.thevpc.nhttp.server.error.NWebErrorCode;

public class NWebResponseHeaders {
    private String contentType;
    private NWebErrorCode errorCode;

    public String getContentType() {
        return contentType;
    }

    public NWebResponseHeaders setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public NWebErrorCode getErrorCode() {
        return errorCode;
    }

    public NWebResponseHeaders setErrorCode(NWebErrorCode errorCode) {
        this.errorCode = errorCode;
        return this;
    }
}