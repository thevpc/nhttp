package net.thevpc.nhttp.server.impl;

import net.thevpc.nhttp.server.api.*;
import net.thevpc.nhttp.server.impl.jwt.JwtToken;
import net.thevpc.nuts.util.NStringUtils;

import java.util.UUID;

public class DefaultNWebTokenBuilder implements NWebTokenBuilder {
    @Override
    public NWebToken createToken(NWebTokenRequest request, NWebCallContext context) {
        NWebTokenType type = request.getType();
        NWebUser user = request.getUser();
        JwtToken t = new JwtToken();
        t.setUserId(user == null ? null : user.getUserId());
        t.setUserName(user == null ? null : user.getUserName());
        NWebContext webContext = context.getWebContext();
        long tokenDuration = type == NWebTokenType.ACCESS ? webContext.getAccessTokenDuration()
                : webContext.getRefreshTokenDuration();
        long creationTime = System.currentTimeMillis();
        long expiryTime = creationTime + tokenDuration;
        t.setCreationTime(creationTime);
        t.setExpiryTime(expiryTime);
        t.getPayload().setJti(UUID.randomUUID().toString());
        t.getPayload().setIat(creationTime);
        t.getPayload().setExp(expiryTime);
        t.getPayload().setSub(user == null ? "" : user.getUserId());
        t.getPayload().setName(user == null ? "" : user.getUserName());
        t.getPayload().setIss(
                NStringUtils.firstNonBlankTrimmed(
                        context.getWebContext().getDisplayName(),
                        context.getWebContext().getServerName(),
                        "Server"
                )
        );
        t.getPayload().setApiKey(request.getApiKey());
        t.getPayload().setRealm(request.getRealm());
        t.getPayload().setTt(type);
        t.setType(type);
        t.setApiKey(request.getApiKey());
        t.setRealm(request.getRealm());
        return t;
    }
}
