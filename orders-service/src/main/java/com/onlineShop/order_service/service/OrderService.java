package com.onlineShop.order_service.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.onlineShop.order_service.dto.InventoryResponse;
import com.onlineShop.order_service.dto.OrderLineItemsDto;
import com.onlineShop.order_service.dto.OrderRequest;
import com.onlineShop.order_service.event.OrderPlacedEvent;
import com.onlineShop.order_service.model.Order;
import com.onlineShop.order_service.model.OrderLineItems;
import com.onlineShop.order_service.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
	
	private final OrderRepository orderRepository;
	
	private final WebClient.Builder webClientBuilder;
	
	private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
	

	
	
	public String placeOrder(OrderRequest orderRequest) {
			Order order = new Order();
			order.setOrderNumber(UUID.randomUUID().toString());
			List<OrderLineItems> orderLineItems =orderRequest.getOrderLineItemsDtoList()
					.stream()
					.map(orderLineItemsDto -> mapToDto(orderLineItemsDto))
					.toList();
			order.setOrderLineItemsList(orderLineItems);
			
			List<String> skuCodes =order.getOrderLineItemsList().stream()
						.map(orderLineItem -> orderLineItem.getSkuCode())
						.toList();
			
			InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
							.uri("http://inventory-service/api/inventory",
									uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
							.retrieve()
							.bodyToMono(InventoryResponse[].class)
							.block(); // to make it synchronous response
			
			boolean allProductsInStock = Arrays.stream(inventoryResponseArray).allMatch(inventoryResponse -> inventoryResponse.getIsInStock());
			if (allProductsInStock) {
				orderRepository.save(order);
				kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
				return "Order Placed Successfully";
			}
			else {
				throw new IllegalArgumentException("Product is not in Stock");
				
			}
			
			}
	
	private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
		
		OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;
	}
			
	}

