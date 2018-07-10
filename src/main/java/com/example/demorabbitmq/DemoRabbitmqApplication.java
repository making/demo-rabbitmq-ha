package com.example.demorabbitmq;

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.PolicyInfo;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootApplication
@RabbitListener(queues = "ha.foo")
@EnableScheduling
public class DemoRabbitmqApplication {
    @Bean
    public Sender mySender() {
        return new Sender();
    }

    @Bean
    public Queue fooQueue(RabbitProperties props) throws Exception {
        this.declareHaPolicy(props);
        return QueueBuilder.durable("ha.foo") //
                .build();
    }

    private void declareHaPolicy(RabbitProperties props) throws Exception {
        // https://www.rabbitmq.com/ha.html#examples
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("ha-mode", "all");
        Client client = new Client("http://" + props.determineHost() + ":15672/api/", props.getUsername(), props.getPassword());
        PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.setPattern("^ha\\.");
        policyInfo.setDefinition(definition);
        client.declarePolicy(props.getVirtualHost(), "ha-all", policyInfo);
    }

    @RabbitHandler
    public void process(@Payload String foo) {
        System.out.println(new Date() + ": " + foo);
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoRabbitmqApplication.class, args);
    }
}
