package com.pontificia.gym.controller.api;

import com.pontificia.gym.entity.Membresia;
import com.pontificia.gym.service.MembresiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membresias")
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
}
