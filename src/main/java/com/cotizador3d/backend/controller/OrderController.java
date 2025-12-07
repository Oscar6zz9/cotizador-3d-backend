package com.cotizador3d.backend.controller;

import com.cotizador3d.backend.model.Order;
import com.cotizador3d.backend.model.OrderDetail;
import com.cotizador3d.backend.model.Product;
import com.cotizador3d.backend.model.User;
import com.cotizador3d.backend.repository.OrderDetailRepository;
import com.cotizador3d.backend.repository.OrderRepository;
import com.cotizador3d.backend.repository.ProductRepository;
import com.cotizador3d.backend.repository.UserRepository;
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
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // Extract email/username from token

        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findAll().stream().filter(u -> u.getEmail().equals(username)).findFirst())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setCreateDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setUser(user);

        List<OrderDetail> details = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderDetailRequest item : request.getDetails()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            OrderDetail detail = new OrderDetail();
            detail.setProduct(product);
            detail.setOrder(order);
            detail.setQuantity(item.getQuantity());
            detail.setSubtotal(item.getSubtotal()); // Or calculate from product price * quantity

            details.add(detail);
            totalAmount = totalAmount.add(item.getSubtotal());
        }

        order.setTotalAmount(totalAmount);
        order.setOrderDetails(details);

        Order savedOrder = orderRepository.save(order); // Cascades should handle details saving if configured, but
                                                        // let's be safe
        // If CascadeType.ALL is set on OneToMany, saving Order saves details.

        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findAll().stream().filter(u -> u.getEmail().equals(username)).findFirst())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(orderRepository.findByUserId(user.getId()));
    }
}
