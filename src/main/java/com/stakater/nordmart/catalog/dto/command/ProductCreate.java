package com.stakater.nordmart.catalog.dto.command;

import lombok.Data;

import static com.stakater.nordmart.catalog.dto.command.ProductCommandType.PRODUCT_CREATE;

@Data
public class ProductCreate implements ProductCommand {

    @Override
    public ProductCommandType getType() {
        return PRODUCT_CREATE;
    }

    private String itemId;
    private String name;
    private String description;
    private double price;
}
