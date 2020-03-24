package com.fn.common.dispatcher.core;

import com.fn.common.dispatcher.bean.MessageRequest;

/**
 * MessageDispatcher 的上下文对象，可在消息分发期间获得当前正在处理的请求的具体信息
 * @author fomin
 * @date 2019-12-07
 */
public final class MessageDispatcherContext {
    private static final ThreadLocal<MessageDispatcherContext> threadLocal = new ThreadLocal<>();
    private final MessageRequest currentRequest;
    private final MessageHandler handler;

    private MessageDispatcherContext(MessageRequest request, MessageHandler handler) {
        this.currentRequest = request;
        this.handler = handler;
    }

    public static MessageDispatcherContext get() {
        return threadLocal.get();
    }

    @SuppressWarnings("unchecked")
    public <T extends MessageRequest> T getCurrentRequest() {
        return (T) currentRequest;
    }

    public MessageHandler getHandler() {
        return handler;
    }

    public static void set(MessageRequest request, MessageHandler handler) {
        threadLocal.set(new MessageDispatcherContext(request, handler));
    }

    public static void clear() {
        threadLocal.remove();
    }
}
