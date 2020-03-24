package com.fn.common.code.util;

import com.fn.common.code.MD5;

import java.time.Instant;
import java.util.Locale;

/**
 * @author fomin
 * @date 2020-02-23
 */
public final class PasswordUtils {
    /**
     * 用户的密码进行加密
     *
     * @param password 原密码
     * @param salt 混入盐值，最好能保证每个用户使用不同的盐
     */
    public static String encrypt(String password, String salt) {
        return MD5.generate(salt + password).toUpperCase(Locale.ENGLISH);
    }

    /**
     * 使用用户的注册时间作为盐来加密密码，注册时间基本可以保证每个用户的盐值不同
     */
    public static String encrypt(String password, Instant registerDate) {
        // MySQL 的 timestamp 有可能无法保存毫秒值，因此手动截断到秒，防止两次加密的结果不一致
        long timestamp = registerDate.toEpochMilli() / 1000 * 1000;
        return encrypt(password, String.valueOf(timestamp));
    }
}
