package net.thevpc.nhttp.server.api;


import java.util.List;

public interface NWebServerRunner {
    void bootstrap(NWebConfig appWebServer);
    NWebUserResolver userResolver();
    void createContext(NWebContainer container) ;
    void initializeConfig();
}
