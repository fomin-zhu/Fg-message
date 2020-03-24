package com.fn.common.code.util;

import com.fn.common.code.Base64;
import com.fn.common.code.Charsets;
import com.fn.common.code.XxTea;

import java.util.UUID;

/**
 * @author fomin
 * @date 2020-02-23
 */
public class LoginUtils {
    private static final String ACCESS_TOKEN_ENCRYPT_KEY = "ACCESS_TOKEN_ENCRYPT_KEY";
    private static final String ADMIN_ACCESS_TOKEN_ENCRYPT_KEY = "ADMIN_ACCESS_TOKEN_ENCRYPT_KEY";

    /**
     * 每次登陆生成唯一的 access token，并且里面包含 userId
     */
    public static String generateAccessToken(String userId) {
        String token = UUID.randomUUID() + ":" + userId;
        byte[] data = token.getBytes(Charsets.UTF_8);
        data = XxTea.encrypt(data, ACCESS_TOKEN_ENCRYPT_KEY.getBytes(Charsets.UTF_8));
        return Base64.encode(data);
    }

    /**
     * 从 access token 中解析出 userId
     */
    public static String parseUserId(String accessToken) {
        byte[] data = Base64.decode(accessToken);
        data = XxTea.decrypt(data, ACCESS_TOKEN_ENCRYPT_KEY.getBytes(Charsets.UTF_8));
        String token = new String(data, Charsets.UTF_8);
        return token.substring(token.indexOf(":") + 1);
    }

    /**
     * 每次登陆生成唯一的 access token，并且里面包含 adminId
     */
    public static String generateAdminAccessToken(int adminId) {
        String token = UUID.randomUUID() + ":" + adminId;
        byte[] data = token.getBytes(Charsets.UTF_8);
        data = XxTea.encrypt(data, ADMIN_ACCESS_TOKEN_ENCRYPT_KEY.getBytes(Charsets.UTF_8));
        return Base64.encode(data);
    }

    /**
     * 从 access token 中解析出 adminId
     */
    public static int parseAdminId(String accessToken) {
        byte[] data = Base64.decode(accessToken);
        data = XxTea.decrypt(data, ADMIN_ACCESS_TOKEN_ENCRYPT_KEY.getBytes(Charsets.UTF_8));
        String token = new String(data, Charsets.UTF_8);
        return Integer.parseInt(token.substring(token.indexOf(":") + 1));
    }
}
