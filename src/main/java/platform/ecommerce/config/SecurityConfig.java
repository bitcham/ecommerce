package platform.ecommerce.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import platform.ecommerce.security.JwtAuthenticationFilter;

/**
 * Spring Security configuration.
 * Implements role-based access control for API endpoints.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Public endpoints - no authentication required
    private static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/v1/products/**",
            "/api/v1/categories/**",
            "/api/v1/products/*/reviews",
            "/api/v1/products/*/reviews/statistics"
    };

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/health",
            "/actuator/info"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/members").permitAll()

                        // Admin only endpoints
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/orders").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/coupons").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/coupons").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/coupons/**").hasRole("ADMIN")

                        // Seller endpoints (create/manage products)
                        .requestMatchers(HttpMethod.POST, "/api/v1/products").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/products/**").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers("/api/v1/products/*/publish").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers("/api/v1/products/*/discontinue").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers("/api/v1/products/*/options/**").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers("/api/v1/products/*/images/**").hasAnyRole("SELLER", "ADMIN")

                        // Order management (seller actions)
                        .requestMatchers("/api/v1/orders/*/prepare").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers("/api/v1/orders/*/ship").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers("/api/v1/orders/*/deliver").hasAnyRole("SELLER", "ADMIN")

                        // Category management (admin only)
                        .requestMatchers(HttpMethod.POST, "/api/v1/categories").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/categories/*/move").hasRole("ADMIN")

                        // Payment endpoints (authenticated)
                        .requestMatchers("/api/v1/payments/**").authenticated()

                        // Member-authenticated endpoints
                        .requestMatchers("/api/v1/members/me/**").authenticated()
                        .requestMatchers("/api/v1/cart/**").authenticated()
                        .requestMatchers("/api/v1/orders/my").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders").authenticated()
                        .requestMatchers("/api/v1/orders/*/cancel").authenticated()
                        .requestMatchers("/api/v1/orders/*/pay").authenticated()
                        .requestMatchers("/api/v1/coupons/my").authenticated()
                        .requestMatchers("/api/v1/coupons/*/issue").authenticated()
                        .requestMatchers("/api/v1/coupons/issue-by-code").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/products/*/reviews").authenticated()
                        .requestMatchers("/api/v1/reviews/**").authenticated()
                        .requestMatchers("/api/v1/members/*/reviews").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
