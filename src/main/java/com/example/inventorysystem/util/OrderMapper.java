package com.example.inventorysystem.util;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.inventorysystem.dto.OrderDTO;
import com.example.inventorysystem.dto.OrderItemDTO;
import com.example.inventorysystem.model.Order;
import com.example.inventorysystem.model.OrderItem;

public class OrderMapper {

    private static final Logger logger = LoggerFactory.getLogger(OrderMapper.class);

    public static OrderDTO toOrderDTO(Order order) {
        if (order == null) {
            logger.error("Order object is null during mapping to OrderDTO.");
            return null;
        }

        logger.debug("Mapping Order to OrderDTO: {}", order);

        OrderDTO orderDTO = OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus().name())
                .adminComments(order.getAdminComments())
                .items(order.getItems().stream()
                        .map(OrderMapper::toOrderItemDTO)
                        .collect(Collectors.toList()))
                .build();

        logger.debug("Mapped OrderDTO: {}", orderDTO);
        return orderDTO;
    }

    public static OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        if (orderItem == null) {
            logger.error("OrderItem object is null during mapping to OrderItemDTO.");
            return null;
        }

        logger.debug("Mapping OrderItem to OrderItemDTO: {}", orderItem);

        OrderItemDTO orderItemDTO = OrderItemDTO.builder()
                .productId(orderItem.getProductId())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .build();

        logger.debug("Mapped OrderItemDTO: {}", orderItemDTO);
        return orderItemDTO;
    }

    public static OrderItem toOrderItem(OrderItemDTO orderItemDTO) {
        if (orderItemDTO == null) {
            logger.error("OrderItemDTO object is null during mapping to OrderItem.");
            return null;
        }

        logger.debug("Mapping OrderItemDTO to OrderItem: {}", orderItemDTO);

        OrderItem orderItem = OrderItem.builder()
                .productId(orderItemDTO.getProductId())
                .quantity(orderItemDTO.getQuantity())
                .price(orderItemDTO.getPrice())
                .build();

        logger.debug("Mapped OrderItem: {}", orderItem);
        return orderItem;
    }
}