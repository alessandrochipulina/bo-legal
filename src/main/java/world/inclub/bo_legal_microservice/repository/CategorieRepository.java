package world.inclub.bo_legal_microservice.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import reactor.core.publisher.Flux;
import world.inclub.bo_legal_microservice.model.*;


public interface CategorieRepository extends R2dbcRepository<Categorie, Integer> {

    @Query("SELECT * FROM core.categorie WHERE categorie_id = :categorieId AND status > 0")
    Flux<Categorie> findAllByCategorieId(Integer categorieId);    
}
