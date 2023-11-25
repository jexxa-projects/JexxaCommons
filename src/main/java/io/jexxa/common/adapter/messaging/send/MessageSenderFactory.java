package io.jexxa.common.adapter.messaging.send;

import io.jexxa.common.adapter.messaging.send.jms.JMSSender;
import io.jexxa.common.adapter.messaging.send.logging.MessageLogger;
import io.jexxa.common.facade.factory.ClassFactory;
import io.jexxa.common.facade.utils.annotation.CheckReturnValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static io.jexxa.common.facade.jms.JMSProperties.JNDI_FACTORY_KEY;


public final class MessageSenderFactory
{
    private static final MessageSenderFactory MESSAGE_SENDER_MANAGER = new MessageSenderFactory();

    private static final Map<Class<?> , Class<? extends MessageSender>> STRATEGY_MAP = new HashMap<>();
    private static Class<? extends MessageSender> defaultStrategy = null;

    private MessageSenderFactory()
    {
    }

    @CheckReturnValue
    // When registering a MessageSender, we check its type so that we can perform an unchecked cast
    private <T> Class<? extends MessageSender> getStrategy(Class<T> aggregateClazz, Properties properties)
    {
        // 1. Check if a dedicated strategy is registered for aggregateClazz
        var result = STRATEGY_MAP
                .entrySet()
                .stream()
                .filter( element -> element.getKey().equals(aggregateClazz))
                .filter( element -> element.getValue() != null )
                .findFirst();

        if (result.isPresent())
        {
            return result.get().getValue();
        }

        // 2. If a default strategy is available, return this one
        if (defaultStrategy != null)
        {
            return defaultStrategy;
        }

        // 3. If a JNDI Factory is defined and simulation mode is deactivated => Use JMSSender
        if (properties.containsKey(JNDI_FACTORY_KEY))
        {
            return JMSSender.class;
        }

        // 5. In all other cases (including simulation mode) return a MessageLogger
        return MessageLogger.class;
    }

    @SuppressWarnings("unused")
    public static Class<?> getDefaultMessageSender(Properties properties)
    {
        return getMessageSender(null, properties).getClass();
    }

    @SuppressWarnings("unused")
    public static <U extends MessageSender, T > void setStrategy(Class<U> strategyType, Class<T> aggregateType)
    {
        STRATEGY_MAP.put(aggregateType, strategyType);
    }

    public static void setDefaultStrategy(Class<? extends MessageSender>  defaultStrategy)
    {
        Objects.requireNonNull(defaultStrategy);

        MessageSenderFactory.defaultStrategy = defaultStrategy;
    }


    public static <T>  MessageSender getMessageSender(Class<T> sendingClass, Properties properties)
    {
        try
        {
            var strategy = MESSAGE_SENDER_MANAGER.getStrategy(sendingClass, properties);

            var result = ClassFactory.newInstanceOf(strategy, new Object[]{properties});
            if (result.isEmpty()) //Try factory method with properties
            {
                result = ClassFactory.newInstanceOf(MessageSender.class, strategy,new Object[]{properties});
            }
            if (result.isEmpty()) //Try default constructor
            {
                result = ClassFactory.newInstanceOf(strategy);
            }
            if (result.isEmpty()) //Try factory method without properties
            {
                result = ClassFactory.newInstanceOf(MessageSender.class, strategy);
            }

            return result.orElseThrow();
        }
        catch (ReflectiveOperationException e)
        {
            if ( e.getCause() != null)
            {
                throw new IllegalArgumentException(e.getCause().getMessage(), e);
            }

            throw new IllegalArgumentException("No suitable default MessageSender available", e);
        }
    }

}