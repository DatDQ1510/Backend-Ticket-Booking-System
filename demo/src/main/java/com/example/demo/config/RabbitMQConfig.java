package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RabbitMQConfig {
    // ====== EXCHANGE ======
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_DLX = "payment.dlx";

    // ====== QUEUES ======
    public static final String PAYMENT_QUEUE = "payment.queue";
    public static final String PAYMENT_DLQ = "payment.dlq";

    public static final String EMAIL_QUEUE = "email.queue";
    public static final String EMAIL_DLQ = "email.dlq";

    // ====== ROUTING ======
    public static final String PAYMENT_ROUTING_KEY = "payment.success";

    // ---------- EXCHANGE ----------
    @Bean
    FanoutExchange paymentExchange() {
        return new FanoutExchange(PAYMENT_EXCHANGE); // FanoutExchange để gửi đến tất cả các queue liên kết
    }

    @Bean
    DirectExchange paymentDLX() {
        return new DirectExchange(PAYMENT_DLX); // DirectExchange để định tuyến dựa trên routing key
    }

    // ---------- QUEUES ----------
    @Bean
    Queue paymentQueue() {
        return QueueBuilder.durable(PAYMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
                .withArgument("x-dead-letter-routing-key", PAYMENT_DLQ)
                .build();
    }

    @Bean
    Queue paymentDLQ() {
        return QueueBuilder.durable(PAYMENT_DLQ).build();
    }

    @Bean
    Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
                .withArgument("x-dead-letter-routing-key", EMAIL_DLQ)
                .build();
    }

    @Bean
    Queue emailDLQ() {
        return QueueBuilder.durable(EMAIL_DLQ).build();
    }

    // ---------- BINDINGS ----------
    @Bean
    Binding paymentBinding() {
        return BindingBuilder.bind(paymentQueue())
                .to(paymentExchange());
    }

    @Bean
    Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(paymentExchange());
    }


    // ---------- MESSAGE CONVERTER ----------
    @Bean
    Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ---------- PRODUCER ----------
    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(jsonMessageConverter());

        RetryTemplate retry = new RetryTemplate();
        ExponentialBackOffPolicy backoff = new ExponentialBackOffPolicy();
        backoff.setInitialInterval(500);
        backoff.setMultiplier(2);
        backoff.setMaxInterval(10000);
        retry.setBackOffPolicy(backoff);

        template.setRetryTemplate(retry);
        return template;
    }

    // ---------- CONSUMER ----------
    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory cf
    ) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(cf);
        factory.setMessageConverter(jsonMessageConverter());

        factory.setConcurrentConsumers(3);     // multiple workers
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(1);            // payment-safe
        factory.setDefaultRequeueRejected(false); // send to DLQ

        return factory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory cf) {
        return new RabbitAdmin(cf);
    }

}
