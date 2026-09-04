package com.pontificia.gym.controller.api;

import com.pontificia.gym.entity.Producto;
import com.pontificia.gym.service.TiendaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tienda")
@CrossOrigin(origins = "*")
public class TiendaApiController {

    private final TiendaService tiendaService;

    public TiendaApiController(TiendaService tiendaService) {
        this.tiendaService = tiendaService;
    }

    @GetMapping("/productos")
    public ResponseEntity<List<Producto>> listarProductos() {
        return ResponseEntity.ok(tiendaService.listarProductos());
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<?> buscarProducto(@PathVariable Long id) {
        return tiendaService.buscarProductoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/venta")
    public ResponseEntity<?> registrarVenta(@RequestBody Map<String, Object> ventaDto) {
        try {
            Long clienteId = ventaDto.get("clienteId") != null ? Long.valueOf(ventaDto.get("clienteId").toString()) : null;
            String metodoPago = (String) ventaDto.getOrDefault("metodoPago", "EFECTIVO");
            String username = (String) ventaDto.getOrDefault("username", "admin");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsRaw = (List<Map<String, Object>>) ventaDto.get("items");
            if (itemsRaw == null || itemsRaw.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Debe incluir al menos un producto"));
            }

            List<TiendaService.ItemVentaDto> items = itemsRaw.stream().map(i -> {
                Long pid = Long.valueOf(i.get("productoId").toString());
                Integer cant = Integer.valueOf(i.get("cantidad").toString());
                return new TiendaService.ItemVentaDto(pid, cant);
            }).toList();

            var venta = tiendaService.registrarVenta(clienteId, username, metodoPago, items);
            return ResponseEntity.ok(venta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
