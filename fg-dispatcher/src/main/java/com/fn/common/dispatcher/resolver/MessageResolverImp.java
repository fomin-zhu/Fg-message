package com.fn.common.dispatcher.resolver;

import com.fn.common.dispatcher.annotation.MessageMapping;

/**
 * @author fomin
 * @date 2019-12-07
 */
public final class MessageResolverImp implements MessageResolver<MessageMapping> {

    @Override
    public Class<MessageMapping> getAnnotationClass() {
        return MessageMapping.class;
    }

    @Override
    public int getMessageType(MessageMapping mapping) {
        return mapping.messageType();
    }
}
