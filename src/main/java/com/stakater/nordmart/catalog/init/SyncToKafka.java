package com.stakater.nordmart.catalog.init;

import com.stakater.nordmart.catalog.domain.Product;
import com.stakater.nordmart.catalog.messaging.MessageSender;
import com.stakater.nordmart.catalog.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SyncToKafka {
    private static final Logger LOG = LoggerFactory.getLogger(SyncToKafka.class);

    @Value("${kafka.sync.onstartup}")
    private Boolean syncOnStartup = Boolean.FALSE;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MessageSender messageSender;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if(syncOnStartup) {
            LOG.info("Going synchronize DB with Kafka.");
            Iterable<Product> products = productRepository.findAll();
            products.forEach(product -> messageSender.sendCreate(product));
            LOG.info("Going synchronized DB with Kafka.");
        }
    }
}
