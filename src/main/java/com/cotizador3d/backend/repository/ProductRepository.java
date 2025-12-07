package com.cotizador3d.backend.repository;

import com.cotizador3d.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
