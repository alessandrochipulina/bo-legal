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
import world.inclub.bo_legal_microservice.application.services.DocumentService;
import world.inclub.bo_legal_microservice.domain.Document;
import world.inclub.bo_legal_microservice.domain.dto.ChangeStatusDocumentDTO;
import world.inclub.bo_legal_microservice.domain.dto.DocumentDTO;
import world.inclub.bo_legal_microservice.domain.dto.DocumentRectificationDTO;
import world.inclub.bo_legal_microservice.domain.enums.DocumentStatusEnum;
import world.inclub.bo_legal_microservice.domain.enums.DocumentTypeEnum;
import world.inclub.bo_legal_microservice.domain.ports.in.DocumentRepository;
import world.inclub.bo_legal_microservice.helpers.ApiResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private DocumentRepository documentRepository; // As per requirement

    private DocumentDTO documentDTO;
    private DocumentRectificationDTO documentRectificationDTO;
    private Document document1;
    private Document document2;
    private ChangeStatusDocumentDTO changeStatusDTO;
    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // For LocalDateTime serialization/deserialization

        documentDTO = new DocumentDTO();
        documentDTO.setUuid(UUID.randomUUID().toString());
        documentDTO.setExternalId("ext-solicitud-123");
        documentDTO.setPaymentId("pay-solicitud-123");
        // documentDTO.setDocumentTypeId will be set by path variable in the test
        documentDTO.setRateId(1L);
        documentDTO.setAmount(BigDecimal.TEN);
        documentDTO.setUserId("user-test-1");
        documentDTO.setUserName("Solicitud User");
        documentDTO.setUserEmail("solicitud@example.com");
        documentDTO.setUserPhone("111222333");
        documentDTO.setCompanyId("comp-solicitud-1");
        documentDTO.setCompanyName("Solicitud Company");
        documentDTO.setPaymentDate(LocalDateTime.now().toString());
        documentDTO.setCreationDate(LocalDateTime.now().toString());
        documentDTO.setDetails(Collections.singletonMap("solicitud_key", "solicitud_value"));

        documentRectificationDTO = new DocumentRectificationDTO();
        documentRectificationDTO.setUuid(UUID.randomUUID().toString());
        documentRectificationDTO.setTargetDocumentUuid(UUID.randomUUID().toString());
        documentRectificationDTO.setPaymentId("pay-rect-123");
        documentRectificationDTO.setRateId(2L);
        documentRectificationDTO.setAmount(BigDecimal.ONE);
        documentRectificationDTO.setUserId("user-rect-1");
        documentRectificationDTO.setUserName("Rectification User");
        documentRectificationDTO.setUserEmail("rect@example.com");
        documentRectificationDTO.setUserPhone("444555666");
        documentRectificationDTO.setCompanyId("comp-rect-1");
        documentRectificationDTO.setCompanyName("Rectification Company");
        documentRectificationDTO.setPaymentDate(LocalDateTime.now().toString());
        documentRectificationDTO.setCreationDate(LocalDateTime.now().toString());
        documentRectificationDTO.setDetails(Collections.singletonMap("rect_key", "rect_value"));

        document1 = new Document();
        document1.setId(1L);
        document1.setUuid(UUID.randomUUID().toString());
        document1.setDocumentTypeId(DocumentTypeEnum.SOLICITUD_CERTIFICADO.getId());
        document1.setDocumentStatusId(DocumentStatusEnum.PENDIENTE.getId());
        document1.setUserId("user-doc-1");
        document1.setCreationDate(LocalDateTime.now());

        document2 = new Document();
        document2.setId(2L);
        document2.setUuid(UUID.randomUUID().toString());
        document2.setDocumentTypeId(DocumentTypeEnum.CERTIFICADO.getId());
        document2.setDocumentStatusId(DocumentStatusEnum.ATENDIDO.getId());
        document2.setUserId("user-doc-2");
        document2.setCreationDate(LocalDateTime.now());

        changeStatusDTO = new ChangeStatusDocumentDTO();
        changeStatusDTO.setUuid(document1.getUuid());
        changeStatusDTO.setNewStatusId(DocumentStatusEnum.APROBADO.getId());
        changeStatusDTO.setObservation("Test approval observation");
        changeStatusDTO.setUserId("admin-user");
    }

    // Helper to convert object to Map, useful for asserting parts of ApiResponse.data
    private Map<String, Object> convertToMap(Object obj) {
        return objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
    }

    // Tests for POST /api/v1/document/add/solicitud/{documentTypeId}
    @Test
    void addSolicitud_whenValidInput_shouldReturnOkAndSavedDocument() {
        // Arrange
        int documentTypeId = DocumentTypeEnum.SOLICITUD_CERTIFICADO.getId();
        documentDTO.setDocumentTypeId(documentTypeId); // Set it for the DTO
        Document savedDocument = new Document(); // Simulate service returning a full Document
        savedDocument.setId(100L);
        savedDocument.setUuid(documentDTO.getUuid());
        savedDocument.setDocumentTypeId(documentTypeId);
        savedDocument.setDocumentStatusId(DocumentStatusEnum.PENDIENTE.getId()); // Initial status
        savedDocument.setExternalId(documentDTO.getExternalId());

        when(documentService.addDocumentSolicitud(any(DocumentDTO.class))).thenReturn(Mono.just(savedDocument));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/add/solicitud/{documentTypeId}", documentTypeId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(documentDTO), DocumentDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Documento agregado satisfactoriamente.");
                    assertThat(apiResponse.getData()).isNotNull();
                    Map<String, Object> dataMap = convertToMap(apiResponse.getData());
                    assertThat(dataMap.get("uuid")).isEqualTo(savedDocument.getUuid());
                    assertThat(dataMap.get("documentTypeId")).isEqualTo(savedDocument.getDocumentTypeId());
                    assertThat(dataMap.get("externalId")).isEqualTo(savedDocument.getExternalId());
                });
    }
    
    @Test
    void addSolicitud_whenServiceError_shouldReturnErrorResponse() {
        // Arrange
        int documentTypeId = DocumentTypeEnum.SOLICITUD_CERTIFICADO.getId();
        documentDTO.setDocumentTypeId(documentTypeId);
        when(documentService.addDocumentSolicitud(any(DocumentDTO.class)))
            .thenReturn(Mono.error(new RuntimeException("Service layer error")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/add/solicitud/{documentTypeId}", documentTypeId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(documentDTO), DocumentDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError() // Controller's onErrorResume maps general errors to 500
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al guardar el documento: Service layer error");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    // Placeholder for other tests (validation errors for addSolicitud will be tricky without @Valid)
    // If DocumentDTO had @NotNull on fields, and controller used @Valid, Spring would return 400.
    // Here, we assume service layer might throw IllegalArgumentException for "validation".
    @Test
    void addSolicitud_whenServiceValidationFails_shouldReturnErrorResponse() {
        // Arrange
        int documentTypeId = DocumentTypeEnum.SOLICITUD_CERTIFICADO.getId();
        documentDTO.setDocumentTypeId(documentTypeId);
        // Simulate service throwing IllegalArgumentException for a missing required field in DTO
        when(documentService.addDocumentSolicitud(any(DocumentDTO.class)))
            .thenReturn(Mono.error(new IllegalArgumentException("ExternalId cannot be null")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/add/solicitud/{documentTypeId}", documentTypeId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(documentDTO), DocumentDTO.class) // Sending a valid DTO here, but service mock rejects it
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest() // Controller maps IllegalArgumentException to 400
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al guardar el documento: ExternalId cannot be null");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    // Tests for POST /api/v1/document/add/rectificacion
    @Test
    void addRectificacion_whenValidInput_shouldReturnOkAndSavedDocument() {
        // Arrange
        Document savedDocument = new Document(); // Simulate service returning a full Document
        savedDocument.setId(101L);
        savedDocument.setUuid(documentRectificationDTO.getUuid());
        savedDocument.setDocumentTypeId(DocumentTypeEnum.VOUCHER_RECTIFICACION.getId()); // Expected type for rectification
        savedDocument.setDocumentStatusId(DocumentStatusEnum.PENDIENTE.getId()); // Initial status
        savedDocument.setTargetDocumentUuid(documentRectificationDTO.getTargetDocumentUuid());

        when(documentService.addDocumentRectificacion(any(DocumentRectificationDTO.class))).thenReturn(Mono.just(savedDocument));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/add/rectificacion")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(documentRectificationDTO), DocumentRectificationDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Documento agregado satisfactoriamente.");
                    assertThat(apiResponse.getData()).isNotNull();
                    Map<String, Object> dataMap = convertToMap(apiResponse.getData());
                    assertThat(dataMap.get("uuid")).isEqualTo(savedDocument.getUuid());
                    assertThat(dataMap.get("documentTypeId")).isEqualTo(savedDocument.getDocumentTypeId());
                    assertThat(dataMap.get("targetDocumentUuid")).isEqualTo(savedDocument.getTargetDocumentUuid());
                });
    }

    @Test
    void addRectificacion_whenServiceError_shouldReturnErrorResponse() {
        // Arrange
        when(documentService.addDocumentRectificacion(any(DocumentRectificationDTO.class)))
            .thenReturn(Mono.error(new RuntimeException("Service layer error for rectification")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/add/rectificacion")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(documentRectificationDTO), DocumentRectificationDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al guardar el documento: Service layer error for rectification");
                    assertThat(apiResponse.getData()).isNull();
                });
    }
    
    @Test
    void addRectificacion_whenServiceValidationFails_shouldReturnErrorResponse() {
        // Arrange
        // Simulate service throwing IllegalArgumentException for a missing required field in DTO
        when(documentService.addDocumentRectificacion(any(DocumentRectificationDTO.class)))
            .thenReturn(Mono.error(new IllegalArgumentException("TargetDocumentUuid cannot be null")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/add/rectificacion")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(documentRectificationDTO), DocumentRectificationDTO.class) 
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest() // Controller maps IllegalArgumentException to 400
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al guardar el documento: TargetDocumentUuid cannot be null");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    // Tests for GET /api/v1/document/all
    @Test
    void getAllDocuments_whenServiceReturnsDocuments_shouldReturnOkAndDocumentList() {
        // Arrange
        when(documentService.findAll()).thenReturn(Flux.just(document1, document2));

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/all")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Documentos recuperados satisfactoriamente.");
                    assertThat(apiResponse.getData()).isNotNull();
                    List<Map<String, Object>> dataList = objectMapper.convertValue(apiResponse.getData(), new TypeReference<List<Map<String, Object>>>() {});
                    assertThat(dataList).hasSize(2);
                    assertThat(dataList.get(0).get("uuid")).isEqualTo(document1.getUuid());
                    assertThat(dataList.get(1).get("uuid")).isEqualTo(document2.getUuid());
                });
    }

    @Test
    void getAllDocuments_whenServiceReturnsEmpty_shouldReturnOkAndEmptyList() {
        // Arrange
        when(documentService.findAll()).thenReturn(Flux.empty());

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/all")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Documentos recuperados satisfactoriamente.");
                    assertThat(apiResponse.getData()).isInstanceOf(List.class);
                    List<?> dataList = (List<?>) apiResponse.getData();
                    assertThat(dataList).isEmpty();
                });
    }

    @Test
    void getAllDocuments_whenServiceFails_shouldReturnErrorResponse() {
        // Arrange
        when(documentService.findAll()).thenReturn(Flux.error(new RuntimeException("Service error on findAll")));

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/all")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al recuperar los documentos: Service error on findAll");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    // Tests for GET /api/v1/document/all/{documentTypeId}
    @Test
    void getDocumentsByTypeId_whenRepositoryReturnsDocuments_shouldReturnOkAndDocumentList() {
        // Arrange
        int documentTypeId = DocumentTypeEnum.SOLICITUD_CERTIFICADO.getId();
        document1.setDocumentTypeId(documentTypeId); // Ensure document1 matches type
        // document2 might be a different type, so only document1 should be returned if filtered by type
        when(documentRepository.findByDocumentTypeId(documentTypeId)).thenReturn(Flux.just(document1));

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/all/{documentTypeId}", documentTypeId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Documentos recuperados satisfactoriamente.");
                    assertThat(apiResponse.getData()).isNotNull();
                    List<Map<String, Object>> dataList = objectMapper.convertValue(apiResponse.getData(), new TypeReference<List<Map<String, Object>>>() {});
                    assertThat(dataList).hasSize(1);
                    assertThat(dataList.get(0).get("uuid")).isEqualTo(document1.getUuid());
                    assertThat(dataList.get(0).get("documentTypeId")).isEqualTo(documentTypeId);
                });
    }

    @Test
    void getDocumentsByTypeId_whenRepositoryReturnsEmpty_shouldReturnOkAndEmptyList() {
        // Arrange
        int documentTypeId = DocumentTypeEnum.CERTIFICADO.getId();
        when(documentRepository.findByDocumentTypeId(documentTypeId)).thenReturn(Flux.empty());

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/all/{documentTypeId}", documentTypeId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Documentos recuperados satisfactoriamente.");
                    assertThat(apiResponse.getData()).isInstanceOf(List.class);
                    List<?> dataList = (List<?>) apiResponse.getData();
                    assertThat(dataList).isEmpty();
                });
    }

    @Test
    void getDocumentsByTypeId_whenRepositoryFails_shouldReturnErrorResponse() {
        // Arrange
        int documentTypeId = DocumentTypeEnum.VOUCHER_OPERACION.getId();
        when(documentRepository.findByDocumentTypeId(documentTypeId))
            .thenReturn(Flux.error(new RuntimeException("Repository error findByDocumentTypeId")));

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/all/{documentTypeId}", documentTypeId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al recuperar los documentos: Repository error findByDocumentTypeId");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    // Tests for GET /api/v1/document/{documentKey}
    @Test
    void getDocumentByKey_whenFoundByProcessKey_shouldReturnOkAndDocument() {
        // Arrange
        String documentKey = "process-key-123";
        when(documentRepository.findByDocumentKeyProcess(documentKey)).thenReturn(Mono.just(document1));
        // No need to mock findByDocumentKeyVoucher if the first one is found

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/{documentKey}", documentKey)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Documento recuperado satisfactoriamente.");
                    assertThat(apiResponse.getData()).isNotNull();
                    Map<String, Object> dataMap = convertToMap(apiResponse.getData());
                    assertThat(dataMap.get("uuid")).isEqualTo(document1.getUuid());
                });
    }

    @Test
    void getDocumentByKey_whenFoundByVoucherKey_shouldReturnOkAndDocument() {
        // Arrange
        String documentKey = "voucher-key-456";
        when(documentRepository.findByDocumentKeyProcess(documentKey)).thenReturn(Mono.empty()); // Not found by process key
        when(documentRepository.findByDocumentKeyVoucher(documentKey)).thenReturn(Mono.just(document2)); // Found by voucher key

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/{documentKey}", documentKey)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Documento recuperado satisfactoriamente.");
                    assertThat(apiResponse.getData()).isNotNull();
                    Map<String, Object> dataMap = convertToMap(apiResponse.getData());
                    assertThat(dataMap.get("uuid")).isEqualTo(document2.getUuid());
                });
    }

    @Test
    void getDocumentByKey_whenNotFoundByEitherKey_shouldReturnOkAndNotFoundMessage() {
        // Arrange
        String documentKey = "unknown-key-789";
        when(documentRepository.findByDocumentKeyProcess(documentKey)).thenReturn(Mono.empty());
        when(documentRepository.findByDocumentKeyVoucher(documentKey)).thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/{documentKey}", documentKey)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk() // Controller logic maps overall empty to OK with specific message
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Documento no encontrado.");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    @Test
    void getDocumentByKey_whenProcessKeySearchFails_shouldAttemptVoucherKeySearchAndSucceed() {
        // Arrange
        String documentKey = "fail-then-success-key";
        when(documentRepository.findByDocumentKeyProcess(documentKey))
            .thenReturn(Mono.error(new RuntimeException("DB error on process key search")));
        when(documentRepository.findByDocumentKeyVoucher(documentKey)).thenReturn(Mono.just(document2));

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/{documentKey}", documentKey)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk() // Because the second search succeeds
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Documento recuperado satisfactoriamente.");
                    assertThat(apiResponse.getData()).isNotNull();
                     Map<String, Object> dataMap = convertToMap(apiResponse.getData());
                    assertThat(dataMap.get("uuid")).isEqualTo(document2.getUuid());
                });
    }
    
    @Test
    void getDocumentByKey_whenProcessKeySearchFails_andVoucherKeySearchFails_shouldReturnError() {
        // Arrange
        String documentKey = "both-fail-key";
        when(documentRepository.findByDocumentKeyProcess(documentKey))
            .thenReturn(Mono.error(new RuntimeException("DB error on process key search")));
        when(documentRepository.findByDocumentKeyVoucher(documentKey))
            .thenReturn(Mono.error(new RuntimeException("DB error on voucher key search")));

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/{documentKey}", documentKey)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError() // The error from the second search (voucher) should propagate
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al recuperar el documento: DB error on voucher key search");
                    assertThat(apiResponse.getData()).isNull();
                });
    }
    
    @Test
    void getDocumentByKey_whenFirstSearchFailsThenSecondNotFound_shouldReturnOkAndNotFoundMessage() {
        // Arrange
        String documentKey = "fail-then-notfound-key";
        when(documentRepository.findByDocumentKeyProcess(documentKey))
            .thenReturn(Mono.error(new RuntimeException("DB error on process key search")));
        when(documentRepository.findByDocumentKeyVoucher(documentKey)).thenReturn(Mono.empty()); // Second search finds nothing

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/{documentKey}", documentKey)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk() // Controller logic maps overall empty (after error in first) to OK with specific message
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Documento no encontrado.");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    // Tests for POST /api/v1/document/change
    @Test
    void changeDocumentStatus_whenValidInput_shouldReturnOkAndChangedDocument() {
        // Arrange
        Document changedDocument = new Document();
        changedDocument.setId(document1.getId());
        changedDocument.setUuid(changeStatusDTO.getUuid());
        changedDocument.setDocumentStatusId(changeStatusDTO.getNewStatusId()); // Status changed to APROBADO
        changedDocument.setDocumentTypeId(document1.getDocumentTypeId());

        when(documentService.changeStatusDocument(any(ChangeStatusDocumentDTO.class))).thenReturn(Mono.just(changedDocument));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/change")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(changeStatusDTO), ChangeStatusDocumentDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Documento actualizado satisfactoriamente.");
                    assertThat(apiResponse.getData()).isNotNull();
                    Map<String, Object> dataMap = convertToMap(apiResponse.getData());
                    assertThat(dataMap.get("uuid")).isEqualTo(changedDocument.getUuid());
                    assertThat(dataMap.get("documentStatusId")).isEqualTo(changedDocument.getDocumentStatusId());
                });
    }

    @Test
    void changeDocumentStatus_whenServiceError_shouldReturnErrorResponse() {
        // Arrange
        when(documentService.changeStatusDocument(any(ChangeStatusDocumentDTO.class)))
            .thenReturn(Mono.error(new RuntimeException("Service error during status change")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/change")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(changeStatusDTO), ChangeStatusDocumentDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al actualizar el documento: Service error during status change");
                    assertThat(apiResponse.getData()).isNull();
                });
    }
    
    @Test
    void changeDocumentStatus_whenServiceValidationFails_shouldReturnErrorResponse() {
        // Arrange
        // Simulate service throwing IllegalArgumentException for an invalid status transition or missing field
        when(documentService.changeStatusDocument(any(ChangeStatusDocumentDTO.class)))
            .thenReturn(Mono.error(new IllegalArgumentException("Invalid status transition")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/change")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(changeStatusDTO), ChangeStatusDocumentDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest() // Controller maps IllegalArgumentException to 400
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al actualizar el documento: Invalid status transition");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    // Tests for POST /api/v1/document/approve
    @Test
    void approveDocument_whenValidInput_shouldReturnOkAndApprovedDocument() {
        // Arrange
        // changeStatusDTO already has UUID and observation, newStatusId is not directly used by approveDocument
        // but the service will set the status to APROBADO.
        Document approvedDocument = new Document();
        approvedDocument.setId(document1.getId());
        approvedDocument.setUuid(changeStatusDTO.getUuid());
        approvedDocument.setDocumentStatusId(DocumentStatusEnum.APROBADO.getId()); // Expected status after approval
        approvedDocument.setDocumentTypeId(document1.getDocumentTypeId());

        when(documentService.approveDocument(any(ChangeStatusDocumentDTO.class))).thenReturn(Mono.just(approvedDocument));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(changeStatusDTO), ChangeStatusDocumentDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Documento aprobado satisfactoriamente.");
                    assertThat(apiResponse.getData()).isNotNull();
                    Map<String, Object> dataMap = convertToMap(apiResponse.getData());
                    assertThat(dataMap.get("uuid")).isEqualTo(approvedDocument.getUuid());
                    assertThat(dataMap.get("documentStatusId")).isEqualTo(DocumentStatusEnum.APROBADO.getId());
                });
    }

    @Test
    void approveDocument_whenServiceError_shouldReturnErrorResponse() {
        // Arrange
        when(documentService.approveDocument(any(ChangeStatusDocumentDTO.class)))
            .thenReturn(Mono.error(new RuntimeException("Service error during approval")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(changeStatusDTO), ChangeStatusDocumentDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al aprobar el documento: Service error during approval");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    @Test
    void approveDocument_whenServiceValidationFails_shouldReturnErrorResponse() {
        // Arrange
        // Simulate service throwing IllegalArgumentException (e.g., document not in PENDING state)
        when(documentService.approveDocument(any(ChangeStatusDocumentDTO.class)))
            .thenReturn(Mono.error(new IllegalArgumentException("Document cannot be approved from its current state")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(changeStatusDTO), ChangeStatusDocumentDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest() // Controller maps IllegalArgumentException to 400
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al aprobar el documento: Document cannot be approved from its current state");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    // Tests for POST /api/v1/document/reject/{documentKey}
    @Test
    void rejectDocument_whenValidInput_shouldReturnOkAndRejectedDocument() {
        // Arrange
        String documentKey = document1.getUuid(); // Assuming documentKey is UUID for rejection
        // changeStatusDTO contains observation and userId. The documentKey is from path.
        // The service will set the status to RECHAZADO.
        Document rejectedDocument = new Document();
        rejectedDocument.setId(document1.getId());
        rejectedDocument.setUuid(documentKey);
        rejectedDocument.setDocumentStatusId(DocumentStatusEnum.RECHAZADO.getId()); // Expected status after rejection
        rejectedDocument.setDocumentTypeId(document1.getDocumentTypeId());

        // Controller uses documentKey from path for rejectDocument, not from DTO.
        // We need to ensure the DTO is still passed for other fields like observation, userId.
        ChangeStatusDocumentDTO rejectDto = new ChangeStatusDocumentDTO(documentKey, "Test rejection", "admin-user");


        when(documentService.rejectDocument(eq(documentKey), any(ChangeStatusDocumentDTO.class))).thenReturn(Mono.just(rejectedDocument));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/reject/{documentKey}", documentKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(rejectDto), ChangeStatusDocumentDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Documento rechazado satisfactoriamente.");
                    assertThat(apiResponse.getData()).isNotNull();
                    Map<String, Object> dataMap = convertToMap(apiResponse.getData());
                    assertThat(dataMap.get("uuid")).isEqualTo(rejectedDocument.getUuid());
                    assertThat(dataMap.get("documentStatusId")).isEqualTo(DocumentStatusEnum.RECHAZADO.getId());
                });
    }

    @Test
    void rejectDocument_whenServiceError_shouldReturnErrorResponse() {
        // Arrange
        String documentKey = document1.getUuid();
        ChangeStatusDocumentDTO rejectDto = new ChangeStatusDocumentDTO(documentKey, "Test rejection", "admin-user");

        when(documentService.rejectDocument(eq(documentKey), any(ChangeStatusDocumentDTO.class)))
            .thenReturn(Mono.error(new RuntimeException("Service error during rejection")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/reject/{documentKey}", documentKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(rejectDto), ChangeStatusDocumentDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al rechazar el documento: Service error during rejection");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    @Test
    void rejectDocument_whenServiceValidationFails_shouldReturnErrorResponse() {
        // Arrange
        String documentKey = document1.getUuid();
        ChangeStatusDocumentDTO rejectDto = new ChangeStatusDocumentDTO(documentKey, "Test rejection", "admin-user");

        // Simulate service throwing IllegalArgumentException (e.g., document not in PENDING state)
        when(documentService.rejectDocument(eq(documentKey), any(ChangeStatusDocumentDTO.class)))
            .thenReturn(Mono.error(new IllegalArgumentException("Document cannot be rejected from its current state")));

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/reject/{documentKey}", documentKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(rejectDto), ChangeStatusDocumentDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest() // Controller maps IllegalArgumentException to 400
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al rechazar el documento: Document cannot be rejected from its current state");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    // Tests for GET /api/v1/document/user/{userId}
    @Test
    void getDocumentsByUserId_whenRepositoryReturnsDocuments_shouldReturnOkAndDocumentList() {
        // Arrange
        String userId = "user-doc-1";
        // document1 has userId "user-doc-1"
        when(documentRepository.findByUserId(userId)).thenReturn(Flux.just(document1));

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/user/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Documentos recuperados satisfactoriamente.");
                    assertThat(apiResponse.getData()).isNotNull();
                    List<Map<String, Object>> dataList = objectMapper.convertValue(apiResponse.getData(), new TypeReference<List<Map<String, Object>>>() {});
                    assertThat(dataList).hasSize(1);
                    assertThat(dataList.get(0).get("uuid")).isEqualTo(document1.getUuid());
                    assertThat(dataList.get(0).get("userId")).isEqualTo(userId);
                });
    }

    @Test
    void getDocumentsByUserId_whenRepositoryReturnsEmpty_shouldReturnOkAndEmptyList() {
        // Arrange
        String userId = "unknown-user";
        when(documentRepository.findByUserId(userId)).thenReturn(Flux.empty());

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/user/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Documentos recuperados satisfactoriamente.");
                    assertThat(apiResponse.getData()).isInstanceOf(List.class);
                    List<?> dataList = (List<?>) apiResponse.getData();
                    assertThat(dataList).isEmpty();
                });
    }

    @Test
    void getDocumentsByUserId_whenRepositoryFails_shouldReturnErrorResponse() {
        // Arrange
        String userId = "error-user";
        when(documentRepository.findByUserId(userId))
            .thenReturn(Flux.error(new RuntimeException("Repository error findByUserId")));

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/user/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al recuperar los documentos: Repository error findByUserId");
                    assertThat(apiResponse.getData()).isNull();
                });
    }
}
