package com.stakater.nordmart.catalog.dto.command;

import lombok.Data;

import static com.stakater.nordmart.catalog.dto.command.ProductCommandType.PRODUCT_UPDATE;

@Data
public class ProductUpdate implements ProductCommand {

    @Override
    public ProductCommandType getType() {
        return PRODUCT_UPDATE;
    }

    private String itemId;
    private String name;
    private String description;
    private double price;
}
