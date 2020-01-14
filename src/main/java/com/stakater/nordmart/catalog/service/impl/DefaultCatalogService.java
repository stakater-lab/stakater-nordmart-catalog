package com.stakater.nordmart.catalog.service.impl;

import com.stakater.nordmart.catalog.domain.Product;
import com.stakater.nordmart.catalog.dto.ProductDto;
import com.stakater.nordmart.catalog.exception.NotFoundException;
import com.stakater.nordmart.catalog.mapper.ProductMapper;
import com.stakater.nordmart.catalog.messaging.MessageSender;
import com.stakater.nordmart.catalog.repository.ProductRepository;
import com.stakater.nordmart.catalog.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DefaultCatalogService implements CatalogService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultCatalogService.class);

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private MessageSender messageSender;

    @Override
    public ProductDto store(ProductDto productDto) {
        LOG.info("Going to save product {}", productDto);
        Product product = productMapper.dtoToEntity(productDto);
        Product savedProduct = productRepository.save(product);
        messageSender.sendCreate(savedProduct);
        LOG.info("Saved product {}", savedProduct);
        return productMapper.entityToDto(savedProduct);
    }

    @Override
    public ProductDto update(ProductDto productDto) {
        LOG.info("Going to update product {}", productDto);
        Optional<Product> foundProductOptional = productRepository.findById(productDto.getItemId());
        if (foundProductOptional.isPresent()) {
            Product foundProduct = foundProductOptional.get();
            foundProduct.setName(productDto.getName());
            foundProduct.setDescription(productDto.getDescription());
            foundProduct.setPrice(productDto.getPrice());

            Product updatedProduct = productRepository.save(foundProduct);
            messageSender.sendUpdate(updatedProduct);
            LOG.info("Updated product {}", updatedProduct);
            return productMapper.entityToDto(updatedProduct);
        } else {
            return store(productDto);
        }
    }

    @Override
    public void delete(String itemId) throws NotFoundException {
        LOG.info("Going to delete product with item id {}", itemId);
        Optional<Product> product = productRepository.findById(itemId);
        if (product.isPresent()) {
            productRepository.deleteById(itemId);
            messageSender.sendDelete(itemId);
            LOG.info("Deleted product with item id {}", itemId);
        } else {
            LOG.error("Product with item id {} was not found", itemId);
            throw new NotFoundException("Product with item id " + itemId + " was not found");
        }
    }

    @Override
    public Optional<ProductDto> get(String itemId) {
        LOG.info("Going to get product with item id {}", itemId);
        Optional<Product> product = productRepository.findById(itemId);
        if (product.isPresent()) {
            return Optional.of(productMapper.entityToDto(product.get()));
        } else {
            return Optional.empty();
        }
    }
}
