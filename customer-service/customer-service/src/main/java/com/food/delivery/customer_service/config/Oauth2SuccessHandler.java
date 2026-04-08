package com.food.delivery.customer_service.config;

import com.food.delivery.customer_service.model.Customer;
import com.food.delivery.customer_service.model.Role;
import com.food.delivery.customer_service.repository.CustomerRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {
    private CustomerRepository customerRepository;
    private JwtUtil jwtUtil;

    public Oauth2SuccessHandler(CustomerRepository customerRepository, JwtUtil jwtUtil) {
        this.customerRepository = customerRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("first_name");
        String lastName = oAuth2User.getAttribute("last_name");


        Customer customer = customerRepository.findByEmail(email)
                .orElseGet(() -> {
                    Customer u = new Customer();
                    u.setEmail(email);
                    u.setFirstName(firstName);
                    u.setLastName(lastName);
                    u.setPassword("OAUTH2_USER");
                    u.setRole(Role.builder().roleName("CUSTOMER").build());
                    u.setCreatedAt(LocalDateTime.now());
                    return customerRepository.save(u);
                });

                 String token = jwtUtil.generateToken(customer);
                 response.sendRedirect( "http://localhost:8085/oauth2/success?token=" + token);
           }

}
