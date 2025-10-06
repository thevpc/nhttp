package net.thevpc.nhttp.server.api;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NMsgCode;
import net.thevpc.nuts.net.NHttpCode;

public interface NResponseResource {
    NResponseResource setStringResponse(String value);

    NResponseResource setXmlResponse(String value);

    NResponseResource setJsonResponse(Object value);

    NResponseResource setBytesResponse(byte[] value);

    NResponseResource setFileResponse(NPath value);

    NResponseResource setErrorResponse(NHttpCode code, NMsgCode errorCode);

    NResponseResource send();

    String getContentType();

    NResponseResource setContentType(String contentType);

    NHttpCode getCode();

    NResponseResource setCode(NHttpCode code);
}
