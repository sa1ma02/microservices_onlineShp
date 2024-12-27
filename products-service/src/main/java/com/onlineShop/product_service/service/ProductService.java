package com.onlineShop.product_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onlineShop.product_service.dto.ProductRequest;
import com.onlineShop.product_service.dto.ProductResponse;
import com.onlineShop.product_service.model.Product;
import com.onlineShop.product_service.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
	
	private final ProductRepository productRepository;
	
	public void createProduct(ProductRequest productRequest) {
		Product product = Product.builder()
				.name(productRequest.getName())
				.description(productRequest.getDescription())
				.price(productRequest.getPrice())
				.build();
		productRepository.save(product);
		log.info("Product {} created successfully", product.getId());
	}

	public List<ProductResponse> getAllProducts() {
		
		return productRepository.findAll().stream().
				map(product -> ProductResponse.builder()
						.id(product.getId())
						.name(product.getName())
						.description(product.getDescription())
						.price(product.getPrice())
						.build())
						.toList();
	}

}
