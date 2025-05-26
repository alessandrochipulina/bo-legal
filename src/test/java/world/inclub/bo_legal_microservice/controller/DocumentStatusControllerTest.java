package world.inclub.bo_legal_microservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.application.services.DocumentStatusService;
import world.inclub.bo_legal_microservice.domain.DocumentStatus;
import world.inclub.bo_legal_microservice.domain.requests.StatusRequest;
import world.inclub.bo_legal_microservice.helpers.ApiResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(DocumentStatusController.class)
class DocumentStatusControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DocumentStatusService documentStatusService;

    private DocumentStatus documentStatus1;
    private DocumentStatus documentStatus2;
    private StatusRequest validStatusRequest;
    private StatusRequest invalidStatusRequest;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        documentStatus1 = new DocumentStatus();
        documentStatus1.setId(1L);
        documentStatus1.setDocumentTypeId(1);
        documentStatus1.setFromStatusId(1);
        documentStatus1.setToStatusId(2);
        documentStatus1.setOrder(1);
        documentStatus1.setStatus("A");
        documentStatus1.setCreationDate(LocalDateTime.now());
        documentStatus1.setModificationDate(LocalDateTime.now());

        documentStatus2 = new DocumentStatus();
        documentStatus2.setId(2L);
        documentStatus2.setDocumentTypeId(1);
        documentStatus2.setFromStatusId(2);
        documentStatus2.setToStatusId(3);
        documentStatus2.setOrder(2);
        documentStatus2.setStatus("A");
        documentStatus2.setCreationDate(LocalDateTime.now());
        documentStatus2.setModificationDate(LocalDateTime.now());

        validStatusRequest = new StatusRequest();
        validStatusRequest.setDocumentTypeId(1);
        validStatusRequest.setFromStatusId(3);
        validStatusRequest.setToStatusId(4);
        validStatusRequest.setOrder(3);
        validStatusRequest.setStatus("A");

        invalidStatusRequest = new StatusRequest(); // Missing required fields for validation
    }

    private Map<String, Object> convertToMap(Object obj) {
        return objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
    }

    // Tests for GET /api/v1/document/status/all
    @Test
    void getAllDocumentStatuses_whenServiceReturnsStatuses_shouldReturnOkAndStatusList() {
        // Arrange
        when(documentStatusService.findAll()).thenReturn(Flux.just(documentStatus1, documentStatus2));

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/status/all")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Document statuses fetched successfully.");
                    assertThat(apiResponse.getData()).isNotNull();
                    List<Map<String, Object>> dataList = objectMapper.convertValue(apiResponse.getData(), new TypeReference<List<Map<String, Object>>>() {});
                    assertThat(dataList).hasSize(2);
                    assertThat(dataList.get(0).get("order")).isEqualTo(documentStatus1.getOrder());
                    assertThat(dataList.get(1).get("fromStatusId")).isEqualTo(documentStatus2.getFromStatusId());
                });
    }

    @Test
    void getAllDocumentStatuses_whenServiceReturnsEmpty_shouldReturnOkAndEmptyList() {
        // Arrange
        when(documentStatusService.findAll()).thenReturn(Flux.empty());

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/status/all")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Document statuses fetched successfully.");
                    assertThat(apiResponse.getData()).isInstanceOf(List.class);
                    List<?> dataList = (List<?>) apiResponse.getData();
                    assertThat(dataList).isEmpty();
                });
    }

    @Test
    void getAllDocumentStatuses_whenServiceFails_shouldReturnErrorResponse() {
        // Arrange
        when(documentStatusService.findAll()).thenReturn(Flux.error(new RuntimeException("Service findAll error")));

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/status/all")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error fetching document statuses: Service findAll error");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    // Placeholder for /add tests

    // Tests for POST /api/v1/document/status/add
    @Test
    void addDocumentStatus_whenValidRequest_shouldReturnOkAndSavedStatus() {
        // Arrange
        DocumentStatus savedStatus = new DocumentStatus();
        savedStatus.setId(3L);
        savedStatus.setDocumentTypeId(validStatusRequest.getDocumentTypeId());
        savedStatus.setFromStatusId(validStatusRequest.getFromStatusId());
        savedStatus.setToStatusId(validStatusRequest.getToStatusId());
        savedStatus.setOrder(validStatusRequest.getOrder());
        savedStatus.setStatus(validStatusRequest.getStatus());

        when(documentStatusService.add(any(StatusRequest.class))).thenReturn(Mono.just(savedStatus));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/status/add")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(validStatusRequest), StatusRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Document status added successfully.");
                    assertThat(apiResponse.getData()).isNotNull();
                    Map<String, Object> dataMap = convertToMap(apiResponse.getData());
                    assertThat(dataMap.get("id")).isEqualTo(savedStatus.getId().intValue());
                    assertThat(dataMap.get("order")).isEqualTo(savedStatus.getOrder());
                });
    }

    @Test
    void addDocumentStatus_whenRequestValidationFails_shouldReturnBadRequest() {
        // Arrange
        // This test relies on Spring's automatic validation if @Valid were used on StatusRequest in controller.
        // Since it's not, we simulate the service layer throwing a validation-like error.
        // Or, if StatusRequest had JSR 303 annotations, and they failed, WebFlux would produce WebExchangeBindException.
        // The controller's onErrorResume for WebExchangeBindException should be hit.

        // We'll test the ConstraintViolationException path as it's explicitly handled.
        javax.validation.ConstraintViolationException mockViolationException =
            new javax.validation.ConstraintViolationException("Validation failed for StatusRequest", new java.util.HashSet<>());
        when(documentStatusService.add(any(StatusRequest.class)))
            .thenReturn(Mono.error(mockViolationException));
        
        // Act & Assert
        webTestClient.post().uri("/api/v1/document/status/add")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(invalidStatusRequest), StatusRequest.class) // Sending an invalid request
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error adding document status: Validation failed for StatusRequest");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    @Test
    void addDocumentStatus_whenServiceError_shouldReturnErrorResponse() {
        // Arrange
        when(documentStatusService.add(any(StatusRequest.class)))
            .thenReturn(Mono.error(new RuntimeException("Service internal error")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/status/add")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(validStatusRequest), StatusRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error adding document status: Service internal error");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    @Test
    void addDocumentStatus_whenServiceReturnsEmpty_shouldReturnConflictWithMessage() {
        // Arrange
        when(documentStatusService.add(any(StatusRequest.class))).thenReturn(Mono.empty()); // Simulate "already exists"

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/status/add")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(validStatusRequest), StatusRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(409) // Conflict
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Document status already exists.");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    // Tests for POST /api/v1/document/status/edit/{Id}
    @Test
    void editDocumentStatus_whenValidRequestAndFound_shouldReturnOkAndUpdatedStatus() {
        // Arrange
        Long existingId = documentStatus1.getId();
        StatusRequest updateRequest = new StatusRequest();
        updateRequest.setDocumentTypeId(1);
        updateRequest.setFromStatusId(1);
        updateRequest.setToStatusId(5); // New toStatusId
        updateRequest.setOrder(10);     // New order
        updateRequest.setStatus("I");   // New status

        DocumentStatus updatedStatusFromService = new DocumentStatus();
        updatedStatusFromService.setId(existingId);
        updatedStatusFromService.setDocumentTypeId(updateRequest.getDocumentTypeId());
        updatedStatusFromService.setFromStatusId(updateRequest.getFromStatusId());
        updatedStatusFromService.setToStatusId(updateRequest.getToStatusId());
        updatedStatusFromService.setOrder(updateRequest.getOrder());
        updatedStatusFromService.setStatus(updateRequest.getStatus());

        when(documentStatusService.findStatus(existingId)).thenReturn(Mono.just(documentStatus1)); // Status found
        when(documentStatusService.update(any(StatusRequest.class), eq(existingId))).thenReturn(Mono.just(updatedStatusFromService));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/status/edit/{Id}", existingId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), StatusRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Document status updated successfully.");
                    assertThat(apiResponse.getData()).isNotNull();
                    Map<String, Object> dataMap = convertToMap(apiResponse.getData());
                    assertThat(dataMap.get("id")).isEqualTo(existingId.intValue());
                    assertThat(dataMap.get("toStatusId")).isEqualTo(updateRequest.getToStatusId());
                    assertThat(dataMap.get("order")).isEqualTo(updateRequest.getOrder());
                    assertThat(dataMap.get("status")).isEqualTo(updateRequest.getStatus());
                });
    }

    @Test
    void editDocumentStatus_whenStatusNotFound_shouldReturnNotFoundWithMessage() {
        // Arrange
        Long nonExistentId = 99L;
        when(documentStatusService.findStatus(nonExistentId)).thenReturn(Mono.empty()); // Status not found

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/status/edit/{Id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(validStatusRequest), StatusRequest.class) // Body is valid, but ID not found
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound() // Controller's logic for findStatus.hasElement().block() being false
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Document status does not exist.");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    @Test
    void editDocumentStatus_whenRequestValidationFails_shouldReturnBadRequest() {
        // Arrange
        Long existingId = documentStatus1.getId();
        // Simulate ConstraintViolationException from service.update or from @Valid on request if it were there
        javax.validation.ConstraintViolationException mockViolationException =
            new javax.validation.ConstraintViolationException("Validation failed for update", new java.util.HashSet<>());
        
        when(documentStatusService.findStatus(existingId)).thenReturn(Mono.just(documentStatus1)); // Status found
        when(documentStatusService.update(any(StatusRequest.class), eq(existingId)))
            .thenReturn(Mono.error(mockViolationException));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/status/edit/{Id}", existingId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(invalidStatusRequest), StatusRequest.class) // Sending invalid request body
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error updating document status: Validation failed for update");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    @Test
    void editDocumentStatus_whenServiceUpdateError_shouldReturnErrorResponse() {
        // Arrange
        Long existingId = documentStatus1.getId();
        when(documentStatusService.findStatus(existingId)).thenReturn(Mono.just(documentStatus1)); // Status found
        when(documentStatusService.update(any(StatusRequest.class), eq(existingId)))
            .thenReturn(Mono.error(new RuntimeException("Service internal update error")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/status/edit/{Id}", existingId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(validStatusRequest), StatusRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error updating document status: Service internal update error");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    // Tests for POST /api/v1/document/status/delete/{Id}
    @Test
    void deleteDocumentStatus_whenValidRequestAndFound_shouldReturnOkAndDeletedStatus() {
        // Arrange
        Long existingId = documentStatus1.getId();
        DocumentStatus deletedStatusFromService = new DocumentStatus(); // Service might return the deleted object
        deletedStatusFromService.setId(existingId);
        deletedStatusFromService.setStatus("I"); // Typically, soft delete sets status to Inactive

        when(documentStatusService.findStatus(existingId)).thenReturn(Mono.just(documentStatus1)); // Status found
        when(documentStatusService.delete(existingId)).thenReturn(Mono.just(deletedStatusFromService)); // Deletion successful

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/status/delete/{Id}", existingId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Document status deleted successfully.");
                    // The controller returns the result of service.delete() as data.
                    // If service.delete() returns the deleted object (even if just its ID or a confirmation),
                    // then apiResponse.getData() would not be null.
                    // If service.delete() returns Mono<Void> then mapped to Mono.just(someConfirmation), data might be that.
                    // Given the controller code: .map(deletedStatus -> new ApiResponse(deletedStatus, ...)),
                    // it returns whatever service.delete() provides.
                    assertThat(apiResponse.getData()).isNotNull();
                    Map<String, Object> dataMap = convertToMap(apiResponse.getData());
                    assertThat(dataMap.get("id")).isEqualTo(existingId.intValue());
                     assertThat(dataMap.get("status")).isEqualTo("I"); // Assuming service returns the updated (soft-deleted) status
                });
    }
    
    @Test
    void deleteDocumentStatus_whenServiceReturnsEmptyOnDelete_shouldStillBeOkByControllerLogic() {
        // Arrange
        Long existingId = documentStatus1.getId();
        // This tests if service.delete() itself returns Mono.empty(), how controller handles it.
        // Controller: service.delete(Id).map(deletedStatus -> new ApiResponse(deletedStatus, ...))
        // .switchIfEmpty(Mono.just(new ApiResponse(null, "Document status could not be deleted or was not found.", HttpStatus.NOT_FOUND.value())));
        // So if service.delete() returns Mono.empty(), it should hit the switchIfEmpty.

        when(documentStatusService.findStatus(existingId)).thenReturn(Mono.just(documentStatus1)); // Status found initially
        when(documentStatusService.delete(existingId)).thenReturn(Mono.empty()); // Deletion returns empty

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/status/delete/{Id}", existingId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound() // Because service.delete() returned empty, triggering switchIfEmpty
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Document status could not be deleted or was not found.");
                    assertThat(apiResponse.getData()).isNull();
                });
    }


    @Test
    void deleteDocumentStatus_whenStatusNotFound_shouldReturnNotFoundWithMessage() {
        // Arrange
        Long nonExistentId = 99L;
        when(documentStatusService.findStatus(nonExistentId)).thenReturn(Mono.empty()); // Status not found

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/status/delete/{Id}", nonExistentId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound() // Controller's logic for findStatus.hasElement().block() being false
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Document status does not exist.");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    @Test
    void deleteDocumentStatus_whenServiceDeleteError_shouldReturnErrorResponse() {
        // Arrange
        Long existingId = documentStatus1.getId();
        when(documentStatusService.findStatus(existingId)).thenReturn(Mono.just(documentStatus1)); // Status found
        when(documentStatusService.delete(existingId))
            .thenReturn(Mono.error(new RuntimeException("Service internal delete error, e.g., protected status")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/status/delete/{Id}", existingId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error deleting document status: Service internal delete error, e.g., protected status");
                    assertThat(apiResponse.getData()).isNull();
                });
    }
}
