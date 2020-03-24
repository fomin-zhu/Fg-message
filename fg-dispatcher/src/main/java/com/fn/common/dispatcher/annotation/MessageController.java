package com.fn.common.dispatcher.annotation;

import com.fn.common.dispatcher.core.MessageDispatcher;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * controller注解类，用来spring扫描生成handler消息处理
 * 跟 MessageMapping 一起使用
 *
 * @see MessageMapping
 * @see MessageDispatcher
 * @author fomin
 * @date 2019-12-07
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Component
public @interface MessageController {
}
