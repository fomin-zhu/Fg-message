package com.fn.common.dispatcher.exception;

import com.fn.common.dispatcher.bean.MessageData;

/**
 *  消息分发器不支持指定的消息时，产生此异常.
 * @author fomin
 * @date 2019-12-07
 */
public final class UnsupportedMessageException extends UnsupportedOperationException {
    private final int messageType;

    public UnsupportedMessageException(MessageData data) {
        super("Unsupported message: " + data);
        this.messageType = data.getMessageType();
    }
}
