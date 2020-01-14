package com.stakater.nordmart.catalog.dto.command;

import lombok.Data;

import static com.stakater.nordmart.catalog.dto.command.ProductCommandType.PRODUCT_DELETE;

@Data
public class ProductDelete implements ProductCommand {

    @Override
    public ProductCommandType getType() {
        return PRODUCT_DELETE;
    }

    private String itemId;
}
