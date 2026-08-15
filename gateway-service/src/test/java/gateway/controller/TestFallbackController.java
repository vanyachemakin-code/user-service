package gateway.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.config.import=optional:configserver:",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",

        "spring.cloud.gateway.routes[0].id=user-service",
        "spring.cloud.gateway.routes[0].uri=http://localhost:9999",
        "spring.cloud.gateway.routes[0].predicates[0]=Path=/api/v1/user-service/**",
        "spring.cloud.gateway.routes[0].filters[0].name=CircuitBreaker",
        "spring.cloud.gateway.routes[0].filters[0].args.name=userServiceCircuitBreaker",
        "spring.cloud.gateway.routes[0].filters[0].args.fallbackUri=forward:/fallback/user-service",

        "resilience4j.circuitbreaker.instances.userServiceCircuitBreaker.sliding-window-size=5",
        "resilience4j.circuitbreaker.instances.userServiceCircuitBreaker.failure-rate-threshold=50",
        "resilience4j.circuitbreaker.instances.userServiceCircuitBreaker.wait-duration-in-open-state=1s",
        "resilience4j.circuitbreaker.instances.userServiceCircuitBreaker.permitted-number-of-calls-in-half-open-state=3"
})
public class TestFallbackController {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Вызов Fallback когда User Service недоступен")
    public void shouldReturnUserServiceFallbackWhenServiceIsDown() {
        webTestClient.get()
                .uri("/api/v1/user-service/user/list")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(String.class)
                .isEqualTo("User Service временно недоступен. Попробуйте позже.");
    }

    @Test
    @DisplayName("Тест эндпоинта Fallback")
    public void shouldReturnDirectFallbackResponse() {
        webTestClient.get()
                .uri("/fallback/user-service")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(String.class)
                .isEqualTo("User Service временно недоступен. Попробуйте позже.");
    }
}