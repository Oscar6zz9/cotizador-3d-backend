package com.cotizador3d.backend.controller;

import com.cotizador3d.backend.model.Order;
import com.cotizador3d.backend.model.OrderDetail;
import com.cotizador3d.backend.model.Product;
import com.cotizador3d.backend.model.User;
import com.cotizador3d.backend.repository.OrderRepository;
import com.cotizador3d.backend.repository.ProductRepository;
import com.cotizador3d.backend.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // 1. CAMBIO DE URL: Quitamos "/create" para que coincida con el Frontend
    @PostMapping
    @Transactional
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequestDto request) {

        // 2. OBTENER USUARIO AUTENTICADO
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findAll().stream().filter(u -> u.getEmail().equals(username)).findFirst())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la sesión"));

        // 3. CREAR LA ORDEN
        Order order = new Order();
        order.setCreateDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setUser(user);

        List<OrderDetail> details = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 4. PROCESAR DETALLES (Mapeo Inteligente)
        for (OrderDetailDto item : request.getOrderDetails()) {

            // Lógica: El frontend manda texto ("Resina...", "Plástico...").
            // Buscamos un producto en la BD que coincida, o usamos el primero por defecto.
            String materialName = item.getMaterial() != null ? item.getMaterial() : "";

            Product product = productRepository.findAll().stream()
                    .filter(p -> materialName.toLowerCase().contains(p.getName().toLowerCase()) ||
                            p.getMaterialType().toLowerCase().contains(materialName.toLowerCase()))
                    .findFirst()
                    .orElse(productRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new RuntimeException(
                                    "No hay productos base en la BD. Ejecuta el DataSeeder.")));

            OrderDetail detail = new OrderDetail();
            detail.setProduct(product);
            detail.setOrder(order);
            // Como es impresión 3D a medida, asumimos cantidad 1 por ítem del array
            detail.setQuantity(1);
            detail.setSubtotal(item.getSubtotal());

            details.add(detail);
            totalAmount = totalAmount.add(item.getSubtotal());
        }

        order.setTotalAmount(totalAmount);
        order.setOrderDetails(details);

        // Guardar
        Order savedOrder = orderRepository.save(order);

        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findAll().stream().filter(u -> u.getEmail().equals(username)).findFirst())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(orderRepository.findByUserId(user.getId()));
    }

    // --- DTOs INTERNOS (Para mapear el JSON del Frontend) ---
    @Data
    public static class OrderRequestDto {
        private BigDecimal totalAmount;
        private String status;
        private List<OrderDetailDto> orderDetails;
    }

    @Data
    public static class OrderDetailDto {
        private String productName;
        private String material;
        private String dimensions;
        private BigDecimal subtotal;
    }
}