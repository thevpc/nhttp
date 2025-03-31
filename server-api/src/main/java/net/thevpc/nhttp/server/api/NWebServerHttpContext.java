package net.thevpc.nhttp.server.api;

import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NMsgCode;
import net.thevpc.nuts.web.NHttpCode;
import net.thevpc.nuts.web.NHttpMethod;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NUnsafeRunnable;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public interface NWebServerHttpContext extends AutoCloseable {

    URI getRequestURI();

    String getFirstPath();

    boolean isEmptyPath();

    int getPathSize();

    String[] getPathParts();

    String getPathPart(int pos);

    <T> T getRequestBodyAs(Class<T> cl);

    <T> T getRequestBodyAs(Class<T> cl, NContentType contentType);

    String getRequestBodyAsString();

    String getPath();

    NWebHttpException wrapException(Throwable ex);

    NWebServerHttpContext setResponseContentType(String contentType);

    NWebServerHttpContext setErrorCode(NMsgCode errorCode);

    NWebServerHttpContext sendResponseHeaders();

    OutputStream getResponseBody();

    NHttpMethod getMethod();

    NWebServerHttpContext requireAuth();

    NWebServerHttpContext trace(Level level, NMsg msg);

    NWebServerHttpContext requireMethod(NHttpMethod... m);

    NWebServerHttpContext throwNoFound();

    NWebPrincipal getPrincipal();

    NOptional<NWebUser> getUser();

    NWebServerHttpContext setUser(NWebUser user);

    NOptional<NWebToken> getToken();

    NWebServerHttpContext setToken(NWebToken token);

    NWebServerHttpContext runWithUnsafe(NUnsafeRunnable callable) throws Throwable;

    Map<String, List<String>> getQueryParams();

    NOptional<String> getQueryParam(String queryParam);

    boolean containsQueryParam(String queryParam);

    NWebServerHttpContext addResponseHeader(String name, String value);

    NWebServerHttpContext setResponseHeader(String name, String value);

    NHttpCode getResponseCode();

    NWebServerHttpContext setResponseCode(NHttpCode responseCode);

    NOptional<String> getRequestHeader(String header);

    Map<String, List<String>> getRequestHeaders();

    List<String> getRequestHeaders(String header);

    Map<String, FormDataItem> getFormaDataMap();

    InputStream getRequestBody();

    NOptional<FormDataItem> getFormaData(String name);

    boolean isMultipartRequest();

    NOptional<String> getMultipartRequestBoundary();

    NWebServerHttpContext setTextResponse(String value);

    NWebServerHttpContext setXmlResponse(String value);

    NWebServerHttpContext setJsonResponse(Object value);

    NWebServerHttpContext setBytesResponse(byte[] value);

    NWebServerHttpContext setFileResponse(NPath value);

    NWebServerHttpContext setErrorResponse(NMsgCode errorCode);

    NWebServerHttpContext sendResponse();

    NWebServerHttpContext setErrorResponse(NWebHttpException ex);

    NWebServerHttpContext setErrorResponse(Throwable ex);

    void close();

    boolean isResponseSent();
}
