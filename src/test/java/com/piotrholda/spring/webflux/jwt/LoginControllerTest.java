package com.piotrholda.spring.webflux.jwt;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void shouldLogin() {

        // given
        LoginRequest loginRequest = new LoginRequest("adamk", "password");

        // when
        LoginResponse response = webTestClient.post()
                .uri("/login")
                .body(BodyInserters.fromValue(loginRequest))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponse.class)
                .returnResult()
                .getResponseBody();

        // then
        assertThat(response).isNotNull();
        assertThat(response.token()).isNotBlank();
    }

    @Test
    void shouldNotLoginWithWrongPassword() {

        // given
        LoginRequest loginRequest = new LoginRequest("adamk", "wrong password");

        // when / then
        webTestClient.post()
                .uri("/login")
                .body(BodyInserters.fromValue(loginRequest))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
