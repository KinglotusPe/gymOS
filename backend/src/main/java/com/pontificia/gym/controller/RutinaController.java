package com.pontificia.gym.controller;

import com.pontificia.gym.entity.Rutina;
import com.pontificia.gym.service.ClienteService;
import com.pontificia.gym.service.EjercicioService;
import com.pontificia.gym.service.EntrenadorService;
import com.pontificia.gym.service.RutinaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/rutinas")
public class RutinaController {

    private final RutinaService rutinaService;
    private final ClienteService clienteService;
    private final EntrenadorService entrenadorService;
    private final EjercicioService ejercicioService;

    public RutinaController(RutinaService rutinaService,
                            ClienteService clienteService,
                            EntrenadorService entrenadorService,
                            EjercicioService ejercicioService) {
        this.rutinaService = rutinaService;
        this.clienteService = clienteService;
        this.entrenadorService = entrenadorService;
        this.ejercicioService = ejercicioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("rutinas", rutinaService.listarTodas());
        return "rutinas/list";
    }

    @GetMapping({"/nuevo", "/nueva"})
    public String nuevoForm(Model model) {
        model.addAttribute("rutina", new Rutina());
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("entrenadores", entrenadorService.listarTodos());
        model.addAttribute("ejerciciosDisponibles", ejercicioService.listarTodos());
        return "rutinas/form";
    }

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model) {
        Rutina rutina = rutinaService.buscarPorId(id);
        if (rutina == null) {
            return "redirect:/rutinas";
        }
        model.addAttribute("rutina", rutina);
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("entrenadores", entrenadorService.listarTodos());
        model.addAttribute("ejerciciosDisponibles", ejercicioService.listarTodos());
        return "rutinas/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("rutina") Rutina rutina,
                          BindingResult result,
                          @RequestParam(value = "ejerciciosSeleccionados", required = false) List<Long> ejerciciosSeleccionados,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("clientes", clienteService.listarTodos());
            model.addAttribute("entrenadores", entrenadorService.listarTodos());
            model.addAttribute("ejerciciosDisponibles", ejercicioService.listarTodos());
            return "rutinas/form";
        }
        rutinaService.guardarConEjercicios(rutina, ejerciciosSeleccionados);
        redirectAttributes.addFlashAttribute("mensaje", "Rutina guardada exitosamente con ejercicios visuales");
        return "redirect:/rutinas";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        rutinaService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Rutina eliminada");
        return "redirect:/rutinas";
    }
}
