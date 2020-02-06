package com.stakater.nordmart.catalog.controller;

import com.stakater.nordmart.catalog.domain.Product;
import com.stakater.nordmart.catalog.dto.ProductDto;
import com.stakater.nordmart.catalog.exception.NotFoundException;
import com.stakater.nordmart.catalog.repository.ProductRepository;
import com.stakater.nordmart.catalog.service.CatalogService;
import com.stakater.nordmart.catalog.tracing.Traced;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Api("Controller responsible for operations with products.")
@Controller
@RequestMapping(value = "/api/products")
public class CatalogController {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogController.class);

    private final ProductRepository repository;
    private final Counter requests;
    private final AtomicInteger productCount;
    private final Timer timer;
    private final CatalogService catalogService;

    public CatalogController(ProductRepository repository, MeterRegistry meterRegistry,
                             CatalogService catalogService) {
        this.repository = repository;
        this.requests = Counter.builder("count_requests_total")
            .description("Total count requests.")
            .register(meterRegistry);
        timer = Timer.builder("catalogController").tag("method", "getAll").register(meterRegistry);
        this.productCount = meterRegistry.gauge("product_count", new AtomicInteger(0));
        this.catalogService = catalogService;
    }

    @Traced
    @ResponseBody
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<Product> getAll(@RequestHeader HttpHeaders headers) {

        LOG.info("Rest request to get all products with headers {}", headers);
        requests.increment();

        long start = System.nanoTime();

        Spliterator<Product> products = repository.findAll().spliterator();
        List<Product> productList = StreamSupport.stream(products, false).collect(Collectors.toList());

        timer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        productCount.set(productList.size());
        return productList;
    }

    @Traced
    @ApiOperation(value = "Saves product.",
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ProductDto saveProduct(@RequestBody ProductDto productDto) {
        return catalogService.store(productDto);
    }

    @ApiOperation(value = "Updated product.",
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @PutMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ProductDto updateProduct(@RequestBody ProductDto productDto) {
        return catalogService.update(productDto);
    }

    @ApiOperation(value = "Removes product by provided itemId. Returns 404 if product was not found.",
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @DeleteMapping(path = "/{itemId}")
    public ResponseEntity<String> deleteProduct(@PathVariable(name = "itemId") String itemId) {
        try {
            catalogService.delete(itemId);
            return new ResponseEntity("Successfully deleted item with id " + itemId, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity("Item with id " + itemId + " was not found.", HttpStatus.NOT_FOUND);
        }
    }

    @Traced
    @ApiOperation(value = "Gets product by provided itemId. Returns 404 status if such product  was not found.",
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @GetMapping(path = "/{itemId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getProduct(@PathVariable(name = "itemId") String itemId) {
        Optional<ProductDto> productDto = catalogService.get(itemId);
        if (productDto.isPresent()) {
            return new ResponseEntity(productDto.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity("Product with item id " + itemId + " not Found", HttpStatus.NOT_FOUND);
        }
    }
}
