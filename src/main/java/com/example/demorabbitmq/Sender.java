package com.example.demorabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class Sender {
    private final Logger log = LoggerFactory.getLogger(Sender.class);
    private final RabbitTemplate rabbitTemplate;
    private final ReceiveChecker receiveChecker;

    public Sender(RabbitTemplate rabbitTemplate, ReceiveChecker receiveChecker) {
        this.rabbitTemplate = rabbitTemplate;
        this.receiveChecker = receiveChecker;
    }

    private final AtomicLong counter = new AtomicLong();
    private final AtomicBoolean send = new AtomicBoolean(true);

    @Scheduled(fixedDelay = 20L)
    public void send() {
        String message = null;
        try {
            if (send.get()) {
                message = String.format("%08d", counter.incrementAndGet());
                this.rabbitTemplate.convertAndSend("ha.foo", message);
                this.receiveChecker.recordSend(message);
            }
        } catch (RuntimeException e) {
            log.error("failed = " + message, e);
        }
    }

    @PreDestroy
    public void foo() {
        send.set(false);
        log.info("Stop " + counter.get());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.receiveChecker.remains().forEach(x -> log.info(x));
    }

}
