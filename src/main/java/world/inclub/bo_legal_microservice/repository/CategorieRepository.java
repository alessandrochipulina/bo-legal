package world.inclub.bo_legal_microservice.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import reactor.core.publisher.Flux;
import world.inclub.bo_legal_microservice.model.*;


public interface CategorieRepository extends R2dbcRepository<Categorie, Integer> {

    Flux<Categorie> findAllByCategorieId(Integer categorieId);    
}
