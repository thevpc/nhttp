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

public interface NWebCallContext extends AutoCloseable {

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

    NWebCallContext setResponseContentType(String contentType);

    NWebCallContext setErrorCode(NMsgCode errorCode);

    NWebCallContext sendResponseHeaders();

    OutputStream getResponseBody();

    NHttpMethod getMethod();

    NWebCallContext requireAuth();

    NWebCallContext trace(Level level, NMsg msg);

    NWebCallContext requireMethod(NHttpMethod... m);

    NWebCallContext throwNoFound();

    NWebPrincipal getPrincipal();

    NOptional<NWebUser> getUser();

    NWebCallContext setUser(NWebUser user);

    NOptional<NWebToken> getToken();

    NWebCallContext setToken(NWebToken token);

    NWebCallContext runWithUnsafe(NUnsafeRunnable callable) throws Throwable;

    Map<String, List<String>> getQueryParams();

    NOptional<String> getQueryParam(String queryParam);

    boolean containsQueryParam(String queryParam);

    NWebCallContext addResponseHeader(String name, String value);

    NWebCallContext setResponseHeader(String name, String value);

    NHttpCode getResponseCode();

    NWebCallContext setResponseCode(NHttpCode responseCode);

    NOptional<String> getApiKeyRequestHeader();

    NOptional<String> getRealmRequestHeader();

    NOptional<String> getRequestHeader(String header);

    Map<String, List<String>> getRequestHeaders();

    List<String> getRequestHeaders(String header);

    Map<String, FormDataItem> getFormaDataMap();

    InputStream getRequestBody();

    NOptional<FormDataItem> getFormaData(String name);

    boolean isMultipartRequest();

    NOptional<String> getMultipartRequestBoundary();

    NWebCallContext setTextResponse(String value);

    NWebCallContext setXmlResponse(String value);

    NWebCallContext setJsonResponse(Object value);

    NWebCallContext setBytesResponse(byte[] value);

    NWebCallContext setFileResponse(NPath value);

    NWebCallContext setErrorResponse(NMsgCode errorCode);

    NWebCallContext sendResponse();

    NWebCallContext setErrorResponse(NWebHttpException ex);

    NWebCallContext setErrorResponse(Throwable ex);

    void close();

    boolean isResponseSent();

    NLoginResult authenticateWithCredentials(NAuthenticationRequest authenticationRequest);

    NLoginResult authenticateWithRefreshToken(String refreshToken);

    NWebUser authenticateWithAccessToken(String accessToken);

    void initializeConfig();

    NWebContext getWebContext();
}
