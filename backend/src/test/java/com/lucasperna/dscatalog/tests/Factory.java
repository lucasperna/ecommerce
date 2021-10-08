package com.lucasperna.dscatalog.tests;

import java.time.Instant;

import com.lucasperna.dscatalog.dto.ProductDTO;
import com.lucasperna.dscatalog.entities.Category;
import com.lucasperna.dscatalog.entities.Product;

public class Factory {

	public static Product createProduct() {
		Product prod = new Product(1L, "Phone", "Good Phone", 800.0, "", Instant.parse("2020-10-20T03:00:00Z"));
		prod.getCategories().add(createCategory());
		return prod;
	}
	
	public static ProductDTO createProductDTO() {
		Product prod = createProduct();
		return new ProductDTO(prod, prod.getCategories());
	}
	
	public static Category createCategory() {
		return new Category(1L, "Electronics");
	}
}
