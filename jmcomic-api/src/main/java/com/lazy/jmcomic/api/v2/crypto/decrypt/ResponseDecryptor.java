package com.lazy.jmcomic.api.v2.crypto.decrypt;

import com.alibaba.fastjson2.JSONObject;
import org.apache.hc.client5.http.utils.Base64;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 响应体解密
 * @since 2026.3.15
 * @author lazy
 */
public final class ResponseDecryptor {
    private ResponseDecryptor() {}
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    public static String decryptData(String token,long unixSeconds,String data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        JSONObject jsonObject=JSONObject.parseObject(data);
        SecretKeySpec secretKey = new SecretKeySpec(
                md5Hex((unixSeconds+token)).getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64.decodeBase64(jsonObject.getString("data"))), StandardCharsets.UTF_8);
    }
    private static String md5Hex(String s) throws NoSuchAlgorithmException {
        MessageDigest digest= MessageDigest.getInstance("MD5");
        return Hex.toHexString(digest.digest(s.getBytes(StandardCharsets.UTF_8)));
    }
}
