package com.example.inventorysystem.util;

import com.example.inventorysystem.dto.OrderDTO;
import com.example.inventorysystem.dto.OrderItemDTO;
import com.example.inventorysystem.model.Order;
import com.example.inventorysystem.model.OrderItem;

import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderDTO toOrderDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus().name())
                .adminComments(order.getAdminComments())
                .items(order.getItems().stream()
                        .map(OrderMapper::toOrderItemDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public static OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        return OrderItemDTO.builder()
                .productId(orderItem.getProductId())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .build();
    }

    public static OrderItem toOrderItem(OrderItemDTO orderItemDTO) {
        return OrderItem.builder()
                .productId(orderItemDTO.getProductId())
                .quantity(orderItemDTO.getQuantity())
                .price(orderItemDTO.getPrice())
                .build();
    }
}
