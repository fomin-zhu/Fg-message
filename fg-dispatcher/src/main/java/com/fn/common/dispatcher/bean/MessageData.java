package com.fn.common.dispatcher.bean;

import lombok.Getter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;

/**
 * 消息数据
 *
 * @author fomin
 * @date 2019-12-07
 */
@Getter
public final class MessageData {
    private final int messageType;
    private final Method method;
    private final Parameter[] parameters;

    public MessageData(int messageType) {
        this(messageType, null);
    }

    public MessageData(int messageType, Method method) {
        this.messageType = messageType;
        this.method = method;
        this.parameters = method == null ? null : method.getParameters();
    }

    /**
     * 参数数量
     */
    public int getParameterCount() {
        return parameters == null ? 0 : parameters.length;
    }

    /**
     * 获取参数信息
     */
    public Parameter getParameter(int i) {
        if (parameters == null) {
            throw new IndexOutOfBoundsException("index: " + i);
        }
        return parameters[i];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageData that = (MessageData) o;
        return messageType == that.messageType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (method != null) {
            sb.append(method.getDeclaringClass().getSimpleName());
            sb.append(".");
            sb.append(method.getName());
        }

        return sb.append("[type=").append(messageType).append("]").toString();
    }
}
