package com.pontificia.gym.controller;

import com.pontificia.gym.entity.Membresia;
import com.pontificia.gym.entity.TipoMembresia;
import com.pontificia.gym.service.ClienteService;
import com.pontificia.gym.service.MembresiaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/membresias")
public class MembresiaController {

    private final MembresiaService membresiaService;
    private final ClienteService clienteService;

    public MembresiaController(MembresiaService membresiaService, ClienteService clienteService) {
        this.membresiaService = membresiaService;
        this.clienteService = clienteService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("membresias", membresiaService.listarTodos());
        return "membresias/list";
    }

    @GetMapping({"/nuevo", "/nueva"})
    public String nuevoForm(Model model) {
        Membresia membresia = new Membresia();
        membresia.setFechaInicio(LocalDate.now());
        membresia.setTipo(TipoMembresia.MENSUAL);
        membresia.setFechaVencimiento(LocalDate.now().plusMonths(1));

        model.addAttribute("membresia", membresia);
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("tipos", TipoMembresia.values());
        return "membresias/form";
    }

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("membresia", membresiaService.buscarPorId(id));
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("tipos", TipoMembresia.values());
        return "membresias/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("membresia") Membresia membresia,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        // Auto-calcular vencimiento si no viene definido
        if (membresia.getFechaInicio() == null) {
            membresia.setFechaInicio(LocalDate.now());
        }
        if (membresia.getFechaVencimiento() == null || membresia.getTipo() != TipoMembresia.PERSONALIZADA) {
            membresia.setFechaVencimiento(membresia.calcularVencimientoSegunTipo());
        }

        if (result.hasErrors() && (result.getErrorCount() > 1 || !result.hasFieldErrors("fechaVencimiento"))) {
            model.addAttribute("clientes", clienteService.listarTodos());
            model.addAttribute("tipos", TipoMembresia.values());
            return "membresias/form";
        }

        membresiaService.guardar(membresia);
        redirectAttributes.addFlashAttribute("mensaje", "Membresía guardada correctamente");
        return "redirect:/membresias";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        membresiaService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Membresía eliminada");
        return "redirect:/membresias";
    }
}
