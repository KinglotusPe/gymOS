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

    @PostMapping("/productos")
    public ResponseEntity<?> crearProducto(@RequestBody Producto producto) {
        try {
            Producto guardado = tiendaService.guardarProducto(producto);
            return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(guardado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Long id, @RequestBody Producto datos) {
        return tiendaService.buscarProductoPorId(id).map(existente -> {
            existente.setNombre(datos.getNombre());
            existente.setDescripcion(datos.getDescripcion());
            if (datos.getPrecioUnitario() != null) existente.setPrecioUnitario(datos.getPrecioUnitario());
            if (datos.getStock() != null) existente.setStock(datos.getStock());
            if (datos.getImagenUrl() != null) existente.setImagenUrl(datos.getImagenUrl());
            if (datos.getCodigoBarra() != null) existente.setCodigoBarra(datos.getCodigoBarra());
            Producto actualizado = tiendaService.guardarProducto(existente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id) {
        try {
            tiendaService.eliminarProducto(id);
            return ResponseEntity.ok(Map.of("mensaje", "Producto eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
