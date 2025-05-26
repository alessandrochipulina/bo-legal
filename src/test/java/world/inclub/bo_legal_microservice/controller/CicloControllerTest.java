package world.inclub.bo_legal_microservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.domain.Ciclo;
import world.inclub.bo_legal_microservice.application.services.CicloService;
import world.inclub.bo_legal_microservice.helpers.ApiResponse;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;


@WebFluxTest(CicloController.class)
class CicloControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CicloService cicloService;

    private Ciclo ciclo1;
    private Ciclo ciclo2;

    @BeforeEach
    void setUp() {
        ciclo1 = new Ciclo();
        ciclo1.setId(1L);
        ciclo1.setNombre("Ciclo Test 1");
        ciclo1.setEstado("ACTIVO");

        ciclo2 = new Ciclo();
        ciclo2.setId(2L);
        ciclo2.setNombre("Ciclo Test 2");
        ciclo2.setEstado("INACTIVO");
    }

    // Tests for GET /api/v1/ciclo/all
    @Test
    void getAllCiclos_whenServiceReturnsCiclos_shouldReturnOkAndCicloList() {
        // Arrange
        when(cicloService.findAll()).thenReturn(Flux.just(ciclo1, ciclo2));

        // Act & Assert
        webTestClient.get().uri("/api/v1/ciclo/all")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getData()).isNotNull();
                    assertThat(apiResponse.getMessage()).isEqualTo("All cycles fetched successfully.");
                    // Further checks on the data list can be done by casting apiResponse.getData() to List<Map> or similar
                    List<?> dataList = (List<?>) apiResponse.getData();
                    assertThat(dataList).hasSize(2);
                });
    }

    @Test
    void getAllCiclos_whenServiceReturnsEmpty_shouldReturnOkAndEmptyList() {
        // Arrange
        when(cicloService.findAll()).thenReturn(Flux.empty());

        // Act & Assert
        webTestClient.get().uri("/api/v1/ciclo/all")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getData()).isNotNull(); // Controller initializes data to empty list
                    assertThat(apiResponse.getMessage()).isEqualTo("All cycles fetched successfully.");
                    List<?> dataList = (List<?>) apiResponse.getData();
                    assertThat(dataList).isEmpty();
                });
    }

    @Test
    void getAllCiclos_whenServiceFails_shouldReturnErrorResponse() {
        // Arrange
        when(cicloService.findAll()).thenReturn(Flux.error(new RuntimeException("Service error")));

        // Act & Assert
        webTestClient.get().uri("/api/v1/ciclo/all")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError() // Assuming controller's onErrorResume maps to 500 for general errors
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getData()).isNull(); // Or an empty list, depending on error handling
                    assertThat(apiResponse.getMessage()).contains("Error fetching cycles: Service error");
                });
    }

    // Placeholder for other tests

    // Tests for GET /api/v1/ciclo/{Id}
    @Test
    void getCicloById_whenFound_shouldReturnOkAndCiclo() {
        // Arrange
        when(cicloService.findById(ciclo1.getId())).thenReturn(Mono.just(ciclo1));

        // Act & Assert
        webTestClient.get().uri("/api/v1/ciclo/{Id}", ciclo1.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getData()).isNotNull();
                    assertThat(apiResponse.getMessage()).isEqualTo("Cycle fetched successfully.");
                    // To assert the data, it needs to be cast from Object to Map or directly to Ciclo if WebTestClient is configured for that
                    // For simplicity, checking for non-null data and specific fields if cast to Map
                    java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) apiResponse.getData();
                    assertThat(dataMap.get("id")).isEqualTo(ciclo1.getId().intValue()); // JSON numbers might be Integers
                    assertThat(dataMap.get("nombre")).isEqualTo(ciclo1.getNombre());
                });
    }

    @Test
    void getCicloById_whenNotFound_shouldReturnError() {
        // Arrange
        Long nonExistentId = 99L;
        when(cicloService.findById(nonExistentId)).thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.get().uri("/api/v1/ciclo/{Id}", nonExistentId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk() // Controller's logic turns empty Mono into a success response with specific message
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getData()).isNull();
                    assertThat(apiResponse.getMessage()).isEqualTo("Cycle not found with id: " + nonExistentId);
                });
    }

    @Test
    void getCicloById_whenServiceFails_shouldReturnErrorResponse() {
        // Arrange
        Long testId = 3L;
        when(cicloService.findById(testId)).thenReturn(Mono.error(new RuntimeException("Service error for findById")));

        // Act & Assert
        webTestClient.get().uri("/api/v1/ciclo/{Id}", testId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getData()).isNull();
                    assertThat(apiResponse.getMessage()).isEqualTo("Error fetching cycle with id " + testId + ": Service error for findById");
                });
    }

    // Tests for POST /api/v1/ciclo/add
    @Test
    void addCiclo_whenValidInput_shouldReturnOkAndSavedCiclo() {
        // Arrange
        Ciclo newCiclo = new Ciclo();
        newCiclo.setNombre("New Ciclo");
        newCiclo.setEstado("PENDIENTE");

        Ciclo savedCiclo = new Ciclo();
        savedCiclo.setId(3L);
        savedCiclo.setNombre(newCiclo.getNombre());
        savedCiclo.setEstado(newCiclo.getEstado());

        when(cicloService.save(any(Ciclo.class))).thenReturn(Mono.just(savedCiclo));

        // Act & Assert
        webTestClient.post().uri("/api/v1/ciclo/add")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newCiclo), Ciclo.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getData()).isNotNull();
                    assertThat(apiResponse.getMessage()).isEqualTo("Cycle added successfully.");
                    java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) apiResponse.getData();
                    assertThat(dataMap.get("id")).isEqualTo(savedCiclo.getId().intValue());
                    assertThat(dataMap.get("nombre")).isEqualTo(savedCiclo.getNombre());
                });
    }

    @Test
    void addCiclo_whenServiceSaveFails_shouldReturnErrorResponse() {
        // Arrange
        Ciclo newCiclo = new Ciclo();
        newCiclo.setNombre("Failing Ciclo");
        newCiclo.setEstado("ACTIVO");

        when(cicloService.save(any(Ciclo.class))).thenReturn(Mono.error(new RuntimeException("Service save error")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/ciclo/add")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newCiclo), Ciclo.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getData()).isNull();
                    assertThat(apiResponse.getMessage()).isEqualTo("Error adding cycle: Service save error");
                });
    }

    @Test
    void addCiclo_whenInputIsConsideredInvalidByService_shouldReturnError() {
        // Arrange: Simulate service throwing an error for "invalid" input
        // (e.g., if nombre is null, which is a common validation)
        Ciclo invalidCiclo = new Ciclo(); // Intentionally missing 'nombre' or other required fields
        invalidCiclo.setEstado("ACTIVO");

        // Simulate service layer validation failure
        when(cicloService.save(any(Ciclo.class))).thenReturn(Mono.error(new IllegalArgumentException("Nombre cannot be null")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/ciclo/add")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(invalidCiclo), Ciclo.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError() // Or 4xx if the controller specifically catches IllegalArgumentException
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getData()).isNull();
                    assertThat(apiResponse.getMessage()).isEqualTo("Error adding cycle: Nombre cannot be null");
                });
    }

    // Tests for POST /api/v1/ciclo/edit/{Id}
    @Test
    void editCiclo_whenValidInputAndFound_shouldReturnOkAndUpdatedCiclo() {
        // Arrange
        Long existingId = ciclo1.getId();
        Ciclo cicloToUpdate = new Ciclo();
        cicloToUpdate.setNombre("Updated Ciclo Name");
        cicloToUpdate.setEstado("ACTIVO_MODIFICADO");

        Ciclo updatedCicloFromService = new Ciclo();
        updatedCicloFromService.setId(existingId);
        updatedCicloFromService.setNombre(cicloToUpdate.getNombre());
        updatedCicloFromService.setEstado(cicloToUpdate.getEstado());

        when(cicloService.update(anyLong(), any(Ciclo.class))).thenReturn(Mono.just(updatedCicloFromService));

        // Act & Assert
        webTestClient.post().uri("/api/v1/ciclo/edit/{Id}", existingId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(cicloToUpdate), Ciclo.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getData()).isNotNull();
                    assertThat(apiResponse.getMessage()).isEqualTo("Cycle updated successfully.");
                    java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) apiResponse.getData();
                    assertThat(dataMap.get("id")).isEqualTo(existingId.intValue());
                    assertThat(dataMap.get("nombre")).isEqualTo(updatedCicloFromService.getNombre());
                    assertThat(dataMap.get("estado")).isEqualTo(updatedCicloFromService.getEstado());
                });
    }

    @Test
    void editCiclo_whenNotFound_shouldReturnError() {
        // Arrange
        Long nonExistentId = 99L;
        Ciclo cicloToUpdate = new Ciclo();
        cicloToUpdate.setNombre("Updated Ciclo Name");

        when(cicloService.update(anyLong(), any(Ciclo.class))).thenReturn(Mono.empty()); // Simulate service returning empty for not found

        // Act & Assert
        webTestClient.post().uri("/api/v1/ciclo/edit/{Id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(cicloToUpdate), Ciclo.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk() // Controller maps empty to a success response with specific message
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getData()).isNull();
                    assertThat(apiResponse.getMessage()).isEqualTo("Cycle not found with id: " + nonExistentId + " for update");
                });
    }

    @Test
    void editCiclo_whenServiceUpdateFails_shouldReturnErrorResponse() {
        // Arrange
        Long existingId = ciclo1.getId();
        Ciclo cicloToUpdate = new Ciclo();
        cicloToUpdate.setNombre("Failing Update Ciclo");

        when(cicloService.update(anyLong(), any(Ciclo.class))).thenReturn(Mono.error(new RuntimeException("Service update error")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/ciclo/edit/{Id}", existingId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(cicloToUpdate), Ciclo.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getData()).isNull();
                    assertThat(apiResponse.getMessage()).isEqualTo("Error updating cycle with id " + existingId + ": Service update error");
                });
    }
    
    @Test
    void editCiclo_whenInputIsConsideredInvalidByService_shouldReturnError() {
        // Arrange
        Long existingId = ciclo1.getId();
        Ciclo invalidCicloToUpdate = new Ciclo(); // Missing 'nombre'
        invalidCicloToUpdate.setEstado("SOME_STATE");

        when(cicloService.update(anyLong(), any(Ciclo.class)))
            .thenReturn(Mono.error(new IllegalArgumentException("Nombre cannot be null for update")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/ciclo/edit/{Id}", existingId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(invalidCicloToUpdate), Ciclo.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError() // Or 4xx if controller maps IllegalArgumentException specifically
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getData()).isNull();
                    assertThat(apiResponse.getMessage()).isEqualTo("Error updating cycle with id " + existingId + ": Nombre cannot be null for update");
                });
    }

    // Tests for POST /api/v1/ciclo/delete/{Id}
    @Test
    void deleteCiclo_whenExistsAndDeletionSuccessful_shouldReturnOk() {
        // Arrange
        Long existingId = ciclo1.getId();
        // Simulate deleteById returning the object that was asked to be deleted
        when(cicloService.deleteById(existingId)).thenReturn(Mono.just(ciclo1));
        // Simulate findById after deletion returning empty, confirming deletion
        when(cicloService.findById(existingId)).thenReturn(Mono.empty());


        // Act & Assert
        webTestClient.post().uri("/api/v1/ciclo/delete/{Id}", existingId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getData()).isNull(); // Or specific representation of deleted ID. Controller sets to null.
                    assertThat(apiResponse.getMessage()).isEqualTo("Cycle with id " + existingId + " deleted successfully.");
                });
    }
    
    @Test
    void deleteCiclo_whenServiceReturnsEmptyOnDeleteById_andFindByIdConfirms_shouldReturnOk() {
        // This covers cases where deleteById might return Mono.empty() if the resource was already gone
        // or if the service's deleteById method has a void return type effectively (Mono<Void> then Mono.empty())
        Long existingId = ciclo1.getId();
        when(cicloService.deleteById(existingId)).thenReturn(Mono.empty()); // deleteById itself returns empty
        when(cicloService.findById(existingId)).thenReturn(Mono.empty()); // findById confirms it's gone

        // Act & Assert
        webTestClient.post().uri("/api/v1/ciclo/delete/{Id}", existingId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(ApiResponse.class)
            .value(apiResponse -> {
                assertThat(apiResponse.getData()).isNull();
                assertThat(apiResponse.getMessage()).isEqualTo("Cycle with id " + existingId + " deleted successfully.");
            });
    }


    @Test
    void deleteCiclo_whenServiceDeleteFails_shouldReturnErrorResponse() {
        // Arrange
        Long existingId = ciclo1.getId();
        when(cicloService.deleteById(existingId)).thenReturn(Mono.error(new RuntimeException("Service delete error")));
        // findById mock is not strictly necessary here as deleteById fails first, but good for completeness
        lenient().when(cicloService.findById(existingId)).thenReturn(Mono.empty());


        // Act & Assert
        webTestClient.post().uri("/api/v1/ciclo/delete/{Id}", existingId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getData()).isNull();
                    assertThat(apiResponse.getMessage()).isEqualTo("Error deleting cycle with id " + existingId + ": Service delete error");
                });
    }

    @Test
    void deleteCiclo_whenConfirmationFindByIdFails_shouldReturnErrorResponse() {
        // Arrange
        Long existingId = ciclo1.getId();
        when(cicloService.deleteById(existingId)).thenReturn(Mono.just(ciclo1)); // Delete itself is fine
        when(cicloService.findById(existingId)).thenReturn(Mono.error(new RuntimeException("Confirmation findById error"))); // Confirmation fails

        // Act & Assert
        webTestClient.post().uri("/api/v1/ciclo/delete/{Id}", existingId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getData()).isNull();
                    assertThat(apiResponse.getMessage()).isEqualTo("Error deleting cycle with id " + existingId + ": Confirmation findById error");
                });
    }
    
    @Test
    void deleteCiclo_whenResourceStillFoundAfterDeleteAttempt_shouldReturnError() {
        // Arrange
        Long existingId = ciclo1.getId();
        // DeleteById returns the object, suggesting it *was* there
        when(cicloService.deleteById(existingId)).thenReturn(Mono.just(ciclo1));
        // But findById *still* returns it, indicating deletion didn't effectively happen or cache wasn't cleared
        when(cicloService.findById(existingId)).thenReturn(Mono.just(ciclo1));

        // Act & Assert
        webTestClient.post().uri("/api/v1/ciclo/delete/{Id}", existingId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk() // Controller logic considers this a failed delete, but returns OK status with error message
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    // The data might contain the ciclo that was "deleted" but still found
                    // assertThat(apiResponse.getData()).isNotNull(); 
                    assertThat(apiResponse.getData()).isNull(); // As per current controller code, it sets data to null on this path.
                    assertThat(apiResponse.getMessage()).isEqualTo("Cycle with id " + existingId + " could not be deleted or was not found initially.");
                });
    }
}
