package com.example.demorabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Sender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final AtomicLong counter = new AtomicLong();
    private final AtomicBoolean send = new AtomicBoolean(true);

    @Scheduled(fixedDelay = 300L)
    public void send() {
        if (send.get()) {
            this.rabbitTemplate.convertAndSend("ha.foo", "hello" + counter.incrementAndGet());
        }
    }

    @PreDestroy
    public void foo() {
        send.set(false);
        System.out.println("Stop " + counter.get());
    }

}
