package com.pontificia.gym.controller;

import com.pontificia.gym.entity.Casillero;
import com.pontificia.gym.service.CasilleroService;
import com.pontificia.gym.service.ClienteService;
import com.pontificia.gym.service.EntrenadorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/casilleros")
public class CasilleroController {

    private final CasilleroService casilleroService;
    private final ClienteService clienteService;
    private final EntrenadorService entrenadorService;

    public CasilleroController(CasilleroService casilleroService,
                               ClienteService clienteService,
                               EntrenadorService entrenadorService) {
        this.casilleroService = casilleroService;
        this.clienteService = clienteService;
        this.entrenadorService = entrenadorService;
    }

    @GetMapping
    public String vistaMapaCasilleros(@RequestParam(value = "piso", required = false) Integer piso, Model model) {
        casilleroService.inicializarCasillerosPorDefecto();

        List<Casillero> casilleros = (piso != null && piso > 0) 
                ? casilleroService.listarPorPiso(piso) 
                : casilleroService.listarTodos();

        model.addAttribute("casilleros", casilleros);
        model.addAttribute("pisoSeleccionado", piso);
        model.addAttribute("pisos", casilleroService.listarPisos());
        model.addAttribute("totalLibres", casilleroService.contarDisponibles());
        model.addAttribute("totalOcupados", casilleroService.contarOcupados());
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("entrenadores", entrenadorService.listarTodos());

        return "casilleros/mapa";
    }

    @PostMapping("/guardar")
    public String guardarCasillero(@RequestParam(value = "id", required = false) Long id,
                                   @RequestParam("numero") String numero,
                                   @RequestParam(value = "piso", defaultValue = "1") Integer piso,
                                   @RequestParam("ubicacion") String ubicacion,
                                   @RequestParam(value = "estado", defaultValue = "DISPONIBLE") String estado,
                                   RedirectAttributes flash) {
        try {
            if (numero == null || numero.trim().isEmpty()) {
                flash.addFlashAttribute("error", "El número o código del casillero es obligatorio (ej. L-06).");
                return "redirect:/casilleros";
            }

            numero = numero.trim().toUpperCase();

            if (id != null) {
                Casillero c = casilleroService.buscarPorId(id)
                        .orElseThrow(() -> new RuntimeException("Casillero no encontrado"));
                c.setNumero(numero);
                c.setPiso(piso);
                c.setUbicacion(ubicacion);
                if (!"OCUPADO".equalsIgnoreCase(c.getEstado())) {
                    c.setEstado(estado);
                }
                casilleroService.guardar(c);
                flash.addFlashAttribute("success", "Casillero " + numero + " actualizado con éxito en Piso " + piso + ".");
            } else {
                if (casilleroService.existePorNumero(numero)) {
                    flash.addFlashAttribute("error", "Ya existe un casillero registrado con el número " + numero);
                    return "redirect:/casilleros";
                }
                Casillero nuevo = new Casillero(null, numero, ubicacion, piso, estado);
                casilleroService.guardar(nuevo);
                flash.addFlashAttribute("success", "Nuevo casillero " + numero + " creado exitosamente en Piso " + piso + ".");
            }
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al guardar casillero: " + e.getMessage());
        }
        return "redirect:/casilleros" + (piso != null && piso > 0 ? "?piso=" + piso : "");
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarCasillero(@PathVariable("id") Long id, RedirectAttributes flash) {
        try {
            Casillero c = casilleroService.buscarPorId(id).orElse(null);
            if (c != null) {
                if ("OCUPADO".equalsIgnoreCase(c.getEstado())) {
                    flash.addFlashAttribute("error", "No se puede eliminar el casillero " + c.getNumero() + " porque está en uso.");
                    return "redirect:/casilleros";
                }
                casilleroService.eliminar(id);
                flash.addFlashAttribute("success", "Casillero " + c.getNumero() + " eliminado del sistema.");
            }
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al eliminar casillero: " + e.getMessage());
        }
        return "redirect:/casilleros";
    }

    @PostMapping("/liberar/{id}")
    public String liberarCasillero(@PathVariable("id") Long id, RedirectAttributes flash) {
        casilleroService.liberarCasilleroPorId(id);
        flash.addFlashAttribute("success", "Casillero liberado correctamente.");
        return "redirect:/casilleros";
    }

    @PostMapping("/liberar-todos")
    public String liberarTodos(RedirectAttributes flash) {
        casilleroService.liberarTodos();
        flash.addFlashAttribute("success", "🧹 Todos los casilleros han sido liberados para el nuevo turno.");
        return "redirect:/casilleros";
    }

    @PostMapping("/mantenimiento/{id}")
    public String toggleMantenimiento(@PathVariable("id") Long id, RedirectAttributes flash) {
        casilleroService.buscarPorId(id).ifPresent(c -> {
            if ("MANTENIMIENTO".equals(c.getEstado())) {
                c.setEstado("DISPONIBLE");
                flash.addFlashAttribute("success", "Casillero habilitado nuevamente.");
            } else {
                c.setEstado("MANTENIMIENTO");
                flash.addFlashAttribute("warning", "Casillero puesto en mantenimiento.");
            }
            casilleroService.guardar(c);
        });
        return "redirect:/casilleros";
    }

    @PostMapping("/asignar-manual")
    public String asignarManual(@RequestParam("casilleroId") Long casilleroId,
                                @RequestParam("nombre") String nombre,
                                @RequestParam("dni") String dni,
                                @RequestParam(value = "tipo", defaultValue = "SOCIO") String tipo,
                                RedirectAttributes flash) {
        try {
            casilleroService.asignarCasilleroEspecifico(casilleroId, nombre, dni, tipo);
            flash.addFlashAttribute("success", "Casillero asignado manualmente a: " + nombre);
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al asignar casillero: " + e.getMessage());
        }
        return "redirect:/casilleros";
    }
}
