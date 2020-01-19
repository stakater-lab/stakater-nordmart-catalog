package com.stakater.nordmart;


import com.stakater.nordmart.catalog.CatalogApplication;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CatalogApplication.class,
        properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EnableKafka
@EmbeddedKafka(
        ports = {9092},
        partitions = 1,
        topics = "${kafka.products.topic}")
public abstract class BaseTest {

}