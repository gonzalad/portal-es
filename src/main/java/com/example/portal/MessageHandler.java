package com.example.portal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@EnableBinding(Sink.class)
public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private ClientService clientService;

    public MessageHandler(ClientService clientService) {
        this.clientService = clientService;
    }

    @StreamListener(Sink.INPUT)
    public void listenForClient(Client client) {
        logger.info("Received {}", client);
        clientService.save(client);
    }
}
