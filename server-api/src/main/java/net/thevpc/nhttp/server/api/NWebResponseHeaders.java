package net.thevpc.nhttp.server.api;


import net.thevpc.nuts.util.NMsgCode;

public class NWebResponseHeaders {
    private String contentType;
    private NMsgCode errorCode;

    public String getContentType() {
        return contentType;
    }

    public NWebResponseHeaders setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public NMsgCode getErrorCode() {
        return errorCode;
    }

    public NWebResponseHeaders setErrorCode(NMsgCode errorCode) {
        this.errorCode = errorCode;
        return this;
    }
}
