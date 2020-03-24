package com.fn.common.dispatcher.bean;

import com.google.protobuf.Message;

/**
 * 请求消息
 *
 * @author fomin
 * @date 2019-12-07
 */
public interface MessageRequest {
    /**
     * 消息类型
     */
    int getMessageType();

    /**
     * 设置消息类型
     */
    void setMessageType(int messageType);

    /**
     * 从请求中解析参数
     *
     * @param indices 需要解析的参数在 handler 方法中的索引。由于参数推断机制的存在，并不是所有 handler 方法的参数都要在此解析，参见 {@link ArgumentData}
     * @param data    handler 方法的元数据
     * @return 解析出来的参数值，与传入的 indices 数组一一对应
     * @see ArgumentData
     */
    Object[] parseArguments(int[] indices, MessageData data);

    /**
     * 设置请求的参数
     *
     * @param args    设置请求的参数
     * @param indices 要设置的参数在 handler 方法中的索引，与 args 数组一一对应
     * @param data    handler 方法的元数据
     */
    void setArguments(Object[] args, int[] indices, MessageData data);


    /**
     * 设置请求的参数
     *
     * @param message    设置请求的参数
     */
    void setArgument(Message message);

    /**
     * 创建该请求对应的响应对象
     */
    MessageResponse createResponse();

    /**
     * 序列化该请求，获得字节数组
     */
    byte[] toByteArray();

    /**
     * 消息体的 Mime-Type 类型，默认为 text/plain
     */
    default String getContentType() {
        return "text/plain; charset=utf-8";
    }
}
