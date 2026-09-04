package com.pontificia.gym.controller;

import com.pontificia.gym.entity.ClaseGrupal;
import com.pontificia.gym.entity.Cliente;
import com.pontificia.gym.entity.Usuario;
import com.pontificia.gym.repository.ClienteRepository;
import com.pontificia.gym.repository.EntrenadorRepository;
import com.pontificia.gym.service.ClaseGrupalService;
import com.pontificia.gym.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/clases")
public class ClaseGrupalController {

    private final ClaseGrupalService claseGrupalService;
    private final EntrenadorRepository entrenadorRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioService usuarioService;

    public ClaseGrupalController(ClaseGrupalService claseGrupalService,
                                 EntrenadorRepository entrenadorRepository,
                                 ClienteRepository clienteRepository,
                                 UsuarioService usuarioService) {
        this.claseGrupalService = claseGrupalService;
        this.entrenadorRepository = entrenadorRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model, Authentication authentication) {
        claseGrupalService.inicializarDatosPorDefecto();
        model.addAttribute("clases", claseGrupalService.listarProximas());
        model.addAttribute("disciplinas", claseGrupalService.listarDisciplinas());

        // Si es cliente, determinar su ID para saber sus reservas
        if (authentication != null) {
            String username = authentication.getName();
            Optional<Usuario> u = usuarioService.buscarPorUsername(username);
            u.ifPresent(usuario -> {
                if (usuario.getCliente() != null) {
                    model.addAttribute("miClienteId", usuario.getCliente().getId());
                    model.addAttribute("misReservas", claseGrupalService.listarReservasPorCliente(usuario.getCliente().getId()));
                }
            });
        }

        return "clases/list";
    }

    @GetMapping({"/nuevo", "/nueva"})
    public String nuevo(Model model) {
        model.addAttribute("clase", new ClaseGrupal());
        model.addAttribute("disciplinas", claseGrupalService.listarDisciplinas());
        model.addAttribute("entrenadores", entrenadorRepository.findAll());
        return "clases/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute ClaseGrupal clase, RedirectAttributes flash) {
        claseGrupalService.guardar(clase);
        flash.addFlashAttribute("success", "¡Clase grupal programada exitosamente!");
        return "redirect:/clases";
    }

    @PostMapping("/{id}/reservar")
    public String reservar(@PathVariable Long id, Authentication authentication, RedirectAttributes flash) {
        if (authentication == null) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Optional<Usuario> u = usuarioService.buscarPorUsername(username);

        if (u.isEmpty() || u.get().getCliente() == null) {
            flash.addFlashAttribute("error", "Solo los socios registrados pueden reservar cupos en clases.");
            return "redirect:/clases";
        }

        try {
            claseGrupalService.reservarCupo(id, u.get().getCliente().getId());
            flash.addFlashAttribute("success", "¡Tu cupo ha sido reservado con éxito! Te esperamos en la clase.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/clases";
    }

    @PostMapping("/reservas/{id}/cancelar")
    public String cancelarReserva(@PathVariable Long id, RedirectAttributes flash) {
        claseGrupalService.cancelarReserva(id);
        flash.addFlashAttribute("info", "Reserva cancelada.");
        return "redirect:/clases";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        claseGrupalService.eliminar(id);
        flash.addFlashAttribute("success", "Clase eliminada del cronograma.");
        return "redirect:/clases";
    }
}
