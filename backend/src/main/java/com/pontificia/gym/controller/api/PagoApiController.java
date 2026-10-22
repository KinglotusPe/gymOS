package com.pontificia.gym.controller.api;

import com.pontificia.gym.entity.Pago;
import com.pontificia.gym.service.PagoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
public class PagoApiController {

    private final PagoService pagoService;

    public PagoApiController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    public ResponseEntity<List<Pago>> listarTodos() {
        return ResponseEntity.ok(pagoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Pago pago = pagoService.buscarPorId(id);
        if (pago != null) {
            return ResponseEntity.ok(pago);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Pago>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(pagoService.listarPorCliente(clienteId));
    }

    @GetMapping("/total-mes")
    public ResponseEntity<Map<String, BigDecimal>> totalMes() {
        return ResponseEntity.ok(Map.of("totalMes", pagoService.totalRecaudadoDelMes()));
    }

    @PostMapping
    public ResponseEntity<?> registrarPago(@RequestBody Pago pago) {
        try {
            Pago guardado = pagoService.guardar(pago);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            pagoService.eliminar(id);
            return ResponseEntity.ok(Map.of("mensaje", "Pago eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
