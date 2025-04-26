package net.thevpc.nhttp.server.api;

import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NMsgCode;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NUnsafeRunnable;
import net.thevpc.nuts.web.NHttpCode;
import net.thevpc.nuts.web.NHttpMethod;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public abstract class NWebCallContextAdapter implements NWebCallContext {
    abstract protected NWebCallContext base();

    @Override
    public URI getRequestURI() {
        return base().getRequestURI();
    }

    @Override
    public String getFirstPath() {
        return base().getFirstPath();
    }

    @Override
    public boolean isEmptyPath() {
        return base().isEmptyPath();
    }

    @Override
    public int getPathSize() {
        return base().getPathSize();
    }

    @Override
    public String[] getPathParts() {
        return base().getPathParts();
    }

    @Override
    public String getPathPart(int pos) {
        return base().getPathPart(pos);
    }

    @Override
    public <T> T getRequestBodyAs(Class<T> cl) {
        return base().getRequestBodyAs(cl);
    }

    @Override
    public String getRequestBodyAsString() {
        return base().getRequestBodyAsString();
    }

    @Override
    public String getPath() {
        return base().getPath();
    }

    @Override
    public OutputStream getResponseBody() {
        return base().getResponseBody();
    }

    @Override
    public NHttpMethod getMethod() {
        return base().getMethod();
    }

    @Override
    public NWebCallContext requireAuth() {
        return base().requireAuth();
    }

    @Override
    public NWebCallContext error(NMsg msg) {
        return base().error(msg);
    }

    @Override
    public NWebCallContext trace(NHttpLogMsg msg) {
        return base().trace(msg);
    }

    @Override
    public NWebCallContext info(NMsg msg) {
        return base().info(msg);
    }

    @Override
    public NWebCallContext requireMethod(NHttpMethod... m) {
        return base().requireMethod(m);
    }

    @Override
    public NWebCallContext throwNoFound() {
        return base().throwNoFound();
    }

    @Override
    public NWebPrincipal getPrincipal() {
        return base().getPrincipal();
    }

    @Override
    public NOptional<NWebUser> getUser() {
        return base().getUser();
    }

    @Override
    public NWebCallContext setUser(NWebUser user) {
        return base().setUser(user);
    }

    @Override
    public NOptional<NWebToken> getToken() {
        return base().getToken();
    }

    @Override
    public NWebCallContext setToken(NWebToken token) {
        return base().setToken(token);
    }

    @Override
    public NWebCallContext runWithUnsafe(NUnsafeRunnable callable) throws Throwable {
        return base().runWithUnsafe(callable);
    }

    @Override
    public Map<String, List<String>> getQueryParams() {
        return base().getQueryParams();
    }

    @Override
    public NOptional<String> getQueryParam(String queryParam) {
        return base().getQueryParam(queryParam);
    }

    @Override
    public boolean containsQueryParam(String queryParam) {
        return base().containsQueryParam(queryParam);
    }

    @Override
    public NOptional<String> getRequestHeader(String header) {
        return base().getRequestHeader(header);
    }

    @Override
    public Map<String, List<String>> getRequestHeaders() {
        return base().getRequestHeaders();
    }

    @Override
    public <T> T getRequestBodyAs(Class<T> cl, NContentType contentType) {
        return base().getRequestBodyAs(cl, contentType);
    }

    @Override
    public List<String> getRequestHeaders(String header) {
        return base().getRequestHeaders(header);
    }

    @Override
    public Map<String, FormDataItem> getFormDataMap() {
        return base().getFormDataMap();
    }

    @Override
    public InputStream getRequestBody() {
        return base().getRequestBody();
    }

    @Override
    public NOptional<FormDataItem> getFormData(String name) {
        return base().getFormData(name);
    }

    @Override
    public boolean isMultipartRequest() {
        return base().isMultipartRequest();
    }

    @Override
    public NOptional<String> getMultipartRequestBoundary() {
        return base().getMultipartRequestBoundary();
    }

    @Override
    public NWebCallContext setResponseContentType(String contentType) {
        return base().setResponseContentType(contentType);
    }

    @Override
    public NWebCallContext setErrorCode(NMsgCode errorCode) {
        return base().setErrorCode(errorCode);
    }

    @Override
    public NWebCallContext sendResponseHeaders() {
        return base().sendResponseHeaders();
    }

    @Override
    public NWebCallContext addResponseHeader(String name, String value) {
        return base().addResponseHeader(name, value);
    }

    @Override
    public NWebCallContext setResponseHeader(String name, String value) {
        return base().setResponseHeader(name, value);
    }

    @Override
    public NHttpCode getResponseCode() {
        return base().getResponseCode();
    }

    @Override
    public NWebCallContext setResponseCode(NHttpCode responseCode) {
        return base().setResponseCode(responseCode);
    }

    @Override
    public NWebCallContext setTextResponse(String value) {
        return base().setTextResponse(value);
    }

    @Override
    public NWebCallContext setXmlResponse(String value) {
        return base().setXmlResponse(value);
    }

    @Override
    public NWebCallContext setJsonResponse(Object value) {
        return base().setJsonResponse(value);
    }

    @Override
    public NWebCallContext setBytesResponse(byte[] value) {
        return base().setBytesResponse(value);
    }

    @Override
    public NWebCallContext setFileResponse(NPath value) {
        return base().setFileResponse(value);
    }

    @Override
    public NWebCallContext setErrorResponse(NMsgCode errorCode) {
        return base().setErrorResponse(errorCode);
    }

    @Override
    public NWebCallContext sendResponse() {
        return base().sendResponse();
    }

    @Override
    public NWebCallContext setErrorResponse(NWebHttpException ex) {
        return base().setErrorResponse(ex);
    }

    @Override
    public NWebCallContext setErrorResponse(Throwable ex) {
        return base().setErrorResponse(ex);
    }

    @Override
    public void close() {
        base().close();
    }

    @Override
    public NWebHttpException wrapException(Throwable ex) {
        return base().wrapException(ex);
    }

    @Override
    public boolean isResponseSent() {
        return base().isResponseSent();
    }


    @Override
    public void initializeConfig() {
        base().initializeConfig();
    }

    @Override
    public NWebContext getWebContext() {
        return base().getWebContext();
    }

    @Override
    public NLoginResult authenticateWithCredentials(NAuthenticationRequest authenticationRequest) {
        return base().authenticateWithCredentials(authenticationRequest);
    }

    @Override
    public NLoginResult authenticateWithRefreshToken(String refreshToken) {
        return base().authenticateWithRefreshToken(refreshToken);
    }

    @Override
    public NWebUser authenticateWithAccessToken(String accessToken) {
        return base().authenticateWithAccessToken(accessToken);
    }

    @Override
    public NOptional<String> getApiKeyRequestHeader() {
        return base().getApiKeyRequestHeader();
    }

    @Override
    public NOptional<String> getRealmRequestHeader() {
        return base().getRealmRequestHeader();
    }
}
