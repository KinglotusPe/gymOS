package com.pontificia.gym.controller.api;

import com.pontificia.gym.entity.Ejercicio;
import com.pontificia.gym.service.EjercicioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/ejercicios", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class EjercicioApiController {

    private final EjercicioService ejercicioService;

    public EjercicioApiController(EjercicioService ejercicioService) {
        this.ejercicioService = ejercicioService;
    }

    @GetMapping
    public ResponseEntity<List<Ejercicio>> listarTodos() {
        return ResponseEntity.ok(ejercicioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return ejercicioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/grupo/{grupo}")
    public ResponseEntity<List<Ejercicio>> buscarPorGrupo(@PathVariable String grupo) {
        return ResponseEntity.ok(ejercicioService.buscarPorGrupoMuscular(grupo));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Ejercicio ejercicio) {
        try {
            Ejercicio guardado = ejercicioService.guardar(ejercicio);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Ejercicio datos) {
        return ejercicioService.buscarPorId(id).map(existente -> {
            existente.setNombre(datos.getNombre());
            existente.setGrupoMuscular(datos.getGrupoMuscular());
            existente.setEquipamiento(datos.getEquipamiento());
            existente.setNivel(datos.getNivel());
            existente.setInstrucciones(datos.getInstrucciones());
            existente.setImagenUrl(datos.getImagenUrl());
            existente.setGifUrl(datos.getGifUrl());
            existente.setCategoria(datos.getCategoria());
            Ejercicio actualizado = ejercicioService.guardar(existente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            ejercicioService.eliminar(id);
            return ResponseEntity.ok(Map.of("mensaje", "Ejercicio eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
