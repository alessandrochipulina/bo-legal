package world.inclub.bo_legal_microservice.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.domain.models.DocumentRates;
import world.inclub.bo_legal_microservice.domain.models.Rates;
import world.inclub.bo_legal_microservice.domain.request.RateRequest;
import world.inclub.bo_legal_microservice.infraestructure.repositories.DocumentRatesRepository;
import world.inclub.bo_legal_microservice.infraestructure.repositories.RatesRepository;

@Component
public class RateService {

    @Autowired
    private DocumentRatesRepository drr;
    @Autowired
    private RatesRepository rr;

    public Mono<Rates> 
    update(
        RateRequest rateRequest)    
    {
        return this.findRate(rateRequest.getLegalType(), rateRequest.getDocumentType(), rateRequest.getLocalType())
            .flatMap(rate -> {
                rate.setPrice(rateRequest.getPrice());
                return rr.save(rate);
            })
            .switchIfEmpty(Mono.error(new IllegalArgumentException("No se encuentra la tarifa de documento")))
            .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())));
    }

    public Mono<DocumentRates>
    find(Integer legalType, Integer documentType, Integer localType) 
    {
        return drr.findByLegalTypeAndDocumentTypeAndLocalType(legalType, documentType, localType);
    }

    public Mono<Rates>
    findRate(Integer legalType, Integer documentType, Integer localType) 
    {
        return rr.findByLegalTypeAndDocumentTypeAndLocalType(legalType, documentType, localType);
    }

    public Flux<DocumentRates>
    findAll() 
    {        
        return drr.findAll();
    }

}