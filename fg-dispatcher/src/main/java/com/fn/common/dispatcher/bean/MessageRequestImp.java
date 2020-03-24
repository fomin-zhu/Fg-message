package com.fn.common.dispatcher.bean;

import com.fn.common.code.util.LoginUtils;
import com.fn.common.dispatcher.util.GzipUtils;
import com.fn.common.dispatcher.util.ProtoUtils;
import com.fn.common.proto.http.PBBytesList;
import com.fn.common.proto.http.PBCode;
import com.fn.common.proto.http.PBRequest;
import com.fn.common.proto.http.PBResponse;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author fomin
 * @date 2019-12-07
 */
@Slf4j
public class MessageRequestImp implements MessageRequest {
    private PBRequest.Builder rawRequest;

    public MessageRequestImp() {
        this(0, null);
    }

    public MessageRequestImp(PBRequest rawRequest) {
        this.rawRequest = rawRequest.toBuilder();
    }

    public MessageRequestImp(InputStream input) {
        try {
            this.rawRequest = PBRequest.parseFrom(input).toBuilder();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MessageRequestImp(int messageType, Message message) {
        this(messageType, message == null ? new byte[0] : message.toByteArray(), false);
    }

    public MessageRequestImp(int messageType, byte[] data, boolean compress) {
        this.rawRequest = PBRequest.newBuilder();
        this.rawRequest.setCompressed(compress);
        this.rawRequest.setType(messageType);
        this.rawRequest.setRequestId(System.currentTimeMillis());
        this.rawRequest.setMessageData(ByteString.copyFrom(GzipUtils.encodeData(data == null ? new byte[0] : data, rawRequest.getCompressed())));
    }

    @Override
    public int getMessageType() {
        return rawRequest.getType();
    }

    @Override
    public void setMessageType(int messageType) {
        rawRequest.setType(messageType);
    }

    @Override
    public Object[] parseArguments(int[] indices, MessageData data) {
        if (indices == null || indices.length == 0) return new Object[0];

        byte[] bytes = GzipUtils.decodeData(rawRequest.getMessageData().toByteArray(), rawRequest.getCompressed());
        Object[] result = new Object[indices.length];
        if (indices.length == 1) {
            // 只有一个参数时，直接解析数据
            result[0] = ProtoUtils.deserialize(bytes, data.getParameter(indices[0]));
        } else {
            // 有多个参数时，把数据解析为 PBBytesList，逐个解析（支持多参数的 rpc 调用，仅用于服务器内部）
            List<ByteString> list = ProtoUtils.parseFrom(bytes, PBBytesList.class).orElse(PBBytesList.getDefaultInstance()).getDataList();
            for (int i = 0; i < indices.length; i++) {
                result[i] = ProtoUtils.deserialize(list.get(i).toByteArray(), data.getParameter(indices[i]));
            }
        }

        return result;
    }

    @Override
    public void setArguments(Object[] args, int[] indices, MessageData data) {
        if (args == null || args.length == 0) return;

        byte[] bytes;
        if (args.length == 1) {
            bytes = ProtoUtils.serialize(args[0]);
        } else {
            PBBytesList.Builder list = PBBytesList.newBuilder();
            for (Object arg : args) {
                list.addData(ByteString.copyFrom(ProtoUtils.serialize(arg)));
            }
            bytes = list.build().toByteArray();
        }

        rawRequest.setMessageData(ByteString.copyFrom(GzipUtils.encodeData(bytes, rawRequest.getCompressed())));
    }

    /**
     * 设置 API 请求参数
     */
    @Override
    public void setArgument(Message message) {
        if (message != null) {
            byte[] data = message.toByteArray();
            rawRequest.setMessageData(ByteString.copyFrom(GzipUtils.encodeData(data, rawRequest.getCompressed())));
        }
    }

    @Override
    public MessageResponse createResponse() {
        PBResponse.Builder response = PBResponse.newBuilder()
                .setCompressed(rawRequest.getCompressed())
                .setType(rawRequest.getType())
                .setRequestId(rawRequest.getRequestId())
                .setResultCode(PBCode.SUCCESS_VALUE)
                .setResultInfo("成功");
        return new MessageResponseImp(response);
    }

    public String getAdminAccessToken() {
        return rawRequest.getAdminAccessToken();
    }

    public int getAdminId() {
        if (StringUtils.isEmpty(rawRequest.getAdminAccessToken())) return 0;
        return LoginUtils.parseAdminId(rawRequest.getAdminAccessToken());
    }

    public String getUserId() {
        if (StringUtils.isEmpty(rawRequest.getAccessToken())) return "0";
        return LoginUtils.parseUserId(rawRequest.getAccessToken());
    }

    public void setAdminAccessToken(String token) {
        rawRequest.setAdminAccessToken(token);
    }


    public String getAccessToken() {
        return rawRequest.getAccessToken();
    }

    public void setAccessToken(String token) {
        rawRequest.setAccessToken(token);
    }


    @Override
    public byte[] toByteArray() {
        return rawRequest.build().toByteArray();
    }

    @Override
    public String toString() {
        return ProtoUtils.toString(rawRequest.build());
    }
}
