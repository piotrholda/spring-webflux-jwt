package com.piotrholda.spring.webflux.jwt;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProfileControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void shouldReturnProfileData() {

        // given
        LoginRequest loginRequest = new LoginRequest("adamk", "password");
        LoginResponse loginResponse = webTestClient.post()
                .uri("/login")
                .body(BodyInserters.fromValue(loginRequest))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponse.class)
                .returnResult()
                .getResponseBody();
        String token = loginResponse.token();

        // when
        ProfileResponse response = webTestClient.get()
                .uri("/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProfileResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.username()).isEqualTo("adamk");
        assertThat(response.roles()).containsExactly("USER");
    }

    @Test
    void shouldReturnUnauthorizedWhenExpiredToken() {

        // when / then
        webTestClient.get()
                .uri("/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZGFtayIsImlhdCI6MTcwNTE2MjU0OCwiZXhwIjoxNzA1MTY0MzQ4fQ.2Mn_R5Ia8iUyO660j7iV1KZnMicuSaVCMbpP9Ah9G3g")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnUnauthorizedWhenInvalidToken() {

        // when / then
        webTestClient.get()
                .uri("/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "invalid token")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
