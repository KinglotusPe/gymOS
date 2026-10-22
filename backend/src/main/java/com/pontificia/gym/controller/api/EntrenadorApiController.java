package com.pontificia.gym.controller.api;

import com.pontificia.gym.entity.Entrenador;
import com.pontificia.gym.service.EntrenadorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entrenadores")
@CrossOrigin(origins = "*")
public class EntrenadorApiController {

    private final EntrenadorService entrenadorService;

    public EntrenadorApiController(EntrenadorService entrenadorService) {
        this.entrenadorService = entrenadorService;
    }

    @GetMapping
    public ResponseEntity<List<Entrenador>> listarTodos() {
        return ResponseEntity.ok(entrenadorService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return entrenadorService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Entrenador>> listarActivos() {
        return ResponseEntity.ok(entrenadorService.listarActivos());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Entrenador entrenador) {
        try {
            if (entrenador.getDni() != null && entrenadorService.buscarPorDni(entrenador.getDni()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ya existe un entrenador con ese DNI"));
            }
            Entrenador guardado = entrenadorService.guardar(entrenador);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Entrenador datos) {
        return entrenadorService.buscarPorId(id).map(existente -> {
            existente.setNombres(datos.getNombres());
            existente.setApellidos(datos.getApellidos());
            existente.setDni(datos.getDni());
            existente.setTelefono(datos.getTelefono());
            existente.setEspecialidad(datos.getEspecialidad());
            existente.setEmail(datos.getEmail());
            existente.setFotoUrl(datos.getFotoUrl());
            if (datos.getActivo() != null) existente.setActivo(datos.getActivo());
            Entrenador actualizado = entrenadorService.guardar(existente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            entrenadorService.eliminar(id);
            return ResponseEntity.ok(Map.of("mensaje", "Entrenador eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
