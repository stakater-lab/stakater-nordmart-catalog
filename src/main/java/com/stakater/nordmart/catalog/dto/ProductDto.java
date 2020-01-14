package com.stakater.nordmart.catalog.dto;

import lombok.Data;

import javax.persistence.Id;
import java.io.Serializable;

@Data
public class ProductDto implements Serializable {
    @Id
    private String itemId;
    private String name;
    private String description;
    private double price;
}

