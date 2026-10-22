package com.pontificia.gym.controller.api;

import com.pontificia.gym.entity.Casillero;
import com.pontificia.gym.service.CasilleroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/casilleros")
@CrossOrigin(origins = "*")
public class CasilleroApiController {

    private final CasilleroService casilleroService;

    public CasilleroApiController(CasilleroService casilleroService) {
        this.casilleroService = casilleroService;
    }

    @GetMapping
    public ResponseEntity<List<Casillero>> listarTodos() {
        return ResponseEntity.ok(casilleroService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return casilleroService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Casillero>> listarDisponibles() {
        return ResponseEntity.ok(casilleroService.listarDisponibles());
    }

    @GetMapping("/piso/{piso}")
    public ResponseEntity<List<Casillero>> listarPorPiso(@PathVariable Integer piso) {
        return ResponseEntity.ok(casilleroService.listarPorPiso(piso));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Casillero casillero) {
        try {
            if (casillero.getNumero() != null && casilleroService.existePorNumero(casillero.getNumero())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ya existe un casillero con ese número"));
            }
            Casillero guardado = casilleroService.guardar(casillero);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Casillero datos) {
        return casilleroService.buscarPorId(id).map(existente -> {
            existente.setNumero(datos.getNumero());
            existente.setPiso(datos.getPiso());
            existente.setEstado(datos.getEstado());
            existente.setUbicacion(datos.getUbicacion());
            existente.setOcupadoPorNombre(datos.getOcupadoPorNombre());
            existente.setOcupadoPorDni(datos.getOcupadoPorDni());
            existente.setOcupadoPorTipo(datos.getOcupadoPorTipo());
            Casillero actualizado = casilleroService.guardar(existente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            casilleroService.eliminar(id);
            return ResponseEntity.ok(Map.of("mensaje", "Casillero eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
