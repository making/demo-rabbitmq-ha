package com.example.demorabbitmq;

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.PolicyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootApplication
@RabbitListener(queues = "ha.foo", concurrency = "5")
@EnableScheduling
public class DemoRabbitmqApplication {
    private final Logger log = LoggerFactory.getLogger(DemoRabbitmqApplication.class);

    @Bean
    @DependsOn("deadLetterQueue")
    public Queue fooQueue(RabbitProperties props) throws Exception {
        this.declareHaPolicy(props);
        return QueueBuilder.durable("ha.foo") //
                .withArgument("x-dead-letter-exchange", "") //
                .withArgument("x-dead-letter-routing-key", "ha.dlq") //
                .build();
    }

    @Bean
    public Queue deadLetterQueue(RabbitProperties props) throws Exception {
        this.declareHaPolicy(props);
        return QueueBuilder.durable("ha.dlq") //
                .build();
    }

    private final ReceiveChecker receiveChecker;

    public DemoRabbitmqApplication(ReceiveChecker receiveChecker) {
        this.receiveChecker = receiveChecker;
    }

    private void declareHaPolicy(RabbitProperties props) throws Exception {
        // https://www.rabbitmq.com/ha.html#examples
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("ha-mode", "all");
        Client client = new Client("https://" + props.determineHost() + ":15671/api/", props.getUsername(), props.getPassword());
        PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.setPattern("^ha\\.");
        policyInfo.setDefinition(definition);
        client.declarePolicy(props.getVirtualHost(), "ha-all", policyInfo);
    }

    @RabbitHandler
    public void process(@Payload String foo) {
        this.receiveChecker.recordReceive(foo);
        log.info(foo);
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoRabbitmqApplication.class, args);
    }
}
