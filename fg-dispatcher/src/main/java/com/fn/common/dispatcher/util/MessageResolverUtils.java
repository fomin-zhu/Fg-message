package com.fn.common.dispatcher.util;

import com.fn.common.dispatcher.annotation.MetaAnnotation;
import com.fn.common.dispatcher.resolver.MessageResolver;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author fomin
 * @date 2019-12-07
 */
public final class MessageResolverUtils {
    private static final Map<Class<? extends MessageResolver>, MessageResolver> mappingResolverCache;

    static {
        mappingResolverCache = Collections.synchronizedMap(new WeakHashMap<>());
    }

    /**
     * 获得 MetaAnnotation 上的 MessageResolver 实例
     */
    public static MessageResolver<?> getMessageResolver(MetaAnnotation mapping) {
        Class<? extends MessageResolver> resolverClass = mapping.value();

        return mappingResolverCache.computeIfAbsent(resolverClass, key -> {
            try {
                Constructor<? extends MessageResolver> constructor = resolverClass.getDeclaredConstructor();
                ReflectionUtils.makeAccessible(constructor);
                return constructor.newInstance();
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
