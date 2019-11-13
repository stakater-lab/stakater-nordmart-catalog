package com.stakater.nordmart.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan
@EnableJpaRepositories
@SpringBootApplication
public class CatalogApplication
{
    public static void main(String[] args) {
        SpringApplication.run(CatalogApplication.class, args);
    }
}
