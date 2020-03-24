package com.fn.common.dispatcher.bean;

/**
 * 消息返回体
 * @author fomin
 * @date 2019-12-07
 */
public interface MessageResponse {
    /**
     * 设置并记录请求处理过程中发生的异常
     */
    void setError(Throwable error);

    /**
     * 获取请求处理过程中发生的异常，如果没有异常，返回 null
     */
    Throwable getError();

    /**
     * 设置响应的消息体
     */
    void setResult(Object result, MessageData data);

    /**
     * 从响应中解析出结果
     */
    <T> T parseResult(MessageData data);

    /**
     * 从响应中解析出结果
     */
    <T> T parseResult(Class<T> resultClass);

    /**
     * 序列化该响应对象，获得字节数组
     */
    byte[] toByteArray();

    /**
     * 消息体的 Mime-Type 类型，默认为 text/plain
     */
    default String getContentType() {
        return "text/plain; charset=utf-8";
    }
}
