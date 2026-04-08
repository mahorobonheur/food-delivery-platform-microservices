package com.food.delivery.api_gateway.config;

import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GatewayConfig {

    @Bean
    public RouterFunction<ServerResponse> customerServiceRoute() {
        return GatewayRouterFunctions.route("customer-service")
                .route(RequestPredicates.path("/api/customers/**"),
                        HandlerFunctions.http())
                .route(RequestPredicates.path("/api/role/**"),
                        HandlerFunctions.http())
                .filter(LoadBalancerFilterFunctions.lb("customer-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> restaurantServiceRoute() {
        return GatewayRouterFunctions.route("restaurant-service")
                .route(RequestPredicates.path("/api/restaurants/**"),
                 HandlerFunctions.http())
                .filter(LoadBalancerFilterFunctions.lb("restaurant-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute(){
        return GatewayRouterFunctions.route("order-service")
                .route(RequestPredicates.path("/api/orders/**"),
                        HandlerFunctions.http())
                .filter(LoadBalancerFilterFunctions.lb("order-service"))
                .build();
    }

    @Bean RouterFunction<ServerResponse> deliveryServiceRoute(){
        return GatewayRouterFunctions.route("delivery-service")
                .route(RequestPredicates.path("/api/deliveries/**"),
                        HandlerFunctions.http())
                .filter(LoadBalancerFilterFunctions.lb("delivery-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> customerServiceDocsRoute() {
        return GatewayRouterFunctions.route("customer-service-docs")
                .route(RequestPredicates.path("/customer-service/v3/api-docs"),
                        HandlerFunctions.http())
                .before(BeforeFilterFunctions.stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("customer-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> restaurantServiceDocsRoute() {
        return GatewayRouterFunctions.route("restaurant-service-docs")
                .route(RequestPredicates.path("/restaurant-service/v3/api-docs"),
                        HandlerFunctions.http())
                .before(BeforeFilterFunctions.stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("restaurant-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceDocsRoute() {
        return GatewayRouterFunctions.route("order-service-docs")
                .route(RequestPredicates.path("/order-service/v3/api-docs"),
                        HandlerFunctions.http())
                .before(BeforeFilterFunctions.stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("order-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> deliveryServiceDocsRoute() {
        return GatewayRouterFunctions.route("delivery-service-docs")
                .route(RequestPredicates.path("/delivery-service/v3/api-docs"),
                        HandlerFunctions.http())
                .before(BeforeFilterFunctions.stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("delivery-service"))
                .build();
    }
}