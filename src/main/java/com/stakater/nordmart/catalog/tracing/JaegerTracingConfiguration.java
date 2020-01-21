package com.stakater.nordmart.catalog.tracing;

import io.opentracing.Tracer;
import io.jaegertracing.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class JaegerTracingConfiguration
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JaegerTracingConfiguration.class);

    @Value("${JAEGER_ENDPOINT}")
    private String jaegerEndpoint;

    @Value("${JAEGER_PROPAGATION}")
    private String jaegerPropogation;

    @Value("${JAEGER_TRACEID_128BIT}")
    private String jaegerTraceId;

    @Bean
    public Tracer jaegerTracer()
    {
        LOGGER.info("jaegerEndpoint {} jaegerPropogation {} jaegerTraceId {}", jaegerEndpoint, jaegerPropogation, jaegerTraceId);
        LOGGER.info("JaegerConfiguration: " + Configuration.fromEnv("nordmart-catalog-opentracing").toString());

        return Configuration.fromEnv("nordmart-catalog-opentracing").getTracer();
    }
}
