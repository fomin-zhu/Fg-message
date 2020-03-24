package com.fn.common.dispatcher.chain;

import com.fn.common.dispatcher.bean.MessageData;
import com.fn.common.dispatcher.bean.MessageFactory;
import com.fn.common.dispatcher.bean.MessageRequest;

/**
 * 拦截器上下文
 * @author fomin
 * @date 2019-12-07
 */
public interface InterceptorContext {
    /**
     * 当前消息请求
     */
    <T extends MessageRequest> T request();

    /**
     * 当前请求的消息处理器的元数据
     */
    MessageData handlerMessageData();

    /**
     * MessageFactory，可使用它创建请求或响应对象
     */
    MessageFactory messageFactory();
}
