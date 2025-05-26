package world.inclub.bo_legal_microservice.application.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import world.inclub.bo_legal_microservice.domain.Ciclo;
import world.inclub.bo_legal_microservice.domain.ports.in.CicloRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CicloServiceTest {

    @Mock
    private CicloRepository cicloRepository;

    @InjectMocks
    private CicloService cicloService;

    private Ciclo ciclo1;
    private Ciclo ciclo2;

    @BeforeEach
    void setUp() {
        ciclo1 = new Ciclo();
        ciclo1.setId(1L);
        ciclo1.setNombre("Ciclo 1");
        ciclo1.setEstado("ACTIVO");

        ciclo2 = new Ciclo();
        ciclo2.setId(2L);
        ciclo2.setNombre("Ciclo 2");
        ciclo2.setEstado("INACTIVO");
    }

    @Test
    void findAll() {
        // Arrange
        when(cicloRepository.findAll()).thenReturn(Flux.just(ciclo1, ciclo2));

        // Act
        Flux<Ciclo> result = cicloService.findAll();

        // Assert
        StepVerifier.create(result)
                .expectNext(ciclo1)
                .expectNext(ciclo2)
                .verifyComplete();
    }

    @Test
    void findById_whenFound() {
        // Arrange
        when(cicloRepository.findById(1L)).thenReturn(Mono.just(ciclo1));

        // Act
        Mono<Ciclo> result = cicloService.findById(1L);

        // Assert
        StepVerifier.create(result)
                .expectNext(ciclo1)
                .verifyComplete();
    }

    @Test
    void findById_whenNotFound() {
        // Arrange
        when(cicloRepository.findById(3L)).thenReturn(Mono.empty());

        // Act
        Mono<Ciclo> result = cicloService.findById(3L);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void save() {
        // Arrange
        when(cicloRepository.save(any(Ciclo.class))).thenReturn(Mono.just(ciclo1));

        // Act
        Mono<Ciclo> result = cicloService.save(ciclo1);

        // Assert
        StepVerifier.create(result)
                .expectNext(ciclo1)
                .verifyComplete();
    }

    @Test
    void deleteById_whenFound() {
        // Arrange
        when(cicloRepository.findById(1L)).thenReturn(Mono.just(ciclo1));
        when(cicloRepository.deleteById(1L)).thenReturn(Mono.empty());


        // Act
        Mono<Ciclo> result = cicloService.deleteById(1L);

        // Assert
        StepVerifier.create(result)
                .expectNext(ciclo1)
                .verifyComplete();
    }

    @Test
    void deleteById_whenNotFound() {
        // Arrange
        when(cicloRepository.findById(3L)).thenReturn(Mono.empty());

        // Act
        Mono<Ciclo> result = cicloService.deleteById(3L);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class) // Assuming it throws RuntimeException when not found
                .verify();
    }

    @Test
    void update_whenFound() {
        // Arrange
        Ciclo updatedCiclo = new Ciclo();
        updatedCiclo.setId(1L);
        updatedCiclo.setNombre("Ciclo Actualizado");
        updatedCiclo.setEstado("ACTIVO");

        when(cicloRepository.findById(1L)).thenReturn(Mono.just(ciclo1));
        when(cicloRepository.save(any(Ciclo.class))).thenReturn(Mono.just(updatedCiclo));

        // Act
        Mono<Ciclo> result = cicloService.update(1L, updatedCiclo);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(ciclo -> ciclo.getNombre().equals("Ciclo Actualizado"))
                .verifyComplete();
    }

    @Test
    void update_whenNotFound() {
        // Arrange
        Ciclo updatedCiclo = new Ciclo();
        updatedCiclo.setId(3L);
        updatedCiclo.setNombre("Ciclo No Existente");
        updatedCiclo.setEstado("ACTIVO");

        when(cicloRepository.findById(3L)).thenReturn(Mono.empty());

        // Act
        Mono<Ciclo> result = cicloService.update(3L, updatedCiclo);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class) // Assuming it throws RuntimeException when not found
                .verify();
    }
}
