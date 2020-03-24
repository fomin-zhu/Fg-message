package com.fn.common.dispatcher.config;

import com.fn.common.dispatcher.bean.MessageFactory;
import com.fn.common.dispatcher.bean.MessageFactoryImp;
import com.fn.common.dispatcher.core.MessageDispatcher;
import com.fn.common.dispatcher.util.ObjectStringifier;
import com.fn.common.dispatcher.util.ProtoUtils;
import com.google.protobuf.Message;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MessageDispatcher自动配置
 *
 * @author fomin
 * @date 2019-12-07
 */
@Configuration
@ConditionalOnClass(MessageDispatcher.class)
public class MessageDispatcherConfiguration {

    @Bean
    @ConditionalOnMissingBean(MessageDispatcher.class)
    public MessageDispatcher messageDispatcher() {
        MessageDispatcher dispatcher = new MessageDispatcher();
        dispatcher.setObjectStringifier(objectStringifier());
        dispatcher.setPath("/api");
        dispatcher.setMessageFactory(messageFactory());
        dispatcher.setGzipEnabled(true);
        dispatcher.setGzipThresholdLength(150);
        return dispatcher;
    }

    @Bean
    @ConditionalOnMissingBean(ObjectStringifier.class)
    public ObjectStringifier objectStringifier() {
        return obj -> {
            if (obj instanceof Message) {
                return ProtoUtils.toString((Message) obj);
            } else {
                return String.valueOf(obj);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(MessageFactory.class)
    public MessageFactory messageFactory() {
        return new MessageFactoryImp();
    }
}
