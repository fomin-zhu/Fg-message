package com.fn.common.dispatcher.core;

import com.fn.common.dispatcher.annotation.MessageController;
import com.fn.common.dispatcher.annotation.MetaAnnotation;
import com.fn.common.dispatcher.bean.ArgumentData;
import com.fn.common.dispatcher.bean.MessageData;
import com.fn.common.dispatcher.bean.MessageFactory;
import com.fn.common.dispatcher.bean.MessageRequest;
import com.fn.common.dispatcher.bean.MessageResponse;
import com.fn.common.dispatcher.exception.UnsupportedMessageException;
import com.fn.common.dispatcher.chain.InterceptorChain;
import com.fn.common.dispatcher.chain.InterceptorChainImp;
import com.fn.common.dispatcher.resolver.MessageResolver;
import com.fn.common.dispatcher.util.GzipUtils;
import com.fn.common.dispatcher.util.MessageResolverUtils;
import com.fn.common.dispatcher.util.ObjectStringifier;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 消息调度，扫描初始化MessageController注解类
 *
 * @author fomin
 * @date 2019-12-07
 */
public class MessageDispatcher implements ApplicationListener<ContextRefreshedEvent> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ObjectStringifier objectStringifier = String::valueOf;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final Map<MessageData, MessageHandler> handlers = new ConcurrentHashMap<>();

    /**
     * API 请求的 web 路径
     */
    @Setter
    private String path = "/";

    /**
     * 用于创建消息请求/响应对象的工厂
     */
    @Setter
    private MessageFactory messageFactory;

    /**
     * 是否启用 gzip 压缩
     */
    @Setter
    private boolean gzipEnabled = true;

    /**
     * 需要进行 gzip 压缩的最小字节数（防止 gzip 之后反而会变大）
     */
    @Setter
    private int gzipThresholdLength = 200;

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        if (initialized.compareAndSet(false, true)) {
            init(event.getApplicationContext());
            registerMapping(event.getApplicationContext());
        }
    }

    /**
     * 初始化 MessageDispatcher, 从 ApplicationContext 中扫描消息处理器并注册
     */
    private void init(ApplicationContext context) {
        for (Object bean : context.getBeansWithAnnotation(MessageController.class).values()) {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);

            for (Method method : targetClass.getMethods()) {
                MessageData data = resolveMessageData(method);
                if (data == null) continue;

                if (handlers.containsKey(data)) {
                    throw new IllegalStateException("Duplicate handler data: " + data);
                } else {
                    handlers.put(data, new MessageHandler(data, bean, objectStringifier));
                    log.debug("Registered handler: {}", data);
                }
            }
        }
    }

    /**
     * 解析方法对应的 handler 元数据，如果没有找到 MetaAnnotation 注解，返回 null
     */
    @SuppressWarnings("unchecked")
    private MessageData resolveMessageData(Method method) {
        MetaAnnotation meta = AnnotationUtils.findAnnotation(method, MetaAnnotation.class);
        if (meta == null) {
            return null;
        } else {
            MessageResolver resolver = MessageResolverUtils.getMessageResolver(meta);
            Class<? extends Annotation> annotationClass = resolver.getAnnotationClass();
            Annotation annotation = AnnotationUtils.findAnnotation(method, annotationClass);

            int type = resolver.getMessageType(annotation);

            return new MessageData(type, method);
        }
    }

    /**
     * 注册方法映射
     */
    private void registerMapping(ApplicationContext context) {
        try {

            RequestMappingInfo mapping = RequestMappingInfo.paths(path).build();
            Method method = this.getClass().getMethod("handleApi", HttpServletRequest.class, HttpServletResponse.class);

            RequestMappingHandlerMapping handlerMapping = context.getBean(RequestMappingHandlerMapping.class);
            handlerMapping.registerMapping(mapping, this, method);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void handleApi(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            MessageRequest request = messageFactory.parseMessageRequest(httpRequest);
            log.info("[API] Request: {}", request);

            ArgumentData argumentData = createArgumentData(httpRequest, httpResponse);
            MessageResponse response = dispatchMessage(request, argumentData);
            log.info("[API] Response: {}", response);

            byte[] rawData = response.toByteArray();
            byte[] sentData = gzip(httpRequest, httpResponse, rawData);
            log.info("[API] Total " + sentData.length + " bytes sent, raw " + rawData.length + " bytes.");

            httpResponse.setContentLength(sentData.length);
            httpResponse.setContentType(response.getContentType());
            httpResponse.getOutputStream().write(sentData);
            httpResponse.getOutputStream().flush();

        } catch (Throwable e) {
            unexpectedException(httpResponse, e);
        }
    }

    /**
     * 将请求分发到对应的消息处理器，返回处理完后的响应
     * <p>
     * 异常规约：此方法永远不会抛出异常，消息分发过程中产生的所有异常都会通过 {@link MessageResponse#setError} 告知调用者
     *
     * @see MessageResponse#setError(Throwable)
     * @see UnsupportedMessageException
     */
    public MessageResponse dispatchMessage(MessageRequest request, ArgumentData argumentData) {
        try {
            MessageHandler handler = resolveHandler(new MessageData(request.getMessageType()));
            InterceptorChain chain = new InterceptorChainImp(handler, argumentData);
            return chain.proceed(request);

        } catch (Throwable e) {
            MessageResponse response = request.createResponse();
            response.setError(e instanceof InvocationTargetException ? e.getCause() : e);
            return response;
        }
    }

    /**
     * 映射参数
     */
    private ArgumentData createArgumentData(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        return (index, metadata) -> {
            Parameter param = metadata.getParameter(index);
            if (ServletRequest.class.isAssignableFrom(param.getType())) {
                return httpRequest;
            } else if (ServletResponse.class.isAssignableFrom(param.getType())) {
                return httpResponse;
            } else {
                return null;
            }
        };
    }

    /**
     * 数据压缩
     */
    private byte[] gzip(HttpServletRequest httpRequest, HttpServletResponse httpResponse, byte[] rawData) {
        byte[] sentData = rawData;

        if (gzipEnabled && rawData.length > gzipThresholdLength) {
            String acceptEncoding = httpRequest.getHeader("Accept-Encoding");
            boolean acceptGzip = acceptEncoding != null && acceptEncoding.contains("gzip");

            if (acceptGzip) {
                sentData = GzipUtils.compress(rawData);
                httpResponse.setHeader("Content-Encoding", "gzip");
            }
        }

        return sentData;
    }

    public void setObjectStringifier(ObjectStringifier objectStringifier) {
        this.objectStringifier = objectStringifier;
    }

    public ObjectStringifier getObjectStringifier() {
        return objectStringifier;
    }

    /**
     * 不支持消息
     */
    private void unexpectedException(HttpServletResponse httpResponse, Throwable e) {
        if (e instanceof IOException && e.getClass().getSimpleName().equals("ClientAbortException")) {
            // 不同的 web 容器都有自己的 ClientAbortException，因此此处仅根据类名判断
            log.warn(e.toString());

        } else {
            try {
                log.error("Error occur: ", e);
                httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * 获得处理指定请求的 MessageHandler 对象
     */
    private MessageHandler resolveHandler(MessageData data) {
        MessageHandler handler = handlers.get(data);
        if (handler == null) {
            throw new UnsupportedMessageException(data);
        } else {
            return handler;
        }
    }
}
