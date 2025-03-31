package net.thevpc.nhttp.server.api;

public interface WebContextBuilder {
    Handler getHandler();

    WebContextBuilder setHandler(Handler handler);

    UserResolver getUserResolver();

    WebContextBuilder setUserResolver(UserResolver userResolver);

    TokenResolver getTokenResolver();

    WebContextBuilder setTokenResolver(TokenResolver tokenResolver);

    NWebLogger getLogger();

    WebContextBuilder setLogger(NWebLogger logger);

    String getContextPath();

    WebContextBuilder setContextPath(String contextPath);
    void bind();
    interface Handler{
        void handle(NWebServerHttpContext rc);
    }
    interface UserResolver{
        NWebUser loadUser(NWebToken token);
    }
    interface TokenResolver{
        NWebToken parseToken(String token);
    }
}
