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
import world.inclub.bo_legal_microservice.domain.models.DocumentRates;
import world.inclub.bo_legal_microservice.domain.models.Rates;
import world.inclub.bo_legal_microservice.domain.request.RateRequest;
import world.inclub.bo_legal_microservice.infraestructure.repositories.DocumentRatesRepository;
import world.inclub.bo_legal_microservice.infraestructure.repositories.RatesRepository;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateServiceTest {

    @Mock
    private DocumentRatesRepository documentRatesRepository;

    @Mock
    private RatesRepository ratesRepository;

    @InjectMocks
    private RateService rateService;

    private DocumentRates documentRate1;
    private DocumentRates documentRate2;
    private Rates rate1;
    private RateRequest rateRequest1;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        documentRate1 = new DocumentRates();
        documentRate1.setId(1);
        documentRate1.setLegalType(1);
        documentRate1.setLegalName("Legal Type A");
        documentRate1.setDocumentType(10);
        documentRate1.setDocumentName("Document Type X");
        documentRate1.setLocalType(100);
        documentRate1.setLocalName("Local Type Alpha");
        documentRate1.setPrice(150.75f);
        documentRate1.setStatus(1); // Active

        documentRate2 = new DocumentRates();
        documentRate2.setId(2);
        documentRate2.setLegalType(2);
        documentRate2.setLegalName("Legal Type B");
        documentRate2.setDocumentType(20);
        documentRate2.setDocumentName("Document Type Y");
        documentRate2.setLocalType(200);
        documentRate2.setLocalName("Local Type Beta");
        documentRate2.setPrice(250.50f);
        documentRate2.setStatus(1); // Active

        rate1 = new Rates();
        rate1.setId(1);
        rate1.setLegalizationType(1);
        rate1.setDocumentTypeId(10);
        rate1.setLocalType(100);
        rate1.setPrice(150.75f);
        rate1.setStatus(1); // Active
        rate1.setCreatedAt(now.minusDays(1));
        rate1.setModifiedAt(now.minusHours(12));

        rateRequest1 = new RateRequest();
        rateRequest1.setLegalType(1);
        rateRequest1.setDocumentType(10);
        rateRequest1.setLocalType(100);
        rateRequest1.setPrice(160.00f);
        // rateRequest1.id is not used by service's update method as per analysis

        // Lenient stubbing for save operations
        lenient().when(ratesRepository.save(any(Rates.class))).thenAnswer(invocation -> {
            Rates r = invocation.getArgument(0);
            r.setModifiedAt(LocalDateTime.now()); // Simulate update of modification date
            if (r.getId() == null) { // Simulate ID generation for new entities if ever used
                r.setId((int) (System.currentTimeMillis() % 100000));
            }
            if (r.getCreatedAt() == null) {
                r.setCreatedAt(LocalDateTime.now());
            }
            return Mono.just(r);
        });
    }

    @Test
    void findAll_shouldReturnAllDocumentRates() {
        // Arrange
        when(documentRatesRepository.findAll()).thenReturn(Flux.just(documentRate1, documentRate2));

        // Act
        Flux<DocumentRates> result = rateService.findAll();

        // Assert
        StepVerifier.create(result)
                .expectNext(documentRate1)
                .expectNext(documentRate2)
                .verifyComplete();
    }

    // Tests for find(Integer legalType, Integer documentType, Integer localType)
    @Test
    void find_whenFound() {
        // Arrange
        when(documentRatesRepository.findByLegalTypeAndDocumentTypeAndLocalType(
                eq(documentRate1.getLegalType()),
                eq(documentRate1.getDocumentType()),
                eq(documentRate1.getLocalType())))
                .thenReturn(Mono.just(documentRate1));

        // Act
        Mono<DocumentRates> result = rateService.find(
                documentRate1.getLegalType(),
                documentRate1.getDocumentType(),
                documentRate1.getLocalType());

        // Assert
        StepVerifier.create(result)
                .expectNext(documentRate1)
                .verifyComplete();
    }

    @Test
    void find_whenNotFound() {
        // Arrange
        Integer nonExistentLegalType = 99;
        Integer nonExistentDocType = 999;
        Integer nonExistentLocalType = 9999;
        when(documentRatesRepository.findByLegalTypeAndDocumentTypeAndLocalType(
                eq(nonExistentLegalType), eq(nonExistentDocType), eq(nonExistentLocalType)))
                .thenReturn(Mono.empty());

        // Act
        Mono<DocumentRates> result = rateService.find(nonExistentLegalType, nonExistentDocType, nonExistentLocalType);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    // Tests for findRate(Integer legalType, Integer documentType, Integer localType)
    @Test
    void findRate_whenFound() {
        // Arrange
        when(ratesRepository.findByLegalizationTypeAndDocumentTypeIdAndLocalType(
                eq(rate1.getLegalizationType()),
                eq(rate1.getDocumentTypeId()),
                eq(rate1.getLocalType())))
                .thenReturn(Mono.just(rate1));

        // Act
        Mono<Rates> result = rateService.findRate(
                rate1.getLegalizationType(),
                rate1.getDocumentTypeId(),
                rate1.getLocalType());

        // Assert
        StepVerifier.create(result)
                .expectNext(rate1)
                .verifyComplete();
    }

    @Test
    void findRate_whenNotFound() {
        // Arrange
        Integer nonExistentLegalType = 88;
        Integer nonExistentDocType = 888;
        Integer nonExistentLocalType = 8888;
        when(ratesRepository.findByLegalizationTypeAndDocumentTypeIdAndLocalType(
                eq(nonExistentLegalType), eq(nonExistentDocType), eq(nonExistentLocalType)))
                .thenReturn(Mono.empty());

        // Act
        Mono<Rates> result = rateService.findRate(nonExistentLegalType, nonExistentDocType, nonExistentLocalType);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    // Tests for update(RateRequest rateRequest)
    @Test
    void update_rate_success() {
        // Arrange
        // rateRequest1 and rate1 share the same type combination
        when(ratesRepository.findByLegalizationTypeAndDocumentTypeIdAndLocalType(
                eq(rateRequest1.getLegalType()),
                eq(rateRequest1.getDocumentType()),
                eq(rateRequest1.getLocalType())))
                .thenReturn(Mono.just(rate1)); // Return the existing rate1

        // The lenient save mock in setUp for ratesRepository will handle the save:
        // - It will use the passed rate object (which service should have updated with new price)
        // - It will update modificationDate

        // Act
        Mono<Rates> result = rateService.update(rateRequest1);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(updatedRate ->
                        updatedRate.getId().equals(rate1.getId()) &&
                        updatedRate.getPrice().equals(rateRequest1.getPrice()) && // Price should be updated
                        updatedRate.getLegalizationType().equals(rateRequest1.getLegalType()) &&
                        updatedRate.getDocumentTypeId().equals(rateRequest1.getDocumentType()) &&
                        updatedRate.getLocalType().equals(rateRequest1.getLocalType()) &&
                        updatedRate.getModifiedAt() != null &&
                        !updatedRate.getModifiedAt().isEqual(rate1.getModifiedAt())) // Check modificationDate changed
                .verifyComplete();
    }

    @Test
    void update_rate_error_notFound() {
        // Arrange
        RateRequest nonExistentRateRequest = new RateRequest();
        nonExistentRateRequest.setLegalType(77);
        nonExistentRateRequest.setDocumentType(777);
        nonExistentRateRequest.setLocalType(7777);
        nonExistentRateRequest.setPrice(200f);

        when(ratesRepository.findByLegalizationTypeAndDocumentTypeIdAndLocalType(
                eq(nonExistentRateRequest.getLegalType()),
                eq(nonExistentRateRequest.getDocumentType()),
                eq(nonExistentRateRequest.getLocalType())))
                .thenReturn(Mono.empty()); // Rate to update does not exist

        // Act
        Mono<Rates> result = rateService.update(nonExistentRateRequest);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class) // Assuming service throws RuntimeException
                .verify();
    }
}
