package world.inclub.bo_legal_microservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.domain.models.*;
import world.inclub.bo_legal_microservice.domain.request.ApiResponse;
import world.inclub.bo_legal_microservice.infraestructure.repositories.CategorieRepository;

@RestController
@RequestMapping("/api/v1/document/categories")
@Slf4j
public class DocumentCategorieController {

    @Autowired
    private CategorieRepository cr;

    @GetMapping("/all")
    @Validated
    Mono<ResponseEntity<ApiResponse<List<Categorie>>>> 
    getAllCategorie() 
    {        
        log.info("getAllCategorie: Recuperando todas las categorias");
        
        return cr.findAll()
        .collectList()
        .map(categorie -> {
            ApiResponse<List<Categorie>> response = new ApiResponse<>(categorie,"Categorias recuperadas correctamente");
            log.debug("getAllCategorie: respuesta: {}", response.toString());
            return ResponseEntity.ok(response);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("No existen categorias registradas")))
        .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())))
        .doOnError(error -> { log.error("getAllCategorie: error: {}", error.getMessage(), error); });        
    }

    @GetMapping("/{categorieId}")
    @Validated
    Mono<ResponseEntity<ApiResponse<List<Categorie>>>> 
    getCategorieById(@PathVariable @NotNull Integer categorieId ) 
    {
        log.info("getCategorieById: categorieId: {}", categorieId);

        return cr.findAllByCategorieId( categorieId )
        .collectList()
        .map(categorie -> {
            ApiResponse<List<Categorie>> response = new ApiResponse<>(categorie, "Categorias recuperadas correctamente" );
            log.debug("getCategorieById: respuesta: {}", response.toString());
            return ResponseEntity.ok(response);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El ID de la categoria no existe")))
        .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())))
        .doOnError(error -> { log.error("getCategorieById: error: {}", error.getMessage(), error); });
    }    
}
