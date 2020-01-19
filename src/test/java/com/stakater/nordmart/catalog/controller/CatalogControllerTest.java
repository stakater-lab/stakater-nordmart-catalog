package com.stakater.nordmart.catalog.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.stakater.nordmart.BaseTest;
import com.stakater.nordmart.catalog.dto.ProductDto;
import com.stakater.nordmart.catalog.dto.command.ProductCreate;
import com.stakater.nordmart.catalog.dto.command.ProductUpdate;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CatalogControllerTest extends BaseTest {
    private static final String TEST_SERVER_URL = "http://localhost:8080";
    private static final String CONTROLLER_PATH = "/api/products";
    private static final String PRODUCT_ITEM_ID = "1";
    private static final String PRODUCT_DESCRIPTION = "Phone";
    private static final int PRODUCT_PRICE = 800;

    @Value("${kafka.products.topic}")
    private String productsTopicName;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mvc;
    private KafkaConsumer<Integer, String> kafkaConsumer;

    @BeforeEach
    public void beforeEach() {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.context).build();

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("sampleRawConsumer", "false", embeddedKafka);
        consumerProps.put("auto.offset.reset", "earliest");
        kafkaConsumer = new KafkaConsumer<>(consumerProps);
        kafkaConsumer.subscribe(Collections.singletonList(productsTopicName));
    }

    @AfterEach
    public void afterEach() {
        synchronized (kafkaConsumer) {
            kafkaConsumer.close();
        }
    }

    @Test
    void saveProduct() throws Exception {
        ProductDto productDto = sendCreateProductRequest();

        final List<String> receivedMessages = receiveMessages(1);

        assertEquals(1, receivedMessages.size());
        String receivedMessage = receivedMessages.get(0);
        assertCreatedProduct(productDto, receivedMessage);
    }


    @Test
    void updateProduct() throws Exception {
        ProductDto productDto = sendCreateProductRequest();

        ProductDto updateProductDto = updateProductDto();

        String jsonUpdateProductDto = objectMapper.writeValueAsString(updateProductDto);

        mvc.perform(MockMvcRequestBuilders.put(TEST_SERVER_URL + CONTROLLER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonUpdateProductDto))
                .andExpect(status().isOk());

        List<String> receivedMessages = receiveMessages(2);

        assertEquals(2, receivedMessages.size());
        String createProductDtoString = receivedMessages.get(0);
        assertCreatedProduct(productDto, createProductDtoString);

        String updateMessage = receivedMessages.get(1);

        ProductUpdate actualUpdateProductDto = objectMapper.readValue(updateMessage, ProductUpdate.class);
        assertEquals(updateProductDto.getItemId(), actualUpdateProductDto.getItemId());
        assertEquals(updateProductDto.getDescription(), actualUpdateProductDto.getDescription());
        assertEquals(updateProductDto.getName(), actualUpdateProductDto.getName());
        assertEquals(updateProductDto.getPrice(), actualUpdateProductDto.getPrice());
    }



    @Test
    void deleteProduct() throws Exception {
        ProductDto productDto = sendCreateProductRequest();

        mvc.perform(MockMvcRequestBuilders.get(TEST_SERVER_URL + CONTROLLER_PATH + "/{jobId}", productDto.getItemId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.itemId", is("1")))
                .andExpect(status().isOk());

        mvc.perform(MockMvcRequestBuilders.delete(TEST_SERVER_URL + CONTROLLER_PATH + "/{jobId}", productDto.getItemId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(MockMvcRequestBuilders.get(TEST_SERVER_URL + CONTROLLER_PATH + "/{jobId}", productDto.getItemId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProduct() throws Exception {
        ProductDto productDto = sendCreateProductRequest();

        mvc.perform(MockMvcRequestBuilders.get(TEST_SERVER_URL + CONTROLLER_PATH + "/{jobId}", productDto.getItemId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.itemId", is("1")))
                .andExpect(status().isOk());
    }

    private void assertCreatedProduct(ProductDto productDto, String receivedMessage) throws JsonProcessingException {
        ProductCreate actualProductDto = objectMapper.readValue(receivedMessage, ProductCreate.class);
        assertEquals(productDto.getItemId(), actualProductDto.getItemId());
        assertEquals(productDto.getDescription(), actualProductDto.getDescription());
        assertEquals(productDto.getName(), actualProductDto.getName());
        assertEquals(productDto.getPrice(), actualProductDto.getPrice());
    }

    private ProductDto sendCreateProductRequest() throws Exception {
        ProductDto productDto = createProductDto();

        String jsonProductsDto = objectMapper.writeValueAsString(productDto);

        mvc.perform(MockMvcRequestBuilders.post(TEST_SERVER_URL + CONTROLLER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonProductsDto))
                .andExpect(status().isOk());
        return productDto;
    }

    private List<String> receiveMessages(int latchCounter) throws InterruptedException {
        final List<String> receivedMessages = Lists.newArrayList();
        final CountDownLatch latch = new CountDownLatch(latchCounter);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            while (true) {
                Duration timeout = Duration.ofMillis(100);
                synchronized (kafkaConsumer) {
                    ConsumerRecords<Integer, String> records = kafkaConsumer.poll(timeout);
                    records.iterator().forEachRemaining(record -> {
                        receivedMessages.add(record.value());
                        latch.countDown();
                    });
                }
            }
        });

        latch.await(10, TimeUnit.SECONDS);
        return receivedMessages;
    }

    private ProductDto createProductDto() {
        ProductDto productDto = new ProductDto();
        productDto.setItemId(PRODUCT_ITEM_ID);
        productDto.setDescription(PRODUCT_DESCRIPTION);
        productDto.setName("Big brand");
        productDto.setPrice(PRODUCT_PRICE);
        return productDto;
    }

    private ProductDto updateProductDto() {
        ProductDto productDto = new ProductDto();
        productDto.setItemId(PRODUCT_ITEM_ID);
        productDto.setDescription(PRODUCT_DESCRIPTION);
        productDto.setName("Bigger brand");
        productDto.setPrice(PRODUCT_PRICE);
        return productDto;
    }
}