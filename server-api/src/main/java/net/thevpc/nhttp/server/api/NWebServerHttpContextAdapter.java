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

public abstract class NWebServerHttpContextAdapter implements NWebServerHttpContext {
    abstract protected NWebServerHttpContext base();

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
    public NWebServerHttpContext requireAuth() {
        return base().requireAuth();
    }

    @Override
    public NWebServerHttpContext trace(Level level, NMsg msg) {
        return base().trace(level, msg);
    }

    @Override
    public NWebServerHttpContext requireMethod(NHttpMethod... m) {
        return base().requireMethod(m);
    }

    @Override
    public NWebServerHttpContext throwNoFound() {
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
    public NWebServerHttpContext setUser(NWebUser user) {
        return base().setUser(user);
    }

    @Override
    public NOptional<NWebToken> getToken() {
        return base().getToken();
    }

    @Override
    public NWebServerHttpContext setToken(NWebToken token) {
        return base().setToken(token);
    }

    @Override
    public NWebServerHttpContext runWithUnsafe(NUnsafeRunnable callable) throws Throwable {
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
    public Map<String, FormDataItem> getFormaDataMap() {
        return base().getFormaDataMap();
    }

    @Override
    public InputStream getRequestBody() {
        return base().getRequestBody();
    }

    @Override
    public NOptional<FormDataItem> getFormaData(String name) {
        return base().getFormaData(name);
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
    public NWebServerHttpContext setResponseContentType(String contentType) {
        return base().setResponseContentType(contentType);
    }

    @Override
    public NWebServerHttpContext setErrorCode(NMsgCode errorCode) {
        return base().setErrorCode(errorCode);
    }

    @Override
    public NWebServerHttpContext sendResponseHeaders() {
        return base().sendResponseHeaders();
    }

    @Override
    public NWebServerHttpContext addResponseHeader(String name, String value) {
        return base().addResponseHeader(name, value);
    }

    @Override
    public NWebServerHttpContext setResponseHeader(String name, String value) {
        return base().setResponseHeader(name, value);
    }

    @Override
    public NHttpCode getResponseCode() {
        return base().getResponseCode();
    }

    @Override
    public NWebServerHttpContext setResponseCode(NHttpCode responseCode) {
        return base().setResponseCode(responseCode);
    }

    @Override
    public NWebServerHttpContext setTextResponse(String value) {
        return base().setTextResponse(value);
    }

    @Override
    public NWebServerHttpContext setXmlResponse(String value) {
        return base().setXmlResponse(value);
    }

    @Override
    public NWebServerHttpContext setJsonResponse(Object value) {
        return base().setJsonResponse(value);
    }

    @Override
    public NWebServerHttpContext setBytesResponse(byte[] value) {
        return base().setBytesResponse(value);
    }

    @Override
    public NWebServerHttpContext setFileResponse(NPath value) {
        return base().setFileResponse(value);
    }

    @Override
    public NWebServerHttpContext setErrorResponse(NMsgCode errorCode) {
        return base().setErrorResponse(errorCode);
    }

    @Override
    public NWebServerHttpContext sendResponse() {
        return base().sendResponse();
    }

    @Override
    public NWebServerHttpContext setErrorResponse(NWebHttpException ex) {
        return base().setErrorResponse(ex);
    }

    @Override
    public NWebServerHttpContext setErrorResponse(Throwable ex) {
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
}
