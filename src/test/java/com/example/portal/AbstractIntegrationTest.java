package com.example.portal;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = AbstractIntegrationTest.Initializer.class)
@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration")
public class AbstractIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);

    @ClassRule
    public static GenericContainer rabbitMQ = new GenericContainer("rabbitmq:3-management")
            .withExposedPorts(5672, 15672)
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.of(240, ChronoUnit.SECONDS))
            .withLogConsumer(new Slf4jLogConsumer(logger));

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues values = TestPropertyValues.of(
//                    "spring.cloud.stream.binders.local_rabbit.environment.spring.rabbitmq.host=" + rabbitMQ.getContainerIpAddress(),
//                    "spring.cloud.stream.binders.local_rabbit.environment.spring.rabbitmq.port=" + rabbitMQ.getMappedPort(5672),
                    "spring.rabbitmq.addresses=" + rabbitMQ.getContainerIpAddress() + ":" + rabbitMQ.getMappedPort(5672)
            );
            values.applyTo(configurableApplicationContext);
        }
    }

    /**
     * @ClassRule public static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer()
     * .waitingFor(Wait.forListeningPort())
     * .withLogConsumer(new Slf4jLogConsumer(logger));
     */

    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Before
    public void setUp() {
        this.rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitTemplate.setExchange("queue.log.messages");
        this.rabbitTemplate.setQueue("queue.log.messages.portal");
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    @Test
    public void testHello() {
        Client client = new Client();
        client.setName("hello");
        rabbitTemplate.convertAndSend(client);
    }
}
