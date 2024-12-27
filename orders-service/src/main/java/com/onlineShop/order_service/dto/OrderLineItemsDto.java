package com.onlineShop.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderLineItemsDto {
	
	
	private Long id;
	private String skuCode;
	private Double price;
	private Integer quantity;

}
