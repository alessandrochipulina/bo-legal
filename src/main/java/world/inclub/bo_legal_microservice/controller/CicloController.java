package world.inclub.bo_legal_microservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.application.services.CicloService;
import world.inclub.bo_legal_microservice.domain.models.*;
import world.inclub.bo_legal_microservice.domain.request.ApiResponse;

@RestController
@RequestMapping("/api/v1/ciclo")
@Slf4j
public class CicloController {

    @Autowired
    private CicloService cr;

    @GetMapping("/all")
    @Validated
    Mono<ResponseEntity<ApiResponse<List<Ciclo>>>> 
    getAllCiclo() 
    {        
        log.info("getAllCiclo: Recuperando todas las ciclos");
        
        return cr.findAll()
        .collectList()
        .map(ciclo -> {
            ApiResponse<List<Ciclo>> response = new ApiResponse<>(ciclo,"Ciclo recuperadas correctamente");
            log.debug("getAllCiclo: respuesta: {}", response.toString());
            return ResponseEntity.ok(response);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("No existen ciclos registradas")))
        .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())))
        .doOnError(error -> { log.error("getAllCiclo: error: {}", error.getMessage(), error); });        
    }

    @GetMapping("/{Id}")
    @Validated
    Mono<ResponseEntity<ApiResponse<Ciclo>>> 
    getCicloId(@PathVariable @NotNull Integer Id ) 
    {
        log.info("getCicloId: Id: {}", Id);

        return cr.findById( Id )
        .map(ciclo -> {
            ApiResponse<Ciclo> response = new ApiResponse<>(ciclo, "Ciclo recuperado correctamente" );
            log.debug("getCicloId: respuesta: {}", response.toString());
            return ResponseEntity.ok(response);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El ID del ciclo no existe")))
        .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())))
        .doOnError(error -> { log.error("getCicloId: error: {}", error.getMessage(), error); });
    }    

    @PostMapping("/add")
    @Validated
    Mono<ResponseEntity<ApiResponse<Ciclo>>> addCiclo(
        @RequestBody @Valid @NotNull Ciclo doc)
    {
        return 
        cr.save(doc)
        .map(response -> {
            ApiResponse<Ciclo> apiResponse = new ApiResponse<>( response, "Ciclo registrado correctamente");
            return ResponseEntity.ok(apiResponse);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El Ciclo no se puede registrar")))
        .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())))
        .doOnError(error -> { log.error("getCicloId: error: {}", error.getMessage(), error); });
    }

    @PostMapping("/edit/{Id}")
    @Validated
    Mono<ResponseEntity<ApiResponse<Ciclo>>> updateCiclo(
        @PathVariable @NotNull Integer Id,
        @RequestBody @NotNull @Valid Ciclo doc)
    {
        return 
        cr.update(Id, doc)        
        .map(response -> {
            ApiResponse<Ciclo> apiResponse = new ApiResponse<>( response, "Ciclo actualizado correctamente");
            return ResponseEntity.ok(apiResponse);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El Ciclo no se puede registrar")))
        .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())))
        .doOnError(error -> { log.error("getCicloId: error: {}", error.getMessage(), error); });
    }

    @PostMapping("/delete/{Id}")
    @Validated
    Mono<ResponseEntity<ApiResponse<Ciclo>>> deleteCiclo(
        @PathVariable @NotNull Integer Id)
    {
        return 
        cr.deleteById(Id)
        .then(cr.findById(Id))        
        .map(response -> {
            ApiResponse<Ciclo> apiResponse = new ApiResponse<>( response, "El Ciclo no se pudo eliminar");
            return ResponseEntity.ok(apiResponse);
        })
        .switchIfEmpty(Mono.just(ResponseEntity.ok(new ApiResponse<Ciclo>(null, "El Ciclo fue eliminado correctamente"))))
        .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())))
        .doOnError(error -> { log.error("getCicloId: error: {}", error.getMessage(), error); });
    }
    
}