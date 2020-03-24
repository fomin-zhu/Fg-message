package com.fn.common.dispatcher.bean;

import com.fn.common.dispatcher.exception.MessageException;
import com.fn.common.dispatcher.exception.UnsupportedMessageException;
import com.fn.common.dispatcher.util.GzipUtils;
import com.fn.common.dispatcher.util.ProtoUtils;
import com.fn.common.proto.http.PBCode;
import com.fn.common.proto.http.PBResponse;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author fomin
 * @date 2019-12-08
 */
@Slf4j
public class MessageResponseImp implements MessageResponse {

    private PBResponse.Builder rawResponse;

    public MessageResponseImp() {
        this.rawResponse = PBResponse.newBuilder();
    }

    public MessageResponseImp(PBResponse.Builder rawResponse) {
        this.rawResponse = rawResponse;
    }

    public MessageResponseImp(PBResponse rawResponse) {
        this.rawResponse = rawResponse.toBuilder();
    }

    public MessageResponseImp(InputStream input) {
        try {
            this.rawResponse = PBResponse.parseFrom(input).toBuilder();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setError(Throwable error) {
        MessageException e = translateException(error);

        rawResponse.setResultCode(e.getCode());
        rawResponse.setResultInfo(e.getMessage());

        Message responseBody = e.getData();
        if (responseBody != null) {
            byte[] bytes = responseBody.toByteArray();
            rawResponse.setMessageData(ByteString.copyFrom(GzipUtils.encodeData(bytes, rawResponse.getCompressed())));
        }
    }

    @Override
    public Throwable getError() {
        if (rawResponse.getResultCode() == 0) {
            return null;
        } else {
            return new MessageException(rawResponse.getResultCode(), rawResponse.getResultInfo(), null);
        }
    }

    @Override
    public void setResult(Object result, MessageData data) {
        if (result == null) {
            // 禁止在提供给 App 客户端的 API 中返回 null 值
            throw new NullPointerException("Null value is not a legal result for controller methods, method: " + data);
        }
        byte[] bytes = ProtoUtils.serialize(result);
        rawResponse.setMessageData(ByteString.copyFrom(GzipUtils.encodeData(bytes, rawResponse.getCompressed())));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T parseResult(MessageData data) {
        byte[] bytes = GzipUtils.decodeData(rawResponse.getMessageData().toByteArray(), rawResponse.getCompressed());
        return (T) ProtoUtils.deserialize(bytes, data.getMethod());
    }


    @Override
    public <T> T parseResult(Class<T> resultClass) {
        byte[] data = GzipUtils.decodeData(rawResponse.getMessageData().toByteArray(), rawResponse.getCompressed());
        return ProtoUtils.parseFrom(data, resultClass).orElse(null);
    }

    @Override
    public byte[] toByteArray() {
        return rawResponse.build().toByteArray();
    }

    /**
     * 转换 API 处理过程中抛出的异常，返回 MessageException 给客户端
     */
    private MessageException translateException(Throwable e) {
        if (e instanceof UnsupportedMessageException) {
            return new MessageException(PBCode.API_UNSUPPORTED_VALUE);
        } else {
            MessageException cause = getCause(e, MessageException.class);
            if (cause != null) {
                return cause;
            } else {
                log.error("Error: ", e);
                return new MessageException(PBCode.SERVER_ERROR_VALUE);
            }
        }
    }

    /**
     * 一个 Throwable 对象的 cause 链中递归查找指定类型的异常
     */
    private <X extends Throwable> X getCause(Throwable e, Class<X> clazz) {
        Throwable curr = e;
        while (true) {
            if (curr == null) {
                return null;
            } else if (clazz.isInstance(curr)) {
                return clazz.cast(curr);
            } else {
                curr = curr.getCause();
            }
        }
    }

    @Override
    public String toString() {
        return ProtoUtils.toString(rawResponse.build());
    }
}
