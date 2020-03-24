package com.fn.common.dispatcher.bean;

import javax.servlet.http.HttpServletRequest;

/**
 * 消息工厂
 * @author fomin
 * @date 2019-12-07
 */
public interface MessageFactory {

    /**
     * 创建一个空的消息请求
     */
    MessageRequest createMessageRequest();

    /**
     * 从客户端传来的二进制数据中解析消息请求对象
     */
    MessageRequest parseMessageRequest(HttpServletRequest httpRequest);

    /**
     * 从服务器返回的二进制数据中解析消息响应对象
     */
    MessageResponse parseMessageResponse(okhttp3.Response httpResponse);
}
