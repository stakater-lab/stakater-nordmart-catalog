package com.stakater.nordmart.catalog.service;

import com.stakater.nordmart.catalog.dto.ProductDto;
import com.stakater.nordmart.catalog.exception.NotFoundException;

import java.util.Optional;

/**
 * Service responsible for operations with products.
 */
public interface CatalogService {

    /**
     * Store product.
     *
     * @param productDto
     * @return
     */
    ProductDto store(ProductDto productDto);

    /**
     * Updates product with new data.
     *
     * @param productDto product data to updated
     * @return updated product
     */
    ProductDto update(ProductDto productDto);

    /**
     * Deletes product by provided itemId.
     *
     * @param itemId item Id of the product that needs to be deleted
     * @throws NotFoundException if entity was not found
     */
    void delete(String itemId) throws NotFoundException;

    /**
     * Returns product by provided itemId. Returns empty optional if product was not found.
     *
     * @param itemId item Id of the product to be returned
     * @return Optional with product if it was found and empty optional if it was not found.
     */
    Optional<ProductDto> get(String itemId);
}
