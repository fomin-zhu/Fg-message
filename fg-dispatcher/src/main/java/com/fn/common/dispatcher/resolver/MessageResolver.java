package com.fn.common.dispatcher.resolver;

import java.lang.annotation.Annotation;

/**
 * 消息解析器
 * @author fomin
 * @date 2019-12-07
 */
public interface MessageResolver<T extends Annotation> {
    /**
     * 该解析器所处理的注解的类型
     */
    Class<T> getAnnotationClass();

    /**
     * 从 MessageMapping 注解中解析出消息类型
     */
    int getMessageType(T mapping);
}
