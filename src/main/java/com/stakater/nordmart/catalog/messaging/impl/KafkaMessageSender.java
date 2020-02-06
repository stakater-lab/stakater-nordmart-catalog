package com.stakater.nordmart.catalog.messaging.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stakater.nordmart.catalog.domain.Product;
import com.stakater.nordmart.catalog.dto.command.ProductCreate;
import com.stakater.nordmart.catalog.dto.command.ProductDelete;
import com.stakater.nordmart.catalog.dto.command.ProductUpdate;
import com.stakater.nordmart.catalog.mapper.ProductMapper;
import com.stakater.nordmart.catalog.messaging.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageSender implements MessageSender {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaMessageSender.class);

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Value("${kafka.products.topic}")
    private String productsTopicName;

    @Override
    public void sendCreate(Product product) {
        LOG.info("Going to send product create command for product {} to topic {}", product, productsTopicName);
        ProductCreate productCreate = productMapper.entityToProductCreate(product);
        try {
            String productCreateString = objectMapper.writeValueAsString(productCreate);
            kafkaTemplate.send(productsTopicName, productCreateString);
            LOG.info("Sent product create command for product {} to topic {}", product, productsTopicName);
        } catch (JsonProcessingException e) {
            LOG.info("Could not convert product create " + productCreate + " to JSON.", e);
        }
    }

    @Override
    public void sendUpdate(Product product) {
        LOG.info("Going to send product update command for product {} to topic {}", product, productsTopicName);
        ProductUpdate productUpdate = productMapper.entityToProductUpdate(product);
        try {
            String productUpdateString = objectMapper.writeValueAsString(productUpdate);
            kafkaTemplate.send(productsTopicName, productUpdateString);
            LOG.info("Sent product update command for product {} to topic {}", product, productsTopicName);
        } catch (JsonProcessingException e) {
            LOG.info("Could not convert product update " + productUpdate + " to JSON.", e);
        }
    }

    @Override
    public void sendDelete(String itemId) {
        LOG.info("Going to send product delete command for item with Id {} to topic {}", itemId, productsTopicName);
        ProductDelete productDelete = new ProductDelete();
        productDelete.setItemId(itemId);
        try {
            String productDeleteString = objectMapper.writeValueAsString(productDelete);
            kafkaTemplate.send(productsTopicName, productDeleteString);
            LOG.info("Sent product delete command for item with Id {} to topic {}", itemId, productsTopicName);
        } catch (JsonProcessingException e) {
            LOG.info("Could not convert product update " + productDelete + " to JSON.", e);
        }
    }
}
