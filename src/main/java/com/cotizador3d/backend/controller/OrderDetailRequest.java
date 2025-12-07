package com.cotizador3d.backend.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailRequest {
    private Long productId;
    private Integer quantity;
    private BigDecimal subtotal;
}
