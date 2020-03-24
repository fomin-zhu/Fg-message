package com.fn.common.dispatcher.bean;

import okhttp3.Response;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * 消息工厂实现
 *
 * @author fomin
 * @date 2019-12-07
 */
public class MessageFactoryImp implements MessageFactory {

//    private MimeType jsonMimeType = MimeType.valueOf("application/json");

    @Override
    public MessageRequest createMessageRequest() {
        return new MessageRequestImp();
    }

    @Override
    public MessageRequest parseMessageRequest(HttpServletRequest httpRequest) {
        try {
            return new MessageRequestImp(httpRequest.getInputStream());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public MessageResponse parseMessageResponse(Response httpResponse) {
        return new MessageResponseImp(httpResponse.body().byteStream());
    }
}
