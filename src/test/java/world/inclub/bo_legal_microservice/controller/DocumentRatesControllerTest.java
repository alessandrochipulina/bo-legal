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
import world.inclub.bo_legal_microservice.application.services.RateService;
import world.inclub.bo_legal_microservice.domain.models.DocumentRates;
import world.inclub.bo_legal_microservice.domain.models.Rates;
import world.inclub.bo_legal_microservice.domain.request.RateRequest;
import world.inclub.bo_legal_microservice.helpers.ApiResponse;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(DocumentRatesController.class)
class DocumentRatesControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RateService rateService;

    private DocumentRates documentRate1;
    private DocumentRates documentRate2;
    private RateRequest validRateRequest;
    private RateRequest invalidRateRequest;
    private Rates updatedRate;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        documentRate1 = new DocumentRates();
        documentRate1.setId(1);
        documentRate1.setLegalType(1);
        documentRate1.setDocumentType(10);
        documentRate1.setLocalType(100);
        documentRate1.setPrice(150.0f);
        documentRate1.setStatus(1);
        documentRate1.setLegalName("Legal A");
        documentRate1.setDocumentName("Doc X");
        documentRate1.setLocalName("Local Alpha");

        documentRate2 = new DocumentRates();
        documentRate2.setId(2);
        documentRate2.setLegalType(2);
        documentRate2.setDocumentType(20);
        documentRate2.setLocalType(200);
        documentRate2.setPrice(250.0f);
        documentRate2.setStatus(1);
        documentRate2.setLegalName("Legal B");
        documentRate2.setDocumentName("Doc Y");
        documentRate2.setLocalName("Local Beta");

        validRateRequest = new RateRequest();
        validRateRequest.setId(101); // Assuming this ID might be used by service if it maps to Rates ID
        validRateRequest.setLegalType(1);
        validRateRequest.setDocumentType(10);
        validRateRequest.setLocalType(100);
        validRateRequest.setPrice(160.0f);

        invalidRateRequest = new RateRequest(); // Missing required fields like price or types
        invalidRateRequest.setId(102);


        updatedRate = new Rates();
        updatedRate.setId(validRateRequest.getId());
        updatedRate.setLegalizationType(validRateRequest.getLegalType());
        updatedRate.setDocumentTypeId(validRateRequest.getDocumentType());
        updatedRate.setLocalType(validRateRequest.getLocalType());
        updatedRate.setPrice(validRateRequest.getPrice());
        updatedRate.setStatus(1); // Active
        updatedRate.setCreatedAt(LocalDateTime.now().minusDays(1));
        updatedRate.setModifiedAt(LocalDateTime.now());
    }

    private Map<String, Object> convertToMap(Object obj) {
        return objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
    }

    // Tests for GET /api/v1/document/rates/all
    @Test
    void getAllDocumentRates_whenServiceReturnsRates_shouldReturnOkAndRateList() {
        // Arrange
        when(rateService.findAll()).thenReturn(Flux.just(documentRate1, documentRate2));

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/rates/all")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Tarifas recuperadas satisfactoriamente.");
                    assertThat(apiResponse.getData()).isNotNull();
                    List<Map<String, Object>> dataList = objectMapper.convertValue(apiResponse.getData(), new TypeReference<List<Map<String, Object>>>() {});
                    assertThat(dataList).hasSize(2);
                    assertThat(dataList.get(0).get("price")).isEqualTo(documentRate1.getPrice());
                    assertThat(dataList.get(1).get("legalName")).isEqualTo(documentRate2.getLegalName());
                });
    }

    @Test
    void getAllDocumentRates_whenServiceReturnsEmpty_shouldReturnOkAndEmptyList() {
        // Arrange
        when(rateService.findAll()).thenReturn(Flux.empty());

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/rates/all")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Tarifas recuperadas satisfactoriamente.");
                    assertThat(apiResponse.getData()).isInstanceOf(List.class);
                    List<?> dataList = (List<?>) apiResponse.getData();
                    assertThat(dataList).isEmpty();
                });
    }

    @Test
    void getAllDocumentRates_whenServiceFails_shouldReturnErrorResponse() {
        // Arrange
        when(rateService.findAll()).thenReturn(Flux.error(new RuntimeException("Service findAll error")));

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/rates/all")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al recuperar las tarifas: Service findAll error");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    // Placeholder for /api/v1/document/rates/edit tests

    // Tests for POST /api/v1/document/rates/edit
    @Test
    void editDocumentRate_whenValidRequestAndRateExists_shouldReturnOkAndUpdateRate() {
        // Arrange
        // First call for hasElement()
        when(rateService.update(any(RateRequest.class))).thenReturn(Mono.just(updatedRate)).thenReturn(Mono.just(updatedRate)); // Chain for two calls

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/rates/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(validRateRequest), RateRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Tarifa de documento actualizada satisfactoriamente.");
                    assertThat(apiResponse.getData()).isNotNull();
                    Map<String, Object> dataMap = convertToMap(apiResponse.getData());
                    assertThat(dataMap.get("price")).isEqualTo(updatedRate.getPrice()); // Price from updatedRate
                    assertThat(dataMap.get("legalizationType")).isEqualTo(updatedRate.getLegalizationType());
                });
    }

    @Test
    void editDocumentRate_whenRateNotFound_shouldReturnNotFoundResponse() {
        // Arrange
        // First call for hasElement() returns empty
        when(rateService.update(any(RateRequest.class))).thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/rates/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(validRateRequest), RateRequest.class) // valid request, but service says not found
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk() // Controller returns 200 OK with specific message
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("No se encuentra la tarifa de documento");
                    assertThat(apiResponse.getData()).isNull();
                });
    }

    @Test
    void editDocumentRate_whenValidationFails_shouldReturnBadRequest() {
        // Arrange
        // Simulate service layer validation failure due to invalid RateRequest (e.g. missing types for lookup)
        // The controller itself doesn't use @Valid on RateRequest.
        // This tests the scenario where the service.update() call throws an IllegalArgumentException.
        when(rateService.update(any(RateRequest.class)))
            .thenReturn(Mono.error(new IllegalArgumentException("Invalid RateRequest data for update")));
        
        // Act & Assert
        webTestClient.post().uri("/api/v1/document/rates/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(invalidRateRequest), RateRequest.class) // Sending a potentially invalid request
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest() // As controller maps IllegalArgumentException to 400
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al actualizar la tarifa: Invalid RateRequest data for update");
                    assertThat(apiResponse.getData()).isNull();
                });
    }
    
    @Test
    void editDocumentRate_whenFirstUpdateSucceedsButSecondUpdateCallFails_shouldReturnErrorResponse() {
        // Arrange
        // First call for hasElement() - succeeds
        when(rateService.update(any(RateRequest.class)))
            .thenReturn(Mono.just(updatedRate)) // First call success
            .thenReturn(Mono.error(new RuntimeException("Service error on second update call"))); // Second call fails

        // Act & Assert
        webTestClient.post().uri("/api/v1/document/rates/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(validRateRequest), RateRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError() // The error from the second service call
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al actualizar la tarifa: Service error on second update call");
                    assertThat(apiResponse.getData()).isNull();
                });
    }
}
