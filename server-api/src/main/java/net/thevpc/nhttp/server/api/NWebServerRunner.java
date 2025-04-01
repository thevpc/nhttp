package net.thevpc.nhttp.server.api;


public interface NWebServerRunner {
    void bootstrap(NWebConfig appWebServer);
    NWebUserResolver userResolver();
    void createContext(NWebContext container) ;
    void initializeConfig();
}
