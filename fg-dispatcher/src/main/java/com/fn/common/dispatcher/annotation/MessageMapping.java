package com.fn.common.dispatcher.annotation;

import com.fn.common.dispatcher.resolver.MessageResolverImp;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 消息处理器，支持处理指定的消息，需要注解到public方法
 *
 * @author fomin
 * @date 2019-12-07
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@MetaAnnotation(MessageResolverImp.class)
public @interface MessageMapping {
    /**
     * 消息类型
     */
    int messageType();
}
