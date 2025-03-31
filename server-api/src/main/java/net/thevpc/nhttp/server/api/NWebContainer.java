package net.thevpc.nhttp.server.api;

import net.thevpc.nuts.NEnvConditionBuilder;

import java.util.Map;

public interface NWebContainer {
    String getContextPath();
    String getDisplayName();
    Map<String,String> getInitParameters();
    WebContextBuilder createContext();
}
