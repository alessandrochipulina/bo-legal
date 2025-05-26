package world.inclub.bo_legal_microservice.application.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import world.inclub.bo_legal_microservice.domain.DocumentStatus;
import world.inclub.bo_legal_microservice.domain.ports.in.DocumentStatusRepository;
import world.inclub.bo_legal_microservice.infraestructure.config.AppProperties;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentStatusServiceTest {

    @Mock
    private DocumentStatusRepository documentStatusRepository;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private DocumentStatusService documentStatusService;

    private DocumentStatus documentStatus1;
    private DocumentStatus documentStatus2;
    private AppProperties.Status mockAppStatus;

    private final int ATENDIDO_STATUS_ID = 5; // Example system-protected threshold

    @BeforeEach
    void setUp() {
        mockAppStatus = Mockito.mock(AppProperties.Status.class);
        lenient().when(appProperties.getStatus()).thenReturn(mockAppStatus);
        lenient().when(mockAppStatus.getAtendido()).thenReturn(ATENDIDO_STATUS_ID);

        documentStatus1 = new DocumentStatus();
        documentStatus1.setId(1L);
        documentStatus1.setDocumentTypeId(1);
        documentStatus1.setFromStatusId(1);
        documentStatus1.setToStatusId(2);
        documentStatus1.setOrder(1);
        documentStatus1.setStatus("A"); // Active
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

        // Lenient stubbing for save operations
        lenient().when(documentStatusRepository.save(any(DocumentStatus.class))).thenAnswer(invocation -> {
            DocumentStatus ds = invocation.getArgument(0);
            if (ds.getId() == null) {
                ds.setId(System.currentTimeMillis()); // Simulate ID generation
            }
            ds.setModificationDate(LocalDateTime.now());
            return Mono.just(ds);
        });
    }

    @Test
    void findAll_shouldReturnAllDocumentStatuses() {
        // Arrange
        when(documentStatusRepository.findAll()).thenReturn(Flux.just(documentStatus1, documentStatus2));

        // Act
        Flux<DocumentStatus> result = documentStatusService.findAll();

        // Assert
        StepVerifier.create(result)
                .expectNext(documentStatus1)
                .expectNext(documentStatus2)
                .verifyComplete();
    }

    @Test
    void findStatus_whenFound() {
        // Arrange
        when(documentStatusRepository.findById(documentStatus1.getId())).thenReturn(Mono.just(documentStatus1));

        // Act
        Mono<DocumentStatus> result = documentStatusService.findStatus(documentStatus1.getId());

        // Assert
        StepVerifier.create(result)
                .expectNext(documentStatus1)
                .verifyComplete();
    }

    @Test
    void findStatus_whenNotFound() {
        // Arrange
        long nonExistentId = 99L;
        when(documentStatusRepository.findById(nonExistentId)).thenReturn(Mono.empty());

        // Act
        Mono<DocumentStatus> result = documentStatusService.findStatus(nonExistentId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete(); // Expect empty Mono
    }

    // Tests for add() will be added next
    @Test
    void add_success() {
        // Arrange
        DocumentStatus newStatus = new DocumentStatus();
        newStatus.setDocumentTypeId(2);
        newStatus.setFromStatusId(1);
        newStatus.setToStatusId(2);
        newStatus.setOrder(1);
        newStatus.setStatus("A");
        // ID, creationDate, modificationDate will be set by the service/repository mock

        // The lenient().when(documentStatusRepository.save(any(DocumentStatus.class))) in setUp will handle this
        // No need to redefine it here unless specific behavior for this test is needed for save.

        // Act
        Mono<DocumentStatus> result = documentStatusService.add(newStatus);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(savedStatus ->
                        savedStatus.getId() != null &&
                        savedStatus.getDocumentTypeId() == newStatus.getDocumentTypeId() &&
                        savedStatus.getCreationDate() != null &&
                        savedStatus.getModificationDate() != null &&
                        savedStatus.getStatus().equals("A"))
                .verifyComplete();
    }

    // Tests for update()
    @Test
    void update_success() {
        // Arrange
        Long existingId = documentStatus1.getId();
        DocumentStatus statusToUpdate = new DocumentStatus();
        statusToUpdate.setDocumentTypeId(1); // Keep same type
        statusToUpdate.setFromStatusId(1);   // Keep same fromStatus
        statusToUpdate.setToStatusId(5);     // Change toStatus
        statusToUpdate.setOrder(3);          // Change order
        statusToUpdate.setStatus("I");       // Change status to Inactive

        when(documentStatusRepository.findById(existingId)).thenReturn(Mono.just(documentStatus1));
        // The lenient save mock in setUp will handle the save operation and update modificationDate

        // Act
        Mono<DocumentStatus> result = documentStatusService.update(existingId, statusToUpdate);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(updatedStatus ->
                        updatedStatus.getId().equals(existingId) &&
                        updatedStatus.getToStatusId() == 5 &&
                        updatedStatus.getOrder() == 3 &&
                        updatedStatus.getStatus().equals("I") &&
                        updatedStatus.getModificationDate() != null &&
                        !updatedStatus.getModificationDate().isEqual(documentStatus1.getModificationDate())) // Check modificationDate changed
                .verifyComplete();
    }

    @Test
    void update_error_notFound() {
        // Arrange
        Long nonExistentId = 99L;
        DocumentStatus statusUpdateData = new DocumentStatus();
        statusUpdateData.setToStatusId(5); // Some data

        when(documentStatusRepository.findById(nonExistentId)).thenReturn(Mono.empty());

        // Act
        Mono<DocumentStatus> result = documentStatusService.update(nonExistentId, statusUpdateData);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class) // Assuming service throws RuntimeException on not found for update
                .verify();
    }

    // Tests for delete()
    @Test
    void delete_success() {
        // Arrange
        Long existingId = documentStatus1.getId(); // documentStatus1 has toStatusId = 2, which is < ATENDIDO_STATUS_ID (5)
                                               // This means it should NOT be system protected by default toStatusId rule.
                                               // Let's ensure documentStatus1.toStatusId is not protected for this test.
        documentStatus1.setToStatusId(ATENDIDO_STATUS_ID + 1); // Ensure it's not protected by toStatusId rule
                                                          // The service's delete logic checks existingDoc.getToStatusId()

        when(documentStatusRepository.findById(existingId)).thenReturn(Mono.just(documentStatus1));
        // The lenient save mock in setUp will handle the save operation

        // Act
        Mono<DocumentStatus> result = documentStatusService.delete(existingId);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(deletedStatus ->
                        deletedStatus.getId().equals(existingId) &&
                        deletedStatus.getStatus().equals("I")) // "I" for Inactive, assuming soft delete sets status
                .verifyComplete();
    }

    @Test
    void delete_error_notFound() {
        // Arrange
        Long nonExistentId = 99L;
        when(documentStatusRepository.findById(nonExistentId)).thenReturn(Mono.empty());

        // Act
        Mono<DocumentStatus> result = documentStatusService.delete(nonExistentId);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class) // Assuming service throws RuntimeException on not found for delete
                .verify();
    }

    @Test
    void delete_error_systemProtectedStatus_byToStatusId() {
        // Arrange
        // documentStatus1 has toStatusId = 2 by default in setUp. ATENDIDO_STATUS_ID is 5.
        // So documentStatus1.getToStatusId() (2) <= ATENDIDO_STATUS_ID (5) is true, so it should be protected.
        Long protectedId = documentStatus1.getId();
        documentStatus1.setToStatusId(ATENDIDO_STATUS_ID -1); // Explicitly set to be protected

        when(documentStatusRepository.findById(protectedId)).thenReturn(Mono.just(documentStatus1));
        // appProperties.getStatus().getAtendido() is already mocked in setUp to return ATENDIDO_STATUS_ID

        // Act
        Mono<DocumentStatus> result = documentStatusService.delete(protectedId);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }
     @Test
    void delete_success_when_toStatusId_is_greater_than_atendido() {
        // Arrange
        Long existingId = documentStatus2.getId(); // documentStatus2 has toStatusId = 3
        documentStatus2.setToStatusId(ATENDIDO_STATUS_ID + 1); // Make sure toStatusId is > atendido (5)

        when(documentStatusRepository.findById(existingId)).thenReturn(Mono.just(documentStatus2));

        // Act
        Mono<DocumentStatus> result = documentStatusService.delete(existingId);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(deletedStatus ->
                        deletedStatus.getId().equals(existingId) &&
                        deletedStatus.getStatus().equals("I")) // Status should be Inactive
                .verifyComplete();
    }
}
