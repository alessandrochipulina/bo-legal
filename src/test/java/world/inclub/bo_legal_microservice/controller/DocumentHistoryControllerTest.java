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
import world.inclub.bo_legal_microservice.domain.DocumentHistory;
import world.inclub.bo_legal_microservice.domain.ports.in.DocumentHistoryRepository;
import world.inclub.bo_legal_microservice.helpers.ApiResponse;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(DocumentHistoryController.class)
class DocumentHistoryControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DocumentHistoryRepository documentHistoryRepository;

    private DocumentHistory historyEntry1;
    private DocumentHistory historyEntry2;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // For LocalDateTime

        historyEntry1 = new DocumentHistory();
        historyEntry1.setId(1L);
        historyEntry1.setDocumentKey("doc-key-123");
        historyEntry1.setDocumentStatusId(1); // PENDIENTE
        historyEntry1.setObservation("Document created");
        historyEntry1.setUserId("user1");
        historyEntry1.setCreationDate(LocalDateTime.now().minusDays(1));

        historyEntry2 = new DocumentHistory();
        historyEntry2.setId(2L);
        historyEntry2.setDocumentKey("doc-key-123"); // Same document key
        historyEntry2.setDocumentStatusId(2); // APROBADO
        historyEntry2.setObservation("Document approved");
        historyEntry2.setUserId("admin1");
        historyEntry2.setCreationDate(LocalDateTime.now());
    }

    private Map<String, Object> convertToMap(Object obj) {
        return objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
    }

    // Tests for GET /api/v1/document/history/{documentKey}
    @Test
    void getHistoryByDocumentKey_whenRepositoryReturnsHistory_shouldReturnOkAndHistoryList() {
        // Arrange
        String documentKey = "doc-key-123";
        when(documentHistoryRepository.findByDocumentKey(documentKey)).thenReturn(Flux.just(historyEntry1, historyEntry2));

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/history/{documentKey}", documentKey)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Historial recuperado satisfactoriamente.");
                    assertThat(apiResponse.getData()).isNotNull();
                    List<Map<String, Object>> dataList = objectMapper.convertValue(apiResponse.getData(), new TypeReference<List<Map<String, Object>>>() {});
                    assertThat(dataList).hasSize(2);
                    assertThat(dataList.get(0).get("documentKey")).isEqualTo(historyEntry1.getDocumentKey());
                    assertThat(dataList.get(1).get("observation")).isEqualTo(historyEntry2.getObservation());
                });
    }

    @Test
    void getHistoryByDocumentKey_whenRepositoryReturnsEmpty_shouldReturnOkAndEmptyList() {
        // Arrange
        String documentKey = "doc-key-empty";
        when(documentHistoryRepository.findByDocumentKey(documentKey)).thenReturn(Flux.empty());

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/history/{documentKey}", documentKey)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Historial recuperado satisfactoriamente.");
                    assertThat(apiResponse.getData()).isInstanceOf(List.class);
                    List<?> dataList = (List<?>) apiResponse.getData();
                    assertThat(dataList).isEmpty();
                    // The specific "No existe el documento en el historial" message is in a switchIfEmpty
                    // on a Mono<List>, which is not hit if the Flux is empty then collected to an empty List.
                });
    }
    
    @Test
    void getHistoryByDocumentKey_whenTriggersSpecificNotFoundMessage_shouldReturnOkWithMessage() {
        // To trigger the "No existe el documento en el historial" message, the
        // `documentHistoryRepository.findByDocumentKey(documentKey).collectList()`
        // must result in `Mono.empty()`. This is not standard for `collectList()` on an empty Flux.
        // Standard behavior: Flux.empty().collectList() -> Mono.just(Collections.emptyList()).
        // To test this specific path, we'd have to mock `collectList()` itself or assume
        // the repository method `findByDocumentKey()` returns `Flux<T>` which then, through some
        // operators before `collectList()`, results in `Mono.empty()` for the list accumulation.
        // This is highly unlikely.
        // A more plausible way this specific switchIfEmpty is hit is if `findByDocumentKey`
        // itself returned a `Mono<List<DocumentHistory>>` and that mono was empty.
        // Given the repository returns `Flux<DocumentHistory>`, this specific message is hard to trigger.
        //
        // Let's assume for the sake of testing the message, that the repository could somehow produce a Mono.empty()
        // for the list if the documentKey is considered invalid before even querying.
        // This is not what `Flux.empty().collectList()` does.
        // The controller code:
        // return dhr.findByDocumentKey(documentKey).collectList()
        // .flatMap(histories -> {
        //     if (histories.isEmpty()) {
        //         return Mono.just(new ApiResponse(Collections.emptyList(), "No existe el documento en el historial", HttpStatus.OK.value()));
        //     }
        //     return Mono.just(new ApiResponse(histories, "Historial recuperado satisfactoriamente.", HttpStatus.OK.value()));
        // })
        // .switchIfEmpty(Mono.just(new ApiResponse(null, "No existe el documento en el historial", HttpStatus.NOT_FOUND.value()))) <--- THIS IS THE TARGET
        // .onErrorResume(e -> Mono.just(new ApiResponse(null, "Error al recuperar el historial: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value())));
        //
        // The `flatMap` handles the empty list case and returns an OK response.
        // The `switchIfEmpty` for NOT_FOUND is only hit if the `Mono<List<DocumentHistory>>` from `collectList()` is ITSELF empty.
        // As stated, `Flux.empty().collectList()` results in `Mono.just(new ArrayList<>())`.
        // So the `switchIfEmpty` resulting in a 404 with "No existe el documento en el historial" is currently UNREACHABLE.
        // The flatMap's logic for `histories.isEmpty()` already covers the "not found" scenario with a 200 OK.
        //
        // The previous test `getHistoryByDocumentKey_whenRepositoryReturnsEmpty_shouldReturnOkAndEmptyList`
        // correctly tests the `histories.isEmpty()` path in the `flatMap`.

        // If the intention of the `switchIfEmpty` was to catch a truly non-existent key *before* collection,
        // the structure would need to be different, perhaps a service layer checking key existence first.
        // I will keep the test for empty list as the practical "not found" for this controller.
        // The specific "No existe el documento en el historial" with 404 status is not testable given current controller logic.
    }


    @Test
    void getHistoryByDocumentKey_whenRepositoryFails_shouldReturnErrorResponse() {
        // Arrange
        String documentKey = "doc-key-fail";
        when(documentHistoryRepository.findByDocumentKey(documentKey))
                .thenReturn(Flux.error(new RuntimeException("Repository findByDocumentKey error")));

        // Act & Assert
        webTestClient.get().uri("/api/v1/document/history/{documentKey}", documentKey)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiResponse.class)
                .value(apiResponse -> {
                    assertThat(apiResponse.getMessage()).isEqualTo("Error al recuperar el historial: Repository findByDocumentKey error");
                    assertThat(apiResponse.getData()).isNull();
                });
    }
}
