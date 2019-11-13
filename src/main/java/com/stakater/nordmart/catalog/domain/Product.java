package com.stakater.nordmart.catalog.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "PRODUCT", uniqueConstraints = @UniqueConstraint(columnNames = "itemId"))
public class Product implements Serializable {

	// Specify column length and size to avoid SQLSyntaxErrorException: Specified key was too long; max key length is 1000 bytes
	@Id
	@NotNull
	@Size(min = 0, max = 50)
	@Column(length = 50)
	private String itemId;

	private String name;

	private String description;

	private double price;

	public Product() {
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "Product [itemId=" + itemId + ", name=" + name + ", price=" + price + "]";
	}
}
