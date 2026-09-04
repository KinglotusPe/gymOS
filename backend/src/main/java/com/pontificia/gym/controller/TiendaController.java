package com.pontificia.gym.controller;

import com.pontificia.gym.entity.Producto;
import com.pontificia.gym.entity.Venta;
import com.pontificia.gym.service.ClienteService;
import com.pontificia.gym.service.TiendaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/tienda")
public class TiendaController {

    private final TiendaService tiendaService;
    private final ClienteService clienteService;

    public TiendaController(TiendaService tiendaService, ClienteService clienteService) {
        this.tiendaService = tiendaService;
        this.clienteService = clienteService;
    }

    @GetMapping
    public String vistaPos(Model model, @RequestParam(value = "categoria", required = false) Long categoriaId,
                           @RequestParam(value = "q", required = false) String query) {
        tiendaService.inicializarCatalogoPorDefecto();

        List<Producto> productos;
        if (query != null && !query.trim().isEmpty()) {
            productos = tiendaService.buscarProductos(query);
        } else if (categoriaId != null) {
            productos = tiendaService.listarPorCategoria(categoriaId);
        } else {
            productos = tiendaService.listarProductos();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", tiendaService.listarCategorias());
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("query", query);
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("totalVentasHoy", tiendaService.calcularTotalVentasHoy());

        if (model.containsAttribute("ultimaVentaId")) {
            Object idObj = model.getAttribute("ultimaVentaId");
            if (idObj instanceof Long vid) {
                tiendaService.buscarVentaPorId(vid).ifPresent(v -> model.addAttribute("ticketVenta", v));
            }
        }

        return "tienda/pos";
    }

    @PostMapping("/cobrar")
    public String procesarVenta(@RequestParam(value = "clienteId", required = false) Long clienteId,
                                @RequestParam(value = "metodoPago", defaultValue = "EFECTIVO") String metodoPago,
                                @RequestParam(value = "productoIds", required = false) List<Long> productoIds,
                                @RequestParam(value = "cantidades", required = false) List<Integer> cantidades,
                                Authentication authentication,
                                RedirectAttributes flash) {
        if (productoIds == null || productoIds.isEmpty() || cantidades == null || cantidades.isEmpty()) {
            flash.addFlashAttribute("error", "El carrito está vacío. Haz clic en al menos un producto del catálogo para agregarlo antes de cobrar.");
            return "redirect:/tienda";
        }

        try {
            List<TiendaService.ItemVentaDto> items = new ArrayList<>();
            for (int i = 0; i < productoIds.size(); i++) {
                if (cantidades.get(i) != null && cantidades.get(i) > 0) {
                    items.add(new TiendaService.ItemVentaDto(productoIds.get(i), cantidades.get(i)));
                }
            }

            if (items.isEmpty()) {
                flash.addFlashAttribute("error", "No se seleccionaron cantidades válidas de productos.");
                return "redirect:/tienda";
            }

            String cajero = authentication != null ? authentication.getName() : "recepcion";
            Venta venta = tiendaService.registrarVenta(clienteId, cajero, metodoPago, items);

            flash.addFlashAttribute("success", "¡Venta registrada con éxito! Comprobante: " + venta.getCodigoComprobante() + " por S/ " + venta.getTotal());
            flash.addFlashAttribute("ultimaVentaId", venta.getId());
            flash.addFlashAttribute("ticketVenta", venta);
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al procesar venta: " + e.getMessage());
        }

        return "redirect:/tienda";
    }

    @GetMapping("/ventas")
    public String historialVentas(Model model) {
        model.addAttribute("ventas", tiendaService.listarVentas());
        model.addAttribute("totalVentasHoy", tiendaService.calcularTotalVentasHoy());
        return "tienda/ventas_list";
    }

    // --- GESTIÓN DE INVENTARIO Y PRODUCTOS ---

    @GetMapping("/productos")
    public String listarInventario(Model model) {
        model.addAttribute("productos", tiendaService.listarProductos());
        model.addAttribute("categorias", tiendaService.listarCategorias());
        return "tienda/productos_list";
    }

    @GetMapping("/productos/nuevo")
    public String nuevoProductoForm(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", tiendaService.listarCategorias());
        return "tienda/producto_form";
    }

    @GetMapping("/productos/editar/{id}")
    public String editarProductoForm(@PathVariable("id") Long id, Model model, RedirectAttributes flash) {
        return tiendaService.buscarProductoPorId(id)
                .map(p -> {
                    model.addAttribute("producto", p);
                    model.addAttribute("categorias", tiendaService.listarCategorias());
                    return "tienda/producto_form";
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "El producto no existe.");
                    return "redirect:/tienda/productos";
                });
    }

    @PostMapping("/productos/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, RedirectAttributes flash) {
        try {
            tiendaService.guardarProducto(producto);
            flash.addFlashAttribute("success", "Producto guardado correctamente en el inventario.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al guardar el producto: " + e.getMessage());
        }
        return "redirect:/tienda/productos";
    }

    @PostMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Long id, RedirectAttributes flash) {
        try {
            tiendaService.eliminarProducto(id);
            flash.addFlashAttribute("success", "Producto eliminado del catálogo.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se puede eliminar el producto porque tiene ventas asociadas.");
        }
        return "redirect:/tienda/productos";
    }
}
