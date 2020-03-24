package com.fn.common.dispatcher.core;

import com.fn.common.dispatcher.bean.MessageData;
import com.fn.common.dispatcher.util.ObjectStringifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 消息处理器
 * @author fomin
 * @date 2019-12-07
 */
public final class MessageHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final MessageData data;
    private final Method method;
    private final Object bean;
    private final ObjectStringifier stringifier;

    MessageHandler(MessageData data, Object bean, ObjectStringifier stringifier) {
        this.data = data;
        this.method = AopUtils.getMostSpecificMethod(data.getMethod(), bean.getClass());
        this.bean = bean;
        this.stringifier = stringifier;
    }

    /**
     * 调用此处理器
     * @param args 调用参数
     * @return 调用结果
     * @throws InvocationTargetException 如果调用过程中发生异常，将发生的异常包装成此异常抛出
     * @throws IllegalAccessException 尝试通过反射调用底层的方法时，如果没有访问权限，将抛出此异常
     */
    public Object invoke(Object[] args) throws InvocationTargetException, IllegalAccessException {
        log.info("[API] Invoke handler: {}", this);
        log.info("[API] Arguments: {}", stringifier.toString(args));

        Object result = method.invoke(bean, args);

        log.debug("[API] Result: {}", stringifier.toString(result));
        return result;
    }

    public MessageData getData() {
        return data;
    }

    public Method getMethod() {
        return method;
    }

    public Object getBean() {
        return bean;
    }

    public ObjectStringifier getStringifier() {
        return stringifier;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
