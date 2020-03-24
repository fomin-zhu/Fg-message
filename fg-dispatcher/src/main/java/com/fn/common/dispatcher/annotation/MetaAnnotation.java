package com.fn.common.dispatcher.annotation;

import com.fn.common.dispatcher.resolver.MessageResolver;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 元注解（meta annotation），用来处理MessageMapping
 * @author fomin
 * @date 2019-12-07
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MetaAnnotation {
    /**
     * 用于解析目标注解的解析器
     */
    Class<? extends MessageResolver> value();
}
