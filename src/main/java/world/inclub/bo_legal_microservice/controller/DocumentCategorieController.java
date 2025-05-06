package world.inclub.bo_legal_microservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import world.inclub.bo_legal_microservice.domain.models.*;
import world.inclub.bo_legal_microservice.infraestructure.repositories.CategorieRepository;

@RestController
@RequestMapping("/api/v1/document/categories")
public class DocumentCategorieController {

    @Autowired
    private CategorieRepository cr;

    @GetMapping("/all")
    Flux<Categorie> getAllCategorie() 
    {
        return cr.findAll()
        .switchIfEmpty(Flux.error(new IllegalArgumentException("No existen categorias registradas")))
        .onErrorResume(e -> Flux.error(new IllegalArgumentException(e.getMessage())));                
    }

    @GetMapping("/{categorieId}")
    Flux<Categorie> getCategorieById(
        @PathVariable Integer categorieId
    ) 
    {
        return cr.findAllByCategorieId( categorieId )
        .switchIfEmpty(Flux.error(new IllegalArgumentException("El ID de la categoria no existe")))
        .onErrorResume(e -> Flux.error(new IllegalArgumentException(e.getMessage())));  
    }    
}
