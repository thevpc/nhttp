package net.thevpc.nhttp.server;

import net.thevpc.nhttp.server.security.NWebUserResolver;

public interface NWebServerRunner {
    void bootstrap(NWebServer appWebServer);
    NWebUserResolver userResolver();
    void createController() ;
    void initializeConfig();
}
