package com.lazy.jmcomic.api.v2.crypto.sign;

import org.bouncycastle.util.encoders.Hex;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 请求签名生成
 * @since 2026.3.15
 * @author lazy
 */
public final class SignatureGenerator {
    private SignatureGenerator() {}
    public record Sign(String token,String tokenParam){}

    /**
     *
     * @param tk 客户端token
     * @param version 客户端版本
     * @param unixSeconds
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static Sign generate(String tk,String version,long unixSeconds) throws NoSuchAlgorithmException {
        //long unixSeconds = new Date().toInstant().getEpochSecond();
        String tokenParam = unixSeconds + "," + version;
        String token=md5Hex(String.format("%d%s",unixSeconds,tk));
        return new Sign(token,tokenParam);
    }
    private static String md5Hex(String s) throws NoSuchAlgorithmException {
        MessageDigest digest= MessageDigest.getInstance("MD5");
        return   Hex.toHexString(digest.digest(s.getBytes(StandardCharsets.UTF_8)));
    }
}
