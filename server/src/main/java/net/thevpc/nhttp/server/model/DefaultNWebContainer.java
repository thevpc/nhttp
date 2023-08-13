package net.thevpc.nhttp.server.model;

import net.thevpc.nhttp.server.api.NWebContainer;
import net.thevpc.nuts.util.NAssert;

import java.util.HashMap;
import java.util.Map;

public class DefaultNWebContainer implements NWebContainer {
    private String contextPath;
    private String displayName;
    private Map<String,String> initParameters;

    public DefaultNWebContainer(String contextPath, String displayName) {
        this.contextPath = NAssert.requireNonBlank(contextPath,"contextPath");
        this.displayName = NAssert.requireNonBlank(displayName,"displayName");
        this.initParameters = new HashMap<>();
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
