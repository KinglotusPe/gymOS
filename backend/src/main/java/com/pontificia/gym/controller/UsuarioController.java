package com.pontificia.gym.controller;

import com.pontificia.gym.entity.Rol;
import com.pontificia.gym.entity.Usuario;
import com.pontificia.gym.service.ClienteService;
import com.pontificia.gym.service.EntrenadorService;
import com.pontificia.gym.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final ClienteService clienteService;
    private final EntrenadorService entrenadorService;

    public UsuarioController(UsuarioService usuarioService,
                             ClienteService clienteService,
                             EntrenadorService entrenadorService) {
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
        this.entrenadorService = entrenadorService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/list";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        Usuario usuario = new Usuario();
        usuario.setActivo(true);
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", Rol.values());
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("entrenadores", entrenadorService.listarTodos());
        return "usuarios/form";
    }

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id).orElse(null);
        if (usuario == null) {
            return "redirect:/usuarios";
        }
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", Rol.values());
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("entrenadores", entrenadorService.listarTodos());
        return "usuarios/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("usuario") Usuario usuario,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Rol.values());
            model.addAttribute("clientes", clienteService.listarTodos());
            model.addAttribute("entrenadores", entrenadorService.listarTodos());
            return "usuarios/form";
        }
        usuarioService.guardar(usuario);
        redirectAttributes.addFlashAttribute("mensaje", "Usuario guardado correctamente");
        return "redirect:/usuarios";
    }

    @GetMapping("/toggle/{id}")
    public String toggleEstado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.buscarPorId(id).ifPresent(u -> {
            u.setActivo(!u.isActivo());
            usuarioService.guardar(u);
        });
        redirectAttributes.addFlashAttribute("mensaje", "Estado del usuario actualizado");
        return "redirect:/usuarios";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado");
        return "redirect:/usuarios";
    }

    @PostMapping("/perfil/cambiar-password")
    public String cambiarPasswordPerfil(@RequestParam("actual") String actual,
                                        @RequestParam("nueva") String nueva,
                                        @RequestParam("confirmacion") String confirmacion,
                                        org.springframework.security.core.Authentication auth,
                                        jakarta.servlet.http.HttpServletRequest request,
                                        RedirectAttributes redirectAttributes) {
        String referer = request.getHeader("Referer");
        String redirectUrl = (referer != null && !referer.isBlank()) ? referer : "/";

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        if (!nueva.equals(confirmacion)) {
            redirectAttributes.addFlashAttribute("errorPassword", "La nueva contraseña y su confirmación no coinciden.");
            return "redirect:" + redirectUrl;
        }

        if (nueva.length() < 4) {
            redirectAttributes.addFlashAttribute("errorPassword", "La nueva contraseña debe tener al menos 4 caracteres.");
            return "redirect:" + redirectUrl;
        }

        boolean exito = usuarioService.cambiarPassword(auth.getName(), actual, nueva);
        if (exito) {
            redirectAttributes.addFlashAttribute("mensajePassword", "¡Contraseña actualizada con éxito!");
        } else {
            redirectAttributes.addFlashAttribute("errorPassword", "La contraseña actual ingresada es incorrecta.");
        }

        return "redirect:" + redirectUrl;
    }
}
