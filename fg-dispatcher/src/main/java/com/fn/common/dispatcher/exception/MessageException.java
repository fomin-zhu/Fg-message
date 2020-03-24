package com.fn.common.dispatcher.exception;

import com.google.protobuf.Message;
import kotlin.text.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fomin
 * @date 2019-12-08
 */
public class MessageException extends RuntimeException {
    private static Map<Integer, String> allMessages = new HashMap<>();
    private int code;
    private String msg;
    private Message data;

    public MessageException(int code) {
        this(code, "", null);
    }

    public MessageException(int code, String msg, Message data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    static {
        loadMessages();
    }

    private static void loadMessages() {
        allMessages = loadMessages("RequestCode.proto");
    }

    protected static Map<Integer, String> loadMessages(String file) {
        InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(file);
        if (in == null) return Collections.emptyMap();
        Map<Integer, String> messages = new HashMap<>();
        InputStreamReader reader = new InputStreamReader(in, Charsets.UTF_8);
        BufferedReader bf = new BufferedReader(reader);
        String reg = "^\\s*.+\\s*=\\s*(\\d+)\\s*;\\s*//\\s*(.+)\\s*$";
        Pattern p = Pattern.compile(reg);
        bf.lines().forEach(it -> {
            if (StringUtils.isNotEmpty(it)) {
                Matcher matcher = p.matcher(it);
                if (matcher.find()) {
                    messages.put(NumberUtils.toInt(matcher.group(1)), matcher.group(2));
                }
            }
        });
        return messages;
    }

    public int getCode() {
        return code;
    }

    public Message getData() {
        return data;
    }

    public String getMessage() {
        return StringUtils.isNotEmpty(msg) ? msg : allMessages.getOrDefault(code, "系统错误码：" + code);
    }
}
