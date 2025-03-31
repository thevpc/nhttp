package net.thevpc.nhttp.server.model;

import com.sun.net.httpserver.HttpServer;
import net.thevpc.nhttp.server.WebContextBuilderImpl;
import net.thevpc.nhttp.server.api.NWebContainer;
import net.thevpc.nhttp.server.api.WebContextBuilder;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NStringUtils;

import java.util.HashMap;
import java.util.Map;

public class DefaultNWebContainer implements NWebContainer {
    private String contextPath;
    private String displayName;
    private Map<String, String> initParameters;
    private HttpServer server;

    public DefaultNWebContainer(String contextPath, String displayName, HttpServer server) {
        this.contextPath = NStringUtils.firstNonBlank(NStringUtils.trim(contextPath), "/");
        this.displayName = NStringUtils.firstNonBlank(displayName, "Path " + contextPath);
        this.initParameters = new HashMap<>();
        this.server = server;
    }

    @Override
    public WebContextBuilder createContext() {
        return new WebContextBuilderImpl(server);
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Map<String, String> getInitParameters() {
        return initParameters;
    }
}
