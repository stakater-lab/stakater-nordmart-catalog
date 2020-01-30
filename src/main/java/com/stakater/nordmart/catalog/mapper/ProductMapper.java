package com.stakater.nordmart.catalog.mapper;

import com.stakater.nordmart.catalog.domain.Product;
import com.stakater.nordmart.catalog.dto.ProductDto;
import com.stakater.nordmart.catalog.dto.command.ProductCreate;
import com.stakater.nordmart.catalog.dto.command.ProductUpdate;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDto entityToDto(Product product);

    Product dtoToEntity(ProductDto productDto);

    ProductCreate entityToProductCreate(Product product);

    ProductUpdate entityToProductUpdate(Product product);
}
