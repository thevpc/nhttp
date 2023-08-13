package net.thevpc.nhttp.server.api;

import java.util.Map;

public interface NWebContainer {
    String getContextPath();
    String getDisplayName();
    Map<String,String> getInitParameters();
}
