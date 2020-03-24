package com.fn.common.dispatcher.util;

import com.fn.common.dispatcher.bean.MessageFactory;
import com.fn.common.dispatcher.bean.MessageFactoryImp;
import com.fn.common.dispatcher.bean.MessageRequest;
import com.fn.common.dispatcher.bean.MessageRequestImp;
import com.fn.common.dispatcher.bean.MessageResponse;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author fomin
 * @date 2019-12-08
 */
public class ApiUtils {

    private static final Logger log = LoggerFactory.getLogger(ApiUtils.class);
    private static final MessageFactory factory = new MessageFactoryImp();
    private static OkHttpClient client;

    static {
        client = new OkHttpClient()
                .newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    public static <T extends Message> T callApi(String url, ProtocolMessageEnum messageType, Message argument, Class<T> clz) {
        return callApi(url, messageType.getNumber(), argument, clz);
    }

    private static <T extends Message> T callApi(String url, int messageType, Message argument, Class<T> clz) {
        MessageRequestImp request = new MessageRequestImp();
        request.setMessageType(messageType);
        request.setArgument(argument);
        request.setAdminAccessToken("waVQ7Kk0ym++OfiYaeRXsuHwyvq2xB0EWqDQ3FMd+cNv3ie5PwhltPGXVes=");
        MessageResponse response = request(url, request);
        if (response == null || response.getError() != null) {
            return null;
        }
        T result = response.parseResult(clz);
        log.info("Result: {}", ProtoUtils.toString(result));
        return result;
    }

    private static MessageResponse request(String url, MessageRequest messageRequest) {
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(messageRequest.toByteArray(), MediaType.parse(messageRequest.getContentType())))
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                MessageResponse rsp = factory.parseMessageResponse(response);
                log.info("request return:{}", rsp);
                return rsp;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
