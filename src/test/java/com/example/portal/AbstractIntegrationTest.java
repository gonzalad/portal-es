package com.example.portal;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = AbstractIntegrationTest.Initializer.class)
@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration")
public class AbstractIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);

    private static final String RABBITMQ_NAME = "rabbitmq";
    private static final int RABBITMQ_PORT = 5672;
    public static final String QUEUE_NAME = "queue.log.messages.portal";
    public static final String DEAD_LETTER_QUEUE_NAME = QUEUE_NAME + ".dlq";
    public static final String EXCHANGE_NAME = "queue.log.messages";

    private static final String ES_NAME = "elasticsearch";
    private static final int ES_PORT = 9200;

    @ClassRule
    public static DockerComposeContainer environment =
            new DockerComposeContainer(new File("src/main/docker/docker-compose.yml"))
                    .withExposedService(RABBITMQ_NAME, RABBITMQ_PORT, Wait.forListeningPort().withStartupTimeout(Duration.of(240, ChronoUnit.SECONDS)))
                    //.withLogConsumer(RABBITMQ_NAME, new Slf4jLogConsumer(RabbitMQContainer.Logger))
                    .withExposedService(ES_NAME, ES_PORT, Wait.forListeningPort().withStartupTimeout(Duration.of(480, ChronoUnit.SECONDS)))
                    //.withLogConsumer(ES_NAME, new Slf4jLogConsumer(ElasticsearchContainer.Logger))
                    .withLocalCompose(true)
                    .withTailChildContainers(true);
                    //.withExposedService("elasticsearch_1", ELASTICSEARCH_PORT);


    /*@ClassRule
    public static GenericContainer rabbitMQ = new GenericContainer("rabbitmq:3-management")
            .withExposedPorts(5672, 15672)
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.of(240, ChronoUnit.SECONDS))
            .withLogConsumer(new Slf4jLogConsumer(logger));
*/

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

            TestPropertyValues values = TestPropertyValues.of(
//                    "spring.cloud.stream.binders.local_rabbit.environment.spring.rabbitmq.host=" + rabbitMQ.getContainerIpAddress(),
//                    "spring.cloud.stream.binders.local_rabbit.environment.spring.rabbitmq.port=" + rabbitMQ.getMappedPort(5672),
                    "spring.rabbitmq.addresses=" + environment.getServiceHost(RABBITMQ_NAME, RABBITMQ_PORT) + ":" + environment.getServicePort(RABBITMQ_NAME, RABBITMQ_PORT),
                    "spring.data.elasticsearch.cluster-nodes=" + environment.getServiceHost(ES_NAME, ES_PORT) + ":" + environment.getServicePort(ES_NAME, ES_PORT)
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

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Before
    public void setUp() {
        this.rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitTemplate.setExchange(EXCHANGE_NAME);
        this.rabbitTemplate.setQueue(QUEUE_NAME);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitAdmin.purgeQueue(QUEUE_NAME);
        rabbitAdmin.purgeQueue(DEAD_LETTER_QUEUE_NAME);
    }

    @Test
    public void testHello() {
        Client client = new Client();
        client.setFirstName("hello");
        client.setLastName("hello");

        rabbitTemplate.convertAndSend(client);
    }

    @Test
    public void givenInvalidMessageWhenSendThenWillGoToDeadLetterQueue() throws InterruptedException {
        Client client = new Client();
        client.setFirstName("hello");

        rabbitTemplate.convertAndSend(client);
        logger.debug("Waiting for listener to receive and process message");
        Thread.sleep(1000L);

        Client clientInDeadLetterQueue = (Client) rabbitTemplate.receiveAndConvert(DEAD_LETTER_QUEUE_NAME);
        assertThat(clientInDeadLetterQueue).isEqualTo(client);
    }

    private static final class ElasticsearchContainer {
        private static final Logger Logger = LoggerFactory.getLogger(ElasticsearchContainer.class);
    }

    private static final class RabbitMQContainer {
        private static final Logger Logger = LoggerFactory.getLogger(RabbitMQContainer.class);
    }
}
