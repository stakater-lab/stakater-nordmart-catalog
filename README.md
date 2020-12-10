# stakater-nordmart-catalog

## Overview

A maven spring boot catalog application for the product catalog and product information retrieval.

## User scenarios

**Product operations**

Service has API for CRUD operations for products:
* Create (POST request against "/api/products/" URL) - service saves entity into the database and also sends product 
create command to products topic of Kafka. Search service consumes this message and store product into Elasticsearch.
 Product can be searched via Nordmart Web after that.
* Update (PUT request against "/api/products/" URL) - service update entity into the database and also sends product 
update command to products topic of Kafka. Search service consumes this message and updates product in Elasticsearch. 
Updated product can be searched via Nordmart Web after that.
* Delete (DELETE request against "/api/products/{itemaId}" URL) - service deletes entity from the database and also 
sends product delete command to products topic of Kafka. Search service consumes this message and deletes product from 
 Elasticsearch. Product no longer available for search via Nordmart Web after that.   
* Read (GET request against "/api/products/{itemaId}" URL) - service exposes API to get product by itemId. Data about 
product is got from Database.

## Dependencies

It requires following things to be installed:

* Java: ^8.0
* Maven

## Deployment strategy

### Local deployment

To run the application locally use the command given below:

```bash
mvn spring-boot:run
```


### Docker

To deploy app inside a docker container

* Create a network if it doesn't already exist by executing

  ```bash
  docker network create --driver bridge nordmart-apps
  ```

* Build jar file of the app by executing command given below:

  ```bash
  mvn clean package
  ```

* Next build the image using

  ```bash
  docker build -t catalog .
  ```

* Finally run the image by executing

  ```bash
  docker run -d --name catalog --network nordmart-apps -p 8080:8080 catalog
  ```

## Helm Charts

### Pre-requisites

Helm operator needs to to be running inside the cluster. Helm operator is deployed by Stakater Global Stack, deployment guidelines are provided in this [link](https://playbook.stakater.com/content/processes/bootstrapping/deploying-stack-on-azure.html)

### Helm chart deployment

To create helm release of this application using the command given below:

kubectl apply -f [helm-release](https://github.com/stakater-lab/nordmart-dev-apps/blob/master/releases/catalog-helm-release.yaml).yaml -n <namespace-name>

## Prometheus

### Prometheus Dependencies

The following dependencies are needed to expose micrometer and application metrics

```xml
<dependencies>
    <!-- For micrometer support -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-core</artifactId>
        <version>1.1.4</version>
    </dependency>
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
        <version>1.1.4</version>
    </dependency>
</dependencies>
```

### Configuration


**Startup configuration**

By default, application synchronizes state of the database on the startup with Kafka. This is helpful
during testing. You can disable it by setting false to the kafka.sync.onstartup property.

**Kafka configuration**
* kafka.bootstrapAddress - address of the Kafka broker
* kafka.products.topic - name of the topic where products will be send on CRU operations 

**Micrometer configuration**

Add the following properties to `application.properties` to expose the micrometer endpoint.

```bash
management.endpoint.metrics.enabled=true
management.endpoints.web.exposure.include=*
management.endpoint.prometheus.enabled=true
management.metrics.export.prometheus.enabled=true
```

### Adding micrometer registry

Add the MeterRegistry bean to your spring boot application by adding the follwoing snippet to your SpringBootApplication class.

```java
    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "common-service");
    }
```

This will help you create custom metrics within the application

### Counter

To count the number of times an operation has been performed, just create a `io.micrometer.core.instrument.Counter` variable by doing

```java
Counter.builder("count_metric_name").description("Description of metric").register(meterRegistry);
```

### Time Measurement

To add metrics that keeps track of processing time taken by a piece of code, follow the following snippet:

```java
private final Timer timer = Timer.builder("metricsname").tag("tagKey", "tagValue").register(meterRegistry);
long start = System.nanoTime();
...your code here
timer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
```

### Gauge

A gauge is a handle to get the current value. Typical examples for gauges would be the size of a collection or map.
To create a gauge metric just do

```java
AtomicInteger myCount = meterRegistry.gauge("gauge_value", new AtomicInteger(0));
```

and then you can just set the value as it changes using

```java
myCount.set(myList.size());
```

### Monitoring

Dasbhoards given below can be used to monitor application by configuring them in Monitoring stack. If monitoring stack is not already configured use guidelines given in this [link](https://playbook.stakater.com/content/processes/bootstrapping/deploying-stack-on-azure.html) to configure it. 

* Catalog service metrics dashboard can be configured using this [config](https://github.com/stakater-lab/nordmart-dev-apps/blob/master/releases/catalog-service-dashboard.yaml).

  ![catalog-service.png](docs/images/catalog-service.png)
  
