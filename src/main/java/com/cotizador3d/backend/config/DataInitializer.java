package com.cotizador3d.backend.config;

import com.cotizador3d.backend.model.Product;
import com.cotizador3d.backend.model.Role;
import com.cotizador3d.backend.model.User;
import com.cotizador3d.backend.repository.ProductRepository;
import com.cotizador3d.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            if (!userRepository.existsByEmail("admin@test.com")) {
                User admin = User.builder()
                        .username("admin")
                        .email("admin@test.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(Role.ADMIN)
                        .build();
                userRepository.save(admin);
                System.out.println("Admin User created");
            }

            if (!userRepository.existsByEmail("cliente@test.com")) {
                User client = User.builder()
                        .username("cliente")
                        .email("cliente@test.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(Role.CLIENTE)
                        .build();
                userRepository.save(client);
                System.out.println("Client User created");
            }

            if (productRepository.count() == 0) {
                Product p1 = Product.builder()
                        .name("Resina Standard")
                        .description("Resina UV gris estándar para prototipos")
                        .price(new BigDecimal("50.00"))
                        .materialType("RESIN")
                        .imageUrl("https://example.com/resina.jpg")
                        .build();
                productRepository.save(p1);

                Product p2 = Product.builder()
                        .name("PLA Plástico")
                        .description("Filamento PLA biodegradable")
                        .price(new BigDecimal("25.00"))
                        .materialType("FDM")
                        .imageUrl("https://example.com/pla.jpg")
                        .build();
                productRepository.save(p2);
                System.out.println("Sample Products created");
            }
        };
    }
}
