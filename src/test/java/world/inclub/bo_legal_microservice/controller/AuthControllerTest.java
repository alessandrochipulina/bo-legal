package world.inclub.bo_legal_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.application.services.UserService;
import world.inclub.bo_legal_microservice.domain.User;
import world.inclub.bo_legal_microservice.domain.dto.LoginRequest;
import world.inclub.bo_legal_microservice.domain.dto.TokenResponse;
import world.inclub.bo_legal_microservice.security.JwtUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    private User testUser;
    private LoginRequest loginRequest;
    private String dummyToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setCorreo("test@example.com");
        testUser.setContrasena("hashedPassword"); // Should be hashed in real scenario
        testUser.setNombre("Test User");
        testUser.setEstado("ACTIVO");

        loginRequest = new LoginRequest();
        loginRequest.setCorreo("test@example.com");
        loginRequest.setContrasena("password123");

        dummyToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIn0.FakeToken";
    }

    // Tests for POST /api/v1/auth/login
    @Test
    void login_whenSuccessfulAuthentication_shouldReturnToken() {
        // Arrange
        when(userService.autenticar(loginRequest.getCorreo(), loginRequest.getContrasena()))
                .thenReturn(Mono.just(testUser));
        when(jwtUtil.generarToken(testUser)).thenReturn(dummyToken);

        // Act & Assert
        webTestClient.post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(loginRequest), LoginRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .value(tokenResponse -> {
                    assertThat(tokenResponse.getToken()).isEqualTo(dummyToken);
                });
    }

    @Test
    void login_whenInvalidCredentials_shouldReturnUnauthorized() {
        // Arrange
        when(userService.autenticar(loginRequest.getCorreo(), loginRequest.getContrasena()))
                .thenReturn(Mono.empty()); // Simulate user not found or password mismatch

        // Act & Assert
        webTestClient.post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(loginRequest), LoginRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void login_whenAuthenticationServiceError_shouldReturnUnauthorized() {
        // Arrange
        when(userService.autenticar(loginRequest.getCorreo(), loginRequest.getContrasena()))
                .thenReturn(Mono.error(new RuntimeException("Service internal error")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(loginRequest), LoginRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized(); // As per controller's onErrorResume
    }

    // Placeholder for /api/v1/auth/user tests

    // Tests for POST /api/v1/auth/user
    @Test
    void createUser_whenSuccessful_shouldReturnUserWithNullPassword() {
        // Arrange
        User userToRegister = new User();
        userToRegister.setCorreo("newuser@example.com");
        userToRegister.setContrasena("newPassword123");
        userToRegister.setNombre("New User");
        userToRegister.setEstado("ACTIVO");

        User createdUserFromService = new User();
        createdUserFromService.setId(2L);
        createdUserFromService.setCorreo(userToRegister.getCorreo());
        createdUserFromService.setContrasena("hashedNewPassword"); // Service would hash it
        createdUserFromService.setNombre(userToRegister.getNombre());
        createdUserFromService.setEstado(userToRegister.getEstado());

        when(userService.crear(any(User.class))).thenReturn(Mono.just(createdUserFromService));

        // Act & Assert
        webTestClient.post().uri("/api/v1/auth/user")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(userToRegister), User.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(returnedUser -> {
                    assertThat(returnedUser.getId()).isEqualTo(createdUserFromService.getId());
                    assertThat(returnedUser.getCorreo()).isEqualTo(createdUserFromService.getCorreo());
                    assertThat(returnedUser.getNombre()).isEqualTo(createdUserFromService.getNombre());
                    assertThat(returnedUser.getEstado()).isEqualTo(createdUserFromService.getEstado());
                    assertThat(returnedUser.getContrasena()).isNull(); // Password should be nullified
                });
    }

    @Test
    void createUser_whenServiceFails_shouldReturnErrorResponse() {
        // Arrange
        User userToRegister = new User();
        userToRegister.setCorreo("failuser@example.com");
        userToRegister.setContrasena("failPassword123");
        userToRegister.setNombre("Fail User");

        // Simulate a service error, e.g., ConstraintViolationException or other RuntimeException
        when(userService.crear(any(User.class)))
                .thenReturn(Mono.error(new RuntimeException("Service error during user creation")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/auth/user")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(userToRegister), User.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError() // Or 400 if a specific exception handler maps it
                .expectBody()
                .jsonPath("$.message").isEqualTo("Error al crear usuario: Service error during user creation") // Assuming ApiResponse structure for errors
                .jsonPath("$.data").doesNotExist();
    }
    
    @Test
    void createUser_whenServiceValidationFails_shouldReturnBadRequest() {
        // Arrange
        User userToRegister = new User(); // Potentially invalid user (e.g. missing fields)
        userToRegister.setCorreo("invalid@example.com");
        // No password, or other fields that might cause validation error in service

        // Simulate a ConstraintViolationException from the service layer
        // In a real scenario, this would be a Set of ConstraintViolation objects
        javax.validation.ConstraintViolationException mockViolationException =
            new javax.validation.ConstraintViolationException("Validation failed", new java.util.HashSet<>());

        when(userService.crear(any(User.class)))
            .thenReturn(Mono.error(mockViolationException));

        // Act & Assert
        webTestClient.post().uri("/api/v1/auth/user")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(userToRegister), User.class)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest() // Expecting 400 due to ConstraintViolationException
            .expectBody()
            .jsonPath("$.message").isEqualTo("Error al crear usuario: Validation failed")
            .jsonPath("$.data").doesNotExist();
    }
}
