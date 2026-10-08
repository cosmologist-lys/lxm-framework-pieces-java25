package com.lxm.framework.redis.impl.bus;

import com.lxm.framework.redis.inf.BusSubscriberSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

/**
 * @Author: Lys
 * @Date 2023/5/12
 * @Describe
 **/
@Slf4j
public class BusSub implements BusSubscriberSession.BusSubInf {
    private final java.util.concurrent.atomic.AtomicBoolean subscribed =
            new java.util.concurrent.atomic.AtomicBoolean();

    private final RedisTemplate<String, Object> session;
    private final RedisMessageListenerContainer container;
    private final BiConsumer<String, Object> consumer;
    private final String topic;
    private final Boolean isOnce;

    protected BusSub(
            RedisTemplate<String, Object> session,
            RedisMessageListenerContainer container,
            BiConsumer<String, Object> consumer,
            String topic,
            Boolean isOnce) {
        this.session = session;
        this.container = container;
        this.consumer = consumer;
        this.topic = topic;
        this.isOnce = isOnce;
    }

    @Override
    public void subscribe() {
        if (!subscribed.compareAndSet(false, true)) return;
        log.debug("lxm-redis-message-bus subscriber , topics : {}", topic);
        final MessageBusSubscriberAdapter subscriberAdapter =
                new MessageBusSubscriberAdapter(consumer);
        container.addMessageListener(subscriberAdapter, new ChannelTopic(topic));
        /*final MessageListenerAdapter adapter = new MessageListenerAdapter((MessageBusSubscriber) (payload, topic) -> consumer.accept(topic, payload), "onMessage");
        adapter.afterPropertiesSet();
        container.addMessageListener(adapter, new ChannelTopic(topic));
        Objects.requireNonNull(container.getConnectionFactory()).getConnection().keyCommands().expire(topic.getBytes(StandardCharsets.UTF_8), 30);
         */
    }

    private class MessageBusSubscriberAdapter implements MessageListener {
        private final java.util.concurrent.atomic.AtomicBoolean delivered =
                new java.util.concurrent.atomic.AtomicBoolean();

        private final BiConsumer<String, Object> consumer;

        public MessageBusSubscriberAdapter(BiConsumer<String, Object> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void onMessage(Message message, byte[] pattern) {
            if (isOnce && !delivered.compareAndSet(false, true)) return;
            String topic = new String(message.getChannel(), StandardCharsets.UTF_8);
            Object payload = session.getValueSerializer().deserialize(message.getBody());
            try {
                consumer.accept(topic, payload);
            } finally {
                if (isOnce) container.removeMessageListener(this);
            }
        }
    }
}
