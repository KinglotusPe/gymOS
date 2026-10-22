package com.pontificia.gym.controller.api;

import com.pontificia.gym.entity.Membresia;
import com.pontificia.gym.service.MembresiaService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/membresias", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class MembresiaApiController {

    private final MembresiaService membresiaService;

    public MembresiaApiController(MembresiaService membresiaService) {
        this.membresiaService = membresiaService;
    }

    @GetMapping
    public ResponseEntity<List<Membresia>> listarTodos() {
        return ResponseEntity.ok(membresiaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Membresia m = membresiaService.buscarPorId(id);
        if (m != null) {
            return ResponseEntity.ok(m);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Membresia>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(membresiaService.listarPorCliente(clienteId));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Membresia membresia) {
        try {
            if (membresia.getFechaVencimiento() == null) {
                membresia.setFechaVencimiento(membresia.calcularVencimientoSegunTipo());
            }
            Membresia guardada = membresiaService.guardar(membresia);
            return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(guardada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Membresia datos) {
        Membresia existente = membresiaService.buscarPorId(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        if (datos.getTipo() != null) existente.setTipo(datos.getTipo());
        if (datos.getFechaInicio() != null) existente.setFechaInicio(datos.getFechaInicio());
        if (datos.getFechaVencimiento() != null) existente.setFechaVencimiento(datos.getFechaVencimiento());
        if (datos.getEstado() != null) existente.setEstado(datos.getEstado());
        Membresia actualizada = membresiaService.guardar(existente);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            membresiaService.eliminar(id);
            return ResponseEntity.ok(java.util.Map.of("mensaje", "Membresía eliminada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
