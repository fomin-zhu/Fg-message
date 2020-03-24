package com.fn.common.code;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * @author fomin
 * @date 2019-11-19
 */
public class MD5 {
    //十六进制下数字到字符的映射数组
    private static final String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    /**
     * 加密inputString
     */
    public static String generate(String inputString) {
        return md5(inputString);
    }

    /**
     * 验证输入的密码是否正确
     *
     * @param password    加密后的密码
     * @param inputString 输入的字符串
     * @return 验证结果，TRUE:正确 FALSE:错误
     */
    public static boolean validate(String password, String inputString) {
        return password.equalsIgnoreCase(md5(inputString));
    }

    /**
     * 对字符串进行MD5加密
     */
    private static String md5(String originString) {
        try {
            //创建具有指定算法名称的信息摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            //使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
            byte[] results = md.digest(originString.getBytes(Charsets.UTF_8));
            //将得到的字节数组变成字符串返回
            String resultString = byteArrayToHexString(results);
            return resultString.toUpperCase(Locale.ENGLISH);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 转换字节数组为十六进制字符串
     */
    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder resultSb = new StringBuilder();
        for (byte b : bytes) {
            resultSb.append(byteToHexString(b));
        }
        return resultSb.toString();
    }

    /**
     * 将一个字节转化成十六进制形式的字符串
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }
}
