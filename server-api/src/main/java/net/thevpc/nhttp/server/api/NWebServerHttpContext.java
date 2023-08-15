package net.thevpc.nhttp.server.api;

import net.thevpc.nuts.web.NHttpCode;
import net.thevpc.nuts.web.NHttpMethod;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NUnsafeRunnable;

import java.util.Map;
import java.util.logging.Level;

public interface NWebServerHttpContext {

    String getFirstPath();

    boolean isEmptyPath();

    int getPathSize();

    String[] getPathParts();

    String getPathPart(int pos);

    NSession getSession();

    <T> T getBodyAs(Class<T> cl);

    String getBodyAsString();

    void sendPlainText(String ex, NHttpCode code, NWebResponseHeaders headers);

    void sendXml(String value);

    void sendXml(String value, NHttpCode code, NWebResponseHeaders headers);

    void sendJson(Object ex, NWebResponseHeaders headers);

    void sendJson(Object ex);

    void sendJson(Object ex, NHttpCode code, NWebResponseHeaders headers);

    String getPath();

    void sendError(Throwable ex);

    void sendError(NWebHttpException ex);

    void sendBytes(byte[] bytes, NHttpCode code, NWebResponseHeaders headers);

    NHttpMethod getMethod();

    NWebServerHttpContext requireAuth();

    void trace(Level level, NMsg msg);

    NWebServerHttpContext requireMethod(NHttpMethod... m);

    NWebServerHttpContext throwNoFound();

    NWebPrincipal getPrincipal();

    NOptional<NWebUser> getUser();

    NWebServerHttpContext setUser(NWebUser user);

    NOptional<NWebToken> getToken();

    NWebServerHttpContext setToken(NWebToken token);

    NWebServerHttpContext runWithUnsafe(NUnsafeRunnable callable) throws Throwable;

    Map<String, String> getQueryParams();

    String getQueryParam(String queryParam);

    boolean containsQueryParam(String queryParam);
}
