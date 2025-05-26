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
import world.inclub.bo_legal_microservice.domain.Document;
import world.inclub.bo_legal_microservice.domain.DocumentHistory;
import world.inclub.bo_legal_microservice.domain.DocumentRates;
import world.inclub.bo_legal_microservice.domain.DocumentStatus;
import world.inclub.bo_legal_microservice.domain.dto.ChangeStatusDocumentDTO;
import world.inclub.bo_legal_microservice.domain.dto.DocumentDTO;
import world.inclub.bo_legal_microservice.domain.dto.DocumentRectificationDTO;
import world.inclub.bo_legal_microservice.domain.enums.DocumentStatusEnum;
import world.inclub.bo_legal_microservice.domain.enums.DocumentTypeEnum;
import world.inclub.bo_legal_microservice.domain.ports.in.DocumentRepository;
import world.inclub.bo_legal_microservice.domain.ports.in.DocumentHistoryRepository;
import world.inclub.bo_legal_microservice.domain.ports.in.DocumentStatusRepository;
import world.inclub.bo_legal_microservice.domain.ports.in.DocumentRatesRepository;
import world.inclub.bo_legal_microservice.infraestructure.config.AppProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentHistoryRepository documentHistoryRepository;

    @Mock
    private DocumentStatusRepository documentStatusRepository;

    @Mock
    private DocumentRatesRepository documentRatesRepository;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private DocumentService documentService;

    private DocumentDTO documentDTO;
    private DocumentRectificationDTO documentRectificationDTO;
    private Document document;
    private Document targetDocument;
    private DocumentRates documentRate;
    private AppProperties.Type mockTypes;
    private AppProperties.Status mockStatus;
    private DocumentStatus docStatusInitial;
    private DocumentStatus docStatusNext;


    @BeforeEach
    void setUp() {
        mockTypes = Mockito.mock(AppProperties.Type.class);
        mockStatus = Mockito.mock(AppProperties.Status.class);

        lenient().when(appProperties.getType()).thenReturn(mockTypes);
        lenient().when(appProperties.getStatus()).thenReturn(mockStatus);

        // Mock specific type values
        lenient().when(mockTypes.getSolicitudcertificado()).thenReturn(DocumentTypeEnum.SOLICITUD_CERTIFICADO.getId()); // 1
        lenient().when(mockTypes.getVoucherrectificacion()).thenReturn(DocumentTypeEnum.VOUCHER_RECTIFICACION.getId()); // 2
        lenient().when(mockTypes.getCertificado()).thenReturn(DocumentTypeEnum.CERTIFICADO.getId()); // 3
        lenient().when(mockTypes.getVoucheroperacion()).thenReturn(DocumentTypeEnum.VOUCHER_OPERACION.getId()); // 4


        // Mock specific status values
        lenient().when(mockStatus.getPendiente()).thenReturn(DocumentStatusEnum.PENDIENTE.getId()); // 1
        lenient().when(mockStatus.getAprobado()).thenReturn(DocumentStatusEnum.APROBADO.getId()); // 2
        lenient().when(mockStatus.getRechazado()).thenReturn(DocumentStatusEnum.RECHAZADO.getId()); // 3
        lenient().when(mockStatus.getProceso()).thenReturn(DocumentStatusEnum.PROCESO.getId()); // 4
        lenient().when(mockStatus.getAtendido()).thenReturn(DocumentStatusEnum.ATENDIDO.getId()); // 5
        lenient().when(mockStatus.getRecojo()).thenReturn(DocumentStatusEnum.RECOJO.getId()); // 6
        lenient().when(mockStatus.getFinalizado()).thenReturn(DocumentStatusEnum.FINALIZADO.getId()); // 7
        lenient().when(mockStatus.getAnulado()).thenReturn(DocumentStatusEnum.ANULADO.getId()); // 8


        documentDTO = new DocumentDTO();
        documentDTO.setUuid(UUID.randomUUID().toString());
        documentDTO.setExternalId("ext-123");
        documentDTO.setPaymentId("pay-123");
        documentDTO.setDocumentTypeId(DocumentTypeEnum.SOLICITUD_CERTIFICADO.getId());
        documentDTO.setRateId(1L);
        documentDTO.setAmount(BigDecimal.TEN);
        documentDTO.setUserId("user-123");
        documentDTO.setUserName("Test User");
        documentDTO.setUserEmail("test@example.com");
        documentDTO.setUserPhone("123456789");
        documentDTO.setCompanyId("comp-123");
        documentDTO.setCompanyName("Test Company");
        documentDTO.setPaymentDate(LocalDateTime.now().toString());
        documentDTO.setCreationDate(LocalDateTime.now().toString());
        documentDTO.setDetails(Collections.singletonMap("key", "value"));

        documentRate = new DocumentRates();
        documentRate.setId(1L);
        documentRate.setRate(BigDecimal.TEN);
        documentRate.setDescription("Test Rate");
        documentRate.setDocumentTypeId(DocumentTypeEnum.SOLICITUD_CERTIFICADO.getId());
        documentRate.setStatus("A"); // Active

        document = new Document();
        document.setId(1L);
        document.setUuid(documentDTO.getUuid());
        document.setDocumentTypeId(documentDTO.getDocumentTypeId());
        document.setDocumentStatusId(mockStatus.getPendiente());
        document.setRateId(documentDTO.getRateId());
        document.setAmount(documentDTO.getAmount());
        document.setUserId(documentDTO.getUserId());
        document.setUserName(documentDTO.getUserName());
        document.setUserEmail(documentDTO.getUserEmail());
        document.setUserPhone(documentDTO.getUserPhone());
        document.setCompanyId(documentDTO.getCompanyId());
        document.setCompanyName(documentDTO.getCompanyName());
        document.setExternalId(documentDTO.getExternalId());
        document.setPaymentId(documentDTO.getPaymentId());
        document.setPaymentDate(LocalDateTime.parse(documentDTO.getPaymentDate()));
        document.setCreationDate(LocalDateTime.parse(documentDTO.getCreationDate()));
        document.setModificationDate(LocalDateTime.now());
        document.setDetails(documentDTO.getDetails());

        targetDocument = new Document();
        targetDocument.setId(2L);
        targetDocument.setUuid(UUID.randomUUID().toString());
        targetDocument.setDocumentTypeId(mockTypes.getCertificado()); // Certificado
        targetDocument.setDocumentStatusId(mockStatus.getAtendido()); // Atendido - allows rectification
        targetDocument.setUserId("user-target");

        documentRectificationDTO = new DocumentRectificationDTO();
        documentRectificationDTO.setUuid(UUID.randomUUID().toString());
        documentRectificationDTO.setTargetDocumentUuid(targetDocument.getUuid());
        documentRectificationDTO.setPaymentId("pay-rect-123");
        documentRectificationDTO.setRateId(2L); // Assuming a different rate for rectification
        documentRectificationDTO.setAmount(BigDecimal.ONE);
        documentRectificationDTO.setUserId("user-rect");
        documentRectificationDTO.setUserName("Rect User");
        documentRectificationDTO.setUserEmail("rect@example.com");
        documentRectificationDTO.setUserPhone("987654321");
        documentRectificationDTO.setCompanyId("comp-rect");
        documentRectificationDTO.setCompanyName("Rect Company");
        documentRectificationDTO.setPaymentDate(LocalDateTime.now().toString());
        documentRectificationDTO.setCreationDate(LocalDateTime.now().toString());
        documentRectificationDTO.setDetails(Collections.singletonMap("reason", "correction"));

        docStatusInitial = new DocumentStatus();
        docStatusInitial.setId(1L);
        docStatusInitial.setDocumentTypeId(DocumentTypeEnum.SOLICITUD_CERTIFICADO.getId());
        docStatusInitial.setFromStatusId(mockStatus.getPendiente());
        docStatusInitial.setToStatusId(mockStatus.getAprobado());
        docStatusInitial.setOrder(1);
        docStatusInitial.setStatus("A");

        docStatusNext = new DocumentStatus();
        docStatusNext.setId(2L);
        docStatusNext.setDocumentTypeId(DocumentTypeEnum.SOLICITUD_CERTIFICADO.getId());
        docStatusNext.setFromStatusId(mockStatus.getAprobado());
        docStatusNext.setToStatusId(mockStatus.getProceso());
        docStatusNext.setOrder(2);
        docStatusNext.setStatus("A");


        // Default lenient stubs for repository saves
        lenient().when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document docToSave = invocation.getArgument(0);
            if (docToSave.getId() == null) {
                docToSave.setId(System.currentTimeMillis()); // Simulate ID generation
            }
            return Mono.just(docToSave);
        });
        lenient().when(documentHistoryRepository.save(any(DocumentHistory.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    @Test
    void findAll_shouldReturnAllDocuments() {
        // Arrange
        Document doc1 = new Document();
        doc1.setId(1L);
        doc1.setUuid(UUID.randomUUID().toString());
        Document doc2 = new Document();
        doc2.setId(2L);
        doc2.setUuid(UUID.randomUUID().toString());

        when(documentRepository.findAll()).thenReturn(Flux.just(doc1, doc2));

        // Act
        Flux<Document> result = documentService.findAll();

        // Assert
        StepVerifier.create(result)
                .expectNext(doc1)
                .expectNext(doc2)
                .verifyComplete();
    }

    // Placeholder for addDocumentSolicitud tests
    @Test
    void addDocumentSolicitud_success() {
        // Arrange
        when(documentRatesRepository.findById(documentDTO.getRateId())).thenReturn(Mono.just(documentRate));
        when(documentRepository.findByExternalIdAndDocumentTypeId(documentDTO.getExternalId(), documentDTO.getDocumentTypeId()))
                .thenReturn(Mono.empty());
        // Simulate initial status lookup for PENDIENTE
        when(documentStatusRepository.findByDocumentTypeIdAndOrderAndStatus(
                eq(mockTypes.getSolicitudcertificado()), eq(1), eq("A")))
                .thenReturn(Mono.just(docStatusInitial)); // docStatusInitial is configured with PENDIENTE

        // Act
        Mono<Document> result = documentService.addDocumentSolicitud(documentDTO);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(savedDoc -> {
                    return savedDoc.getUuid().equals(documentDTO.getUuid()) &&
                           savedDoc.getDocumentStatusId().equals(docStatusInitial.getFromStatusId()) && // Check it's set to PENDIENTE
                           savedDoc.getDocumentTypeId().equals(mockTypes.getSolicitudcertificado());
                })
                .verifyComplete();
    }

    @Test
    void addDocumentSolicitud_error_documentTypeNotPermitted() {
        // Arrange
        documentDTO.setDocumentTypeId(mockTypes.getCertificado()); // Not a SOLICITUD_CERTIFICADO

        // Act
        Mono<Document> result = documentService.addDocumentSolicitud(documentDTO);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void addDocumentSolicitud_error_rateNotFound() {
        // Arrange
        when(documentRatesRepository.findById(documentDTO.getRateId())).thenReturn(Mono.empty());

        // Act
        Mono<Document> result = documentService.addDocumentSolicitud(documentDTO);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void addDocumentSolicitud_error_rateNotActive() {
        // Arrange
        documentRate.setStatus("I"); // Inactive
        when(documentRatesRepository.findById(documentDTO.getRateId())).thenReturn(Mono.just(documentRate));

        // Act
        Mono<Document> result = documentService.addDocumentSolicitud(documentDTO);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void addDocumentSolicitud_error_documentAlreadyExists() {
        // Arrange
        when(documentRatesRepository.findById(documentDTO.getRateId())).thenReturn(Mono.just(documentRate));
        when(documentRepository.findByExternalIdAndDocumentTypeId(documentDTO.getExternalId(), documentDTO.getDocumentTypeId()))
                .thenReturn(Mono.just(document)); // Document already exists

        // Act
        Mono<Document> result = documentService.addDocumentSolicitud(documentDTO);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void addDocumentSolicitud_error_initialStatusNotFound() {
        // Arrange
        when(documentRatesRepository.findById(documentDTO.getRateId())).thenReturn(Mono.just(documentRate));
        when(documentRepository.findByExternalIdAndDocumentTypeId(documentDTO.getExternalId(), documentDTO.getDocumentTypeId()))
                .thenReturn(Mono.empty());
        when(documentStatusRepository.findByDocumentTypeIdAndOrderAndStatus(documentDTO.getDocumentTypeId(), 1, "A"))
                .thenReturn(Mono.empty()); // Initial status not found

        // Act
        Mono<Document> result = documentService.addDocumentSolicitud(documentDTO);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    // More tests will be added in subsequent steps

    @Test
    void addDocumentRectificacion_success() {
        // Arrange
        DocumentRates rectificationRate = new DocumentRates();
        rectificationRate.setId(documentRectificationDTO.getRateId());
        rectificationRate.setStatus("A");
        rectificationRate.setDocumentTypeId(mockTypes.getVoucherrectificacion());


        when(documentRepository.findByUuid(documentRectificationDTO.getTargetDocumentUuid())).thenReturn(Mono.just(targetDocument));
        // targetDocument is CERTIFICADO and ATENDIDO, which is valid for rectification
        when(documentRepository.findByTargetDocumentUuidAndDocumentStatusId(
                documentRectificationDTO.getTargetDocumentUuid(), mockStatus.getPendiente()))
                .thenReturn(Mono.empty()); // No pending rectification
        when(documentRatesRepository.findById(documentRectificationDTO.getRateId())).thenReturn(Mono.just(rectificationRate));

        DocumentStatus initialRectificationStatus = new DocumentStatus();
        initialRectificationStatus.setDocumentTypeId(mockTypes.getVoucherrectificacion());
        initialRectificationStatus.setFromStatusId(mockStatus.getPendiente()); // Should start as PENDIENTE
        initialRectificationStatus.setOrder(1);

        when(documentStatusRepository.findByDocumentTypeIdAndOrderAndStatus(
                eq(mockTypes.getVoucherrectificacion()), eq(1), eq("A")))
                .thenReturn(Mono.just(initialRectificationStatus));


        // Act
        Mono<Document> result = documentService.addDocumentRectificacion(documentRectificationDTO);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(savedDoc ->
                        savedDoc.getUuid().equals(documentRectificationDTO.getUuid()) &&
                        savedDoc.getDocumentTypeId().equals(mockTypes.getVoucherrectificacion()) &&
                        savedDoc.getTargetDocumentUuid().equals(documentRectificationDTO.getTargetDocumentUuid()) &&
                        savedDoc.getDocumentStatusId().equals(mockStatus.getPendiente()))
                .verifyComplete();
    }

    @Test
    void addDocumentRectificacion_error_pendingRectificationExists() {
        // Arrange
        when(documentRepository.findByUuid(documentRectificationDTO.getTargetDocumentUuid())).thenReturn(Mono.just(targetDocument));
        when(documentRepository.findByTargetDocumentUuidAndDocumentStatusId(
                documentRectificationDTO.getTargetDocumentUuid(), mockStatus.getPendiente()))
                .thenReturn(Mono.just(new Document())); // Pending rectification exists

        // Act
        Mono<Document> result = documentService.addDocumentRectificacion(documentRectificationDTO);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void addDocumentRectificacion_error_targetDocumentNotFound() {
        // Arrange
        when(documentRepository.findByUuid(documentRectificationDTO.getTargetDocumentUuid())).thenReturn(Mono.empty());

        // Act
        Mono<Document> result = documentService.addDocumentRectificacion(documentRectificationDTO);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void addDocumentRectificacion_error_targetDocumentTypeInvalid() {
        // Arrange
        targetDocument.setDocumentTypeId(mockTypes.getSolicitudcertificado()); // Not a CERTIFICADO
        when(documentRepository.findByUuid(documentRectificationDTO.getTargetDocumentUuid())).thenReturn(Mono.just(targetDocument));

        // Act
        Mono<Document> result = documentService.addDocumentRectificacion(documentRectificationDTO);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void addDocumentRectificacion_error_targetDocumentStatusInvalid() {
        // Arrange
        targetDocument.setDocumentStatusId(mockStatus.getProceso()); // Not ATENDIDO or RECOJO
        when(documentRepository.findByUuid(documentRectificationDTO.getTargetDocumentUuid())).thenReturn(Mono.just(targetDocument));
        when(documentRepository.findByTargetDocumentUuidAndDocumentStatusId(
                documentRectificationDTO.getTargetDocumentUuid(), mockStatus.getPendiente()))
                .thenReturn(Mono.empty());

        // Act
        Mono<Document> result = documentService.addDocumentRectificacion(documentRectificationDTO);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void addDocumentRectificacion_error_rateNotFound() {
        // Arrange
        when(documentRepository.findByUuid(documentRectificationDTO.getTargetDocumentUuid())).thenReturn(Mono.just(targetDocument));
        when(documentRepository.findByTargetDocumentUuidAndDocumentStatusId(
                documentRectificationDTO.getTargetDocumentUuid(), mockStatus.getPendiente()))
                .thenReturn(Mono.empty());
        when(documentRatesRepository.findById(documentRectificationDTO.getRateId())).thenReturn(Mono.empty());

        // Act
        Mono<Document> result = documentService.addDocumentRectificacion(documentRectificationDTO);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void addDocumentRectificacion_error_rateNotActive() {
        // Arrange
        DocumentRates rectificationRate = new DocumentRates();
        rectificationRate.setId(documentRectificationDTO.getRateId());
        rectificationRate.setStatus("I"); // Inactive
        rectificationRate.setDocumentTypeId(mockTypes.getVoucherrectificacion());

        when(documentRepository.findByUuid(documentRectificationDTO.getTargetDocumentUuid())).thenReturn(Mono.just(targetDocument));
        when(documentRepository.findByTargetDocumentUuidAndDocumentStatusId(
                documentRectificationDTO.getTargetDocumentUuid(), mockStatus.getPendiente()))
                .thenReturn(Mono.empty());
        when(documentRatesRepository.findById(documentRectificationDTO.getRateId())).thenReturn(Mono.just(rectificationRate));

        // Act
        Mono<Document> result = documentService.addDocumentRectificacion(documentRectificationDTO);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }
    
    @Test
    void addDocumentRectificacion_error_initialStatusNotFound() {
        // Arrange
        DocumentRates rectificationRate = new DocumentRates();
        rectificationRate.setId(documentRectificationDTO.getRateId());
        rectificationRate.setStatus("A");
        rectificationRate.setDocumentTypeId(mockTypes.getVoucherrectificacion());

        when(documentRepository.findByUuid(documentRectificationDTO.getTargetDocumentUuid())).thenReturn(Mono.just(targetDocument));
        when(documentRepository.findByTargetDocumentUuidAndDocumentStatusId(
                documentRectificationDTO.getTargetDocumentUuid(), mockStatus.getPendiente()))
                .thenReturn(Mono.empty());
        when(documentRatesRepository.findById(documentRectificationDTO.getRateId())).thenReturn(Mono.just(rectificationRate));
        when(documentStatusRepository.findByDocumentTypeIdAndOrderAndStatus(
                eq(mockTypes.getVoucherrectificacion()), eq(1), eq("A")))
                .thenReturn(Mono.empty()); // Initial status not found

        // Act
        Mono<Document> result = documentService.addDocumentRectificacion(documentRectificationDTO);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    // Tests for changeStatusDocument
    @Test
    void changeStatusDocument_success() {
        // Arrange
        ChangeStatusDocumentDTO changeStatusDTO = new ChangeStatusDocumentDTO();
        changeStatusDTO.setUuid(document.getUuid());
        changeStatusDTO.setNewStatusId(mockStatus.getAprobado()); // Target: APROBADO
        changeStatusDTO.setObservation("Approved by test");
        changeStatusDTO.setUserId("test-user");

        document.setDocumentStatusId(mockStatus.getPendiente()); // Current: PENDIENTE

        DocumentStatus transition = new DocumentStatus();
        transition.setDocumentTypeId(document.getDocumentTypeId());
        transition.setFromStatusId(mockStatus.getPendiente());
        transition.setToStatusId(mockStatus.getAprobado());
        transition.setStatus("A");

        when(documentRepository.findByUuid(changeStatusDTO.getUuid())).thenReturn(Mono.just(document));
        when(documentStatusRepository.findByDocumentTypeIdAndFromStatusIdAndToStatusIdAndStatus(
                document.getDocumentTypeId(), document.getDocumentStatusId(), changeStatusDTO.getNewStatusId(), "A"))
                .thenReturn(Mono.just(transition));

        // Act
        Mono<Document> result = documentService.changeStatusDocument(changeStatusDTO);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(updatedDoc ->
                        updatedDoc.getUuid().equals(document.getUuid()) &&
                        updatedDoc.getDocumentStatusId().equals(mockStatus.getAprobado()))
                .verifyComplete();
    }

    @Test
    void changeStatusDocument_error_documentNotFound() {
        // Arrange
        ChangeStatusDocumentDTO changeStatusDTO = new ChangeStatusDocumentDTO();
        changeStatusDTO.setUuid(UUID.randomUUID().toString()); // Non-existent UUID
        changeStatusDTO.setNewStatusId(mockStatus.getAprobado());

        when(documentRepository.findByUuid(changeStatusDTO.getUuid())).thenReturn(Mono.empty());

        // Act
        Mono<Document> result = documentService.changeStatusDocument(changeStatusDTO);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class) // Assuming service throws RuntimeException
                .verify();
    }

    @Test
    void changeStatusDocument_error_alreadyInRequestedStatus() {
        // Arrange
        ChangeStatusDocumentDTO changeStatusDTO = new ChangeStatusDocumentDTO();
        changeStatusDTO.setUuid(document.getUuid());
        changeStatusDTO.setNewStatusId(mockStatus.getPendiente()); // Already in PENDIENTE

        document.setDocumentStatusId(mockStatus.getPendiente());

        when(documentRepository.findByUuid(changeStatusDTO.getUuid())).thenReturn(Mono.just(document));

        // Act
        Mono<Document> result = documentService.changeStatusDocument(changeStatusDTO);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class) // Assuming service throws RuntimeException
                .verify();
    }

    @Test
    void changeStatusDocument_error_statusTransitionNotAllowed() {
        // Arrange
        ChangeStatusDocumentDTO changeStatusDTO = new ChangeStatusDocumentDTO();
        changeStatusDTO.setUuid(document.getUuid());
        changeStatusDTO.setNewStatusId(mockStatus.getFinalizado()); // Target: FINALIZADO

        document.setDocumentStatusId(mockStatus.getPendiente()); // Current: PENDIENTE

        when(documentRepository.findByUuid(changeStatusDTO.getUuid())).thenReturn(Mono.just(document));
        when(documentStatusRepository.findByDocumentTypeIdAndFromStatusIdAndToStatusIdAndStatus(
                document.getDocumentTypeId(), document.getDocumentStatusId(), changeStatusDTO.getNewStatusId(), "A"))
                .thenReturn(Mono.empty()); // No such transition configured

        // Act
        Mono<Document> result = documentService.changeStatusDocument(changeStatusDTO);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class) // Assuming service throws RuntimeException
                .verify();
    }

    @Test
    void changeStatusDocument_error_documentInFinalStatus_anulado() {
        // Arrange
        ChangeStatusDocumentDTO changeStatusDTO = new ChangeStatusDocumentDTO();
        changeStatusDTO.setUuid(document.getUuid());
        changeStatusDTO.setNewStatusId(mockStatus.getAprobado());

        document.setDocumentStatusId(mockStatus.getAnulado()); // Current: ANULADO (final)

        when(documentRepository.findByUuid(changeStatusDTO.getUuid())).thenReturn(Mono.just(document));

        // Act
        Mono<Document> result = documentService.changeStatusDocument(changeStatusDTO);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void changeStatusDocument_error_documentInFinalStatus_finalizado() {
        // Arrange
        ChangeStatusDocumentDTO changeStatusDTO = new ChangeStatusDocumentDTO();
        changeStatusDTO.setUuid(document.getUuid());
        changeStatusDTO.setNewStatusId(mockStatus.getAprobado());

        document.setDocumentStatusId(mockStatus.getFinalizado()); // Current: FINALIZADO (final)

        when(documentRepository.findByUuid(changeStatusDTO.getUuid())).thenReturn(Mono.just(document));

        // Act
        Mono<Document> result = documentService.changeStatusDocument(changeStatusDTO);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    // Tests for approveDocument
    @Test
    void approveDocument_solicitud_success() {
        // Arrange
        document.setDocumentStatusId(mockStatus.getPendiente()); // Current: PENDIENTE
        document.setDocumentTypeId(mockTypes.getSolicitudcertificado());

        ChangeStatusDocumentDTO approvalDto = new ChangeStatusDocumentDTO(document.getUuid(), "Approved", "test-user");

        DocumentStatus approvedTransition = new DocumentStatus();
        approvedTransition.setToStatusId(mockStatus.getAprobado());

        when(documentRepository.findByUuid(document.getUuid())).thenReturn(Mono.just(document));
        when(documentStatusRepository.findByDocumentTypeIdAndFromStatusIdAndToStatusIdAndStatus(
                eq(document.getDocumentTypeId()), eq(mockStatus.getPendiente()), eq(mockStatus.getAprobado()), eq("A")))
                .thenReturn(Mono.just(approvedTransition));

        // Act
        Mono<Document> result = documentService.approveDocument(approvalDto);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(approvedDoc ->
                        approvedDoc.getDocumentStatusId().equals(mockStatus.getAprobado()))
                .verifyComplete();
    }

    @Test
    void approveDocument_rectificacion_success() {
        // Arrange
        Document rectificationVoucher = new Document();
        rectificationVoucher.setId(3L);
        rectificationVoucher.setUuid(UUID.randomUUID().toString());
        rectificationVoucher.setDocumentTypeId(mockTypes.getVoucherrectificacion());
        rectificationVoucher.setDocumentStatusId(mockStatus.getPendiente());
        rectificationVoucher.setTargetDocumentUuid(targetDocument.getUuid()); // targetDocument is CERTIFICADO, ATENDIDO

        targetDocument.setDocumentStatusId(mockStatus.getAtendido()); // Target document is ATENDIDO

        ChangeStatusDocumentDTO approvalDto = new ChangeStatusDocumentDTO(rectificationVoucher.getUuid(), "Rectification Approved", "test-user");

        DocumentStatus voucherApprovedTransition = new DocumentStatus();
        voucherApprovedTransition.setToStatusId(mockStatus.getAprobado());

        DocumentStatus targetProcessedTransition = new DocumentStatus(); // Transition for target document to PROCESO
        targetProcessedTransition.setToStatusId(mockStatus.getProceso());


        when(documentRepository.findByUuid(rectificationVoucher.getUuid())).thenReturn(Mono.just(rectificationVoucher));
        when(documentRepository.findByUuid(targetDocument.getUuid())).thenReturn(Mono.just(targetDocument));

        // Mock transition for voucher to APROBADO
        when(documentStatusRepository.findByDocumentTypeIdAndFromStatusIdAndToStatusIdAndStatus(
                eq(mockTypes.getVoucherrectificacion()), eq(mockStatus.getPendiente()), eq(mockStatus.getAprobado()), eq("A")))
                .thenReturn(Mono.just(voucherApprovedTransition));

        // Mock transition for target document (CERTIFICADO) from ATENDIDO to PROCESO
        when(documentStatusRepository.findByDocumentTypeIdAndFromStatusIdAndToStatusIdAndStatus(
                eq(targetDocument.getDocumentTypeId()), eq(mockStatus.getAtendido()), eq(mockStatus.getProceso()), eq("A")))
                .thenReturn(Mono.just(targetProcessedTransition));


        // Act
        Mono<Document> result = documentService.approveDocument(approvalDto);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(approvedVoucher ->
                        approvedVoucher.getDocumentStatusId().equals(mockStatus.getAprobado()))
                .verifyComplete();

        // Verify target document was also updated (this happens in a flatMap, so a bit harder to test directly without capturing args)
        // For simplicity, we assume if the voucher approval succeeds, the target update was attempted.
        // A more robust test might involve ArgumentCaptor on documentRepository.save() for the target document.
    }


    @Test
    void approveDocument_error_documentNotFound() {
        // Arrange
        ChangeStatusDocumentDTO approvalDto = new ChangeStatusDocumentDTO(UUID.randomUUID().toString(), "Approve non-existent", "test-user");
        when(documentRepository.findByUuid(approvalDto.getUuid())).thenReturn(Mono.empty());

        // Act
        Mono<Document> result = documentService.approveDocument(approvalDto);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void approveDocument_error_notPending() {
        // Arrange
        document.setDocumentStatusId(mockStatus.getAprobado()); // Already APROBADO
        ChangeStatusDocumentDTO approvalDto = new ChangeStatusDocumentDTO(document.getUuid(), "Approve non-pending", "test-user");
        when(documentRepository.findByUuid(document.getUuid())).thenReturn(Mono.just(document));

        // Act
        Mono<Document> result = documentService.approveDocument(approvalDto);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void approveDocument_error_invalidTypeForApproval() {
        // Arrange
        document.setDocumentTypeId(mockTypes.getVoucheroperacion()); // Some other type
        document.setDocumentStatusId(mockStatus.getPendiente());
        ChangeStatusDocumentDTO approvalDto = new ChangeStatusDocumentDTO(document.getUuid(), "Approve invalid type", "test-user");
        when(documentRepository.findByUuid(document.getUuid())).thenReturn(Mono.just(document));

        // Act
        Mono<Document> result = documentService.approveDocument(approvalDto);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void approveDocument_rectificacion_error_targetDocumentNotFound() {
        // Arrange
        Document rectificationVoucher = new Document();
        rectificationVoucher.setUuid(UUID.randomUUID().toString());
        rectificationVoucher.setDocumentTypeId(mockTypes.getVoucherrectificacion());
        rectificationVoucher.setDocumentStatusId(mockStatus.getPendiente());
        rectificationVoucher.setTargetDocumentUuid(UUID.randomUUID().toString()); // Non-existent target

        ChangeStatusDocumentDTO approvalDto = new ChangeStatusDocumentDTO(rectificationVoucher.getUuid(), "Approve rect with no target", "test-user");

        when(documentRepository.findByUuid(rectificationVoucher.getUuid())).thenReturn(Mono.just(rectificationVoucher));
        when(documentRepository.findByUuid(rectificationVoucher.getTargetDocumentUuid())).thenReturn(Mono.empty()); // Target not found

        // Act
        Mono<Document> result = documentService.approveDocument(approvalDto);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void approveDocument_rectificacion_error_targetDocumentInFinalStatus() {
        // Arrange
        Document rectificationVoucher = new Document();
        rectificationVoucher.setUuid(UUID.randomUUID().toString());
        rectificationVoucher.setDocumentTypeId(mockTypes.getVoucherrectificacion());
        rectificationVoucher.setDocumentStatusId(mockStatus.getPendiente());
        rectificationVoucher.setTargetDocumentUuid(targetDocument.getUuid());

        targetDocument.setDocumentStatusId(mockStatus.getFinalizado()); // Target in final status

        ChangeStatusDocumentDTO approvalDto = new ChangeStatusDocumentDTO(rectificationVoucher.getUuid(), "Approve rect with final target", "test-user");

        when(documentRepository.findByUuid(rectificationVoucher.getUuid())).thenReturn(Mono.just(rectificationVoucher));
        when(documentRepository.findByUuid(targetDocument.getUuid())).thenReturn(Mono.just(targetDocument));

        // Act
        Mono<Document> result = documentService.approveDocument(approvalDto);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void approveDocument_error_approvalStatusTransitionNotFound() {
        // Arrange
        document.setDocumentStatusId(mockStatus.getPendiente());
        document.setDocumentTypeId(mockTypes.getSolicitudcertificado());
        ChangeStatusDocumentDTO approvalDto = new ChangeStatusDocumentDTO(document.getUuid(), "Approve no transition", "test-user");

        when(documentRepository.findByUuid(document.getUuid())).thenReturn(Mono.just(document));
        when(documentStatusRepository.findByDocumentTypeIdAndFromStatusIdAndToStatusIdAndStatus(
                anyInt(), eq(mockStatus.getPendiente()), eq(mockStatus.getAprobado()), eq("A")))
                .thenReturn(Mono.empty()); // No transition to APROBADO found

        // Act
        Mono<Document> result = documentService.approveDocument(approvalDto);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void approveDocument_rectificacion_error_targetProcessStatusTransitionNotFound() {
        // Arrange
        Document rectificationVoucher = new Document();
        rectificationVoucher.setId(3L);
        rectificationVoucher.setUuid(UUID.randomUUID().toString());
        rectificationVoucher.setDocumentTypeId(mockTypes.getVoucherrectificacion());
        rectificationVoucher.setDocumentStatusId(mockStatus.getPendiente());
        rectificationVoucher.setTargetDocumentUuid(targetDocument.getUuid());

        targetDocument.setDocumentStatusId(mockStatus.getAtendido());

        ChangeStatusDocumentDTO approvalDto = new ChangeStatusDocumentDTO(rectificationVoucher.getUuid(), "Approve rect no target transition", "test-user");

        DocumentStatus voucherApprovedTransition = new DocumentStatus();
        voucherApprovedTransition.setToStatusId(mockStatus.getAprobado());

        when(documentRepository.findByUuid(rectificationVoucher.getUuid())).thenReturn(Mono.just(rectificationVoucher));
        when(documentRepository.findByUuid(targetDocument.getUuid())).thenReturn(Mono.just(targetDocument));
        when(documentStatusRepository.findByDocumentTypeIdAndFromStatusIdAndToStatusIdAndStatus(
                eq(mockTypes.getVoucherrectificacion()), eq(mockStatus.getPendiente()), eq(mockStatus.getAprobado()), eq("A")))
                .thenReturn(Mono.just(voucherApprovedTransition)); // Voucher approval transition is fine
        when(documentStatusRepository.findByDocumentTypeIdAndFromStatusIdAndToStatusIdAndStatus(
                eq(targetDocument.getDocumentTypeId()), eq(mockStatus.getAtendido()), eq(mockStatus.getProceso()), eq("A")))
                .thenReturn(Mono.empty()); // No transition for target document to PROCESO

        // Act
        Mono<Document> result = documentService.approveDocument(approvalDto);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    // Tests for rejectDocument
    @Test
    void rejectDocument_success() {
        // Arrange
        document.setDocumentStatusId(mockStatus.getPendiente()); // Current: PENDIENTE
        document.setDocumentTypeId(mockTypes.getSolicitudcertificado()); // Example type

        ChangeStatusDocumentDTO rejectionDto = new ChangeStatusDocumentDTO(document.getUuid(), "Rejected by test", "test-user");

        DocumentStatus rejectedTransition = new DocumentStatus();
        rejectedTransition.setToStatusId(mockStatus.getRechazado());

        when(documentRepository.findByUuid(document.getUuid())).thenReturn(Mono.just(document));
        when(documentStatusRepository.findByDocumentTypeIdAndFromStatusIdAndToStatusIdAndStatus(
                eq(document.getDocumentTypeId()), eq(mockStatus.getPendiente()), eq(mockStatus.getRechazado()), eq("A")))
                .thenReturn(Mono.just(rejectedTransition));

        // Act
        Mono<Document> result = documentService.rejectDocument(rejectionDto);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(rejectedDoc ->
                        rejectedDoc.getDocumentStatusId().equals(mockStatus.getRechazado()))
                .verifyComplete();
    }

    @Test
    void rejectDocument_error_documentNotFound() {
        // Arrange
        ChangeStatusDocumentDTO rejectionDto = new ChangeStatusDocumentDTO(UUID.randomUUID().toString(), "Reject non-existent", "test-user");
        when(documentRepository.findByUuid(rejectionDto.getUuid())).thenReturn(Mono.empty());

        // Act
        Mono<Document> result = documentService.rejectDocument(rejectionDto);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void rejectDocument_error_notPending() {
        // Arrange
        document.setDocumentStatusId(mockStatus.getAprobado()); // Not PENDIENTE
        ChangeStatusDocumentDTO rejectionDto = new ChangeStatusDocumentDTO(document.getUuid(), "Reject non-pending", "test-user");
        when(documentRepository.findByUuid(document.getUuid())).thenReturn(Mono.just(document));

        // Act
        Mono<Document> result = documentService.rejectDocument(rejectionDto);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void rejectDocument_error_invalidTypeForRejection() {
        // Arrange
        // Assuming VOUCHER_OPERACION is not directly rejectable this way or requires a different flow.
        // This test depends heavily on the actual business logic for what types are "rejectable".
        // For this example, let's assume only SOLICITUD_CERTIFICADO and VOUCHER_RECTIFICACION can be rejected via this method.
        document.setDocumentTypeId(mockTypes.getVoucheroperacion()); // An example of a type that might not be rejectable.
        document.setDocumentStatusId(mockStatus.getPendiente());
        ChangeStatusDocumentDTO rejectionDto = new ChangeStatusDocumentDTO(document.getUuid(), "Reject invalid type", "test-user");
        
        // Simulate that the service checks the type before attempting status change
        // This part of the mock might not be hit if the service checks type and throws error before status lookup
        when(documentRepository.findByUuid(document.getUuid())).thenReturn(Mono.just(document));
        
        // If the service's `rejectDocument` has a guard for document type before calling `changeStatusDocument`,
        // then the `documentStatusRepository` mock for RECHAZADO might not be needed for this specific error case.
        // However, if it calls `changeStatusDocument` which then fails due to no valid transition for this type to RECHAZADO,
        // then the `documentStatusRepository` mock returning empty would be the cause of failure.

        // For robustness, let's assume the service has a check. If not, the `rejectionStatusTransitionNotFound`
        // test might cover this scenario if no transition to RECHAZADO is defined for VOUCHER_OPERACION.

        // Act
        Mono<Document> result = documentService.rejectDocument(rejectionDto);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class) // Expecting an error due to invalid type for this operation
                .verify();
    }


    @Test
    void rejectDocument_error_rejectionStatusTransitionNotFound() {
        // Arrange
        document.setDocumentStatusId(mockStatus.getPendiente());
        document.setDocumentTypeId(mockTypes.getSolicitudcertificado()); // A type that should be rejectable
        ChangeStatusDocumentDTO rejectionDto = new ChangeStatusDocumentDTO(document.getUuid(), "Reject no transition", "test-user");

        when(documentRepository.findByUuid(document.getUuid())).thenReturn(Mono.just(document));
        when(documentStatusRepository.findByDocumentTypeIdAndFromStatusIdAndToStatusIdAndStatus(
                eq(document.getDocumentTypeId()), eq(mockStatus.getPendiente()), eq(mockStatus.getRechazado()), eq("A")))
                .thenReturn(Mono.empty()); // No transition to RECHAZADO found

        // Act
        Mono<Document> result = documentService.rejectDocument(rejectionDto);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }
}
