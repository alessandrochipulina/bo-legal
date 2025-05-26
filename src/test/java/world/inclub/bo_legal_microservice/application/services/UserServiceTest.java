package world.inclub.bo_legal_microservice.application.services;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import world.inclub.bo_legal_microservice.domain.User;
import world.inclub.bo_legal_microservice.domain.ports.in.UserRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Validator validator;

    // Using a real encoder as it's generally safe and tests the actual encoding/matching logic
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    private User userToCreate;
    private User existingUser;
    private String rawPassword = "password123";
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        // Initialize userService with the real encoder manually since it's not a mock
        userService = new UserService(userRepository, validator, passwordEncoder);

        userToCreate = new User();
        userToCreate.setCorreo("test@example.com");
        userToCreate.setContrasena(rawPassword);
        userToCreate.setNombre("Test User");
        userToCreate.setEstado("ACTIVO");

        encodedPassword = passwordEncoder.encode(rawPassword);

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setCorreo("existing@example.com");
        existingUser.setContrasena(encodedPassword);
        existingUser.setNombre("Existing User");
        existingUser.setEstado("ACTIVO");
    }

    // Tests for crear(User usuario)
    @Test
    void crear_success() {
        // Arrange
        when(validator.validate(any(User.class))).thenReturn(Collections.emptySet());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Simulate ID generation and return the saved user
            if (savedUser.getId() == null) {
                savedUser.setId(System.currentTimeMillis());
            }
            return Mono.just(savedUser);
        });

        // Act
        Mono<User> result = userService.crear(userToCreate);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(createdUser -> {
                    assertNotNull(createdUser.getId());
                    assertEquals(userToCreate.getCorreo(), createdUser.getCorreo());
                    assertTrue(passwordEncoder.matches(rawPassword, createdUser.getContrasena()));
                    assertNotEquals(rawPassword, createdUser.getContrasena()); // Ensure password is encoded
                    return true;
                })
                .verifyComplete();

        verify(userRepository).save(userArgumentCaptor.capture());
        assertTrue(passwordEncoder.matches(rawPassword, userArgumentCaptor.getValue().getContrasena()));
    }

    @Test
    void crear_validationFailure() {
        // Arrange
        Set<ConstraintViolation<User>> violations = new HashSet<>();
        // Mock a ConstraintViolation object
        ConstraintViolation<User> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Validation error message");
        violations.add(violation);

        when(validator.validate(any(User.class))).thenReturn(violations);

        // Act
        Mono<User> result = userService.crear(userToCreate);

        // Assert
        StepVerifier.create(result)
                .expectError(ConstraintViolationException.class)
                .verify();

        verify(userRepository, never()).save(any(User.class));
    }

    // Tests for autenticar(String correo, String contrasena)
    @Test
    void autenticar_success() {
        // Arrange
        when(userRepository.findByCorreo(existingUser.getCorreo())).thenReturn(Mono.just(existingUser));

        // Act
        Mono<User> result = userService.autenticar(existingUser.getCorreo(), rawPassword);

        // Assert
        StepVerifier.create(result)
                .expectNext(existingUser)
                .verifyComplete();
    }

    @Test
    void autenticar_failure_userNotFound() {
        // Arrange
        String nonExistentEmail = "notfound@example.com";
        when(userRepository.findByCorreo(nonExistentEmail)).thenReturn(Mono.empty());

        // Act
        Mono<User> result = userService.autenticar(nonExistentEmail, rawPassword);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class) // Assuming service throws a generic RuntimeException or specific auth error
                .verify();
    }

    @Test
    void autenticar_failure_passwordMismatch() {
        // Arrange
        when(userRepository.findByCorreo(existingUser.getCorreo())).thenReturn(Mono.just(existingUser));
        String wrongPassword = "wrongPassword";

        // Act
        Mono<User> result = userService.autenticar(existingUser.getCorreo(), wrongPassword);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class) // Assuming service throws a generic RuntimeException or specific auth error
                .verify();
    }
}
