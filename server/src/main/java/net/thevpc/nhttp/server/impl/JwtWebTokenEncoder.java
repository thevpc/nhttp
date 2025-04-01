package net.thevpc.nhttp.server.impl;

import net.thevpc.nhttp.server.api.NWebCallContext;
import net.thevpc.nhttp.server.api.NWebToken;
import net.thevpc.nhttp.server.api.NWebTokenEncoder;
import net.thevpc.nhttp.server.impl.jwt.JwtHeader;
import net.thevpc.nhttp.server.impl.jwt.JwtPayload;
import net.thevpc.nhttp.server.impl.jwt.JwtToken;
import net.thevpc.nhttp.server.security.NWebSecurityUtils;
import net.thevpc.nhttp.server.util.JsonUtils;
import net.thevpc.nuts.util.NBlankable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

public class JwtWebTokenEncoder implements NWebTokenEncoder {

    @Override
    public String encode(NWebToken token, NWebCallContext context) {
        if (token instanceof JwtToken) {
            JwtToken jwt = (JwtToken) token;
            String headerBase64 = base64UrlEncode(JsonUtils.toJson(jwt.getHeader()).getBytes(StandardCharsets.UTF_8));
            String payloadBase64 = base64UrlEncode(JsonUtils.toJson(jwt.getPayload()).getBytes(StandardCharsets.UTF_8));
            String signature = signHmacSHA256(headerBase64 + "." + payloadBase64, context.getWebContext().getPrivateKey());
            jwt.setSignature(signature);
            return headerBase64 + "." + payloadBase64 + "." + signature;
        }
        return NWebSecurityUtils.encryptString(JsonUtils.toJson(token), context.getWebContext().getPrivateKey());
    }

    private static String base64UrlEncode(byte[] input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input);
    }

    private static String signHmacSHA256(String data, String secret) {
        try {
            Key secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error signing JWT", e);
        }
    }

    @Override
    public NWebToken decode(String token, NWebCallContext context) {
        if (NBlankable.isBlank(token)) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length == 3) {
            try {
                String header = parts[0];
                String payload = parts[1];
                String headerPayload = header + "." + payload;
                String signature = parts[2];
                // Recalculate signature
                String expectedSignature = signHmacSHA256(headerPayload, context.getWebContext().getPrivateKey());
                if (expectedSignature.equals(signature)) {
                    JwtToken jwtToken = new JwtToken();
                    jwtToken.setHeader(
                            JsonUtils.fromJson(new String(Base64.getUrlDecoder().decode(header)), JwtHeader.class)
                    );
                    jwtToken.setPayload(
                            JsonUtils.fromJson(new String(Base64.getUrlDecoder().decode(payload)), JwtPayload.class)
                    );
                    jwtToken.setSignature(expectedSignature);
                    jwtToken.setSignature(expectedSignature);
                    jwtToken.setCreationTime(jwtToken.getPayload().getIat());
                    jwtToken.setExpiryTime(jwtToken.getPayload().getExp());
                    jwtToken.setType(jwtToken.getPayload().getTt());
                    jwtToken.setUserId(jwtToken.getPayload().getSub());
                    jwtToken.setUserName(jwtToken.getPayload().getName());
                    jwtToken.setRealm(jwtToken.getPayload().getRealm());
                    jwtToken.setApiKey(jwtToken.getPayload().getApiKey());
                    return jwtToken;
                }
            } catch (Exception e) {
                //just ignore
            }
        } else {
            try {
                String ss = NWebSecurityUtils.decryptString(token, context.getWebContext().getPrivateKey());
                return JsonUtils.fromJson(ss, NWebToken.class);
            } catch (Exception e) {
                //just ignore
            }
        }
        return null;

    }
}
