package gateway.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
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