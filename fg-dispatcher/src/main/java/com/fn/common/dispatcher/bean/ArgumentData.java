package com.fn.common.dispatcher.bean;

import com.fn.common.dispatcher.core.MessageDispatcher;

/**
 * 参数数据
 *
 * @author fomin
 * @date 2019-12-07
 */
public interface ArgumentData {
    /**
     * 在调用 MessageHandler 之前，手动为传入其中的每一个参数赋值
     *
     * @param index 当前推断的参数索引
     * @param data 消息处理器的元数据
     * @return 返回推断的参数值。如果无法推断，返回 null，此时将由框架自行推断，
     *         最终还是无法推断的，交由 {@link MessageRequest#parseArguments(int[], MessageData)} 方法解析
     * @see MessageDispatcher#dispatchMessage(MessageRequest, ArgumentData)
     * @see MessageRequest#parseArguments(int[], MessageData)
     */
    Object argument(int index, MessageData data);
}
