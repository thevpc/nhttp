package net.thevpc.nhttp.server.security;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NMsgCode;
import net.thevpc.nuts.util.NMsgCodeException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class NWebSecurityUtils {

    public static String hash(String originalString) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(
                    originalString.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            int u = String.valueOf(originalString).hashCode();
            return Integer.toHexString(u);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String encryptString(String strToEncrypt, String secret, NSession session) {
        try {
            //strToEncrypt must be multiple of 16 (bug in jdk11)
            byte[] bytes = strToEncrypt.getBytes(StandardCharsets.UTF_8);
            int v = bytes.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write((v >>> 24) & 0xFF);
            out.write((v >>> 16) & 0xFF);
            out.write((v >>> 8) & 0xFF);
            out.write((v >>> 0) & 0xFF);
            out.write(bytes);
            int s = v + 4;
            while (s % 16 != 0) {
                out.write(0);
                s++;
            }
            bytes = out.toByteArray();

            KeyInfo k = createKeyInfo(secret, session);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, k.secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(bytes));
        } catch (Exception ex) {
            throw new NMsgCodeException(session, new NMsgCode("Security.EncryptionFailed"), NMsg.ofC("encryption failed : %s", ex), ex);
        }
    }

    public static String decryptString(String strToDecrypt, String secret, NSession session) {
        if (secret == null || secret.trim().length() == 0) {
            throw new IllegalArgumentException("missing token");
        }
        try {
            KeyInfo k = createKeyInfo(secret, session);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, k.secretKey);
            byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));

            //bytes is padded to be multiple of 16 (bug in jdk11)
            int ch1 = bytes[0] & 0xff;
            int ch2 = bytes[1] & 0xff;
            int ch3 = bytes[2] & 0xff;
            int ch4 = bytes[3] & 0xff;
            if ((ch1 | ch2 | ch3 | ch4) < 0)
                throw new EOFException();
            int v = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
            bytes = Arrays.copyOfRange(bytes, 4, 4 + v);
            return new String(bytes);
        } catch (Exception ex) {
            throw new NMsgCodeException(session, new NMsgCode("Security.DecryptionFailed"), NMsg.ofC("decryption failed : %s", ex), ex);
        }
    }

    private static KeyInfo createKeyInfo(String password, NSession session) {
        if (password == null || password.length() == 0) {
            password = "password";
        }
        MessageDigest sha = null;
        KeyInfo k = new KeyInfo();
        try {
            k.key = password.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-256");
            k.key = sha.digest(k.key);
            k.secretKey = new SecretKeySpec(k.key, "AES");
        } catch (NoSuchAlgorithmException ex) {
            throw new NMsgCodeException(session, new NMsgCode("Security.DecryptionFailed"),
                    NMsg.ofC("encryption key building failed : %s", ex),
                    ex);
        }
        return k;
    }

    private static class KeyInfo {

        SecretKeySpec secretKey;
        byte[] key;
    }
}
