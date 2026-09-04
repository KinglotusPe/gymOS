package com.pontificia.gym.controller;

import com.pontificia.gym.entity.Cliente;
import com.pontificia.gym.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public String listar(@RequestParam(value = "buscar", required = false) String buscar,
                         @RequestParam(value = "pagina", defaultValue = "0") int pagina,
                         @RequestParam(value = "tamanio", defaultValue = "8") int tamanio,
                         @RequestParam(value = "ordenarPor", defaultValue = "id") String ordenarPor,
                         @RequestParam(value = "direccion", defaultValue = "desc") String direccion,
                         Model model) {
        org.springframework.data.domain.Page<Cliente> clientesPage = clienteService.listarPaginado(pagina, tamanio, ordenarPor, direccion, buscar);
        model.addAttribute("clientes", clientesPage.getContent());
        model.addAttribute("clientesPage", clientesPage);
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", clientesPage.getTotalPages());
        model.addAttribute("totalElementos", clientesPage.getTotalElements());
        model.addAttribute("ordenarPor", ordenarPor);
        model.addAttribute("direccion", direccion);
        model.addAttribute("buscar", buscar != null ? buscar : "");
        return "clientes/list";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        Cliente cliente = new Cliente();
        cliente.setFechaInscripcion(java.time.LocalDate.now());
        model.addAttribute("cliente", cliente);
        return "clientes/form";
    }

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.buscarPorId(id));
        return "clientes/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cliente") Cliente cliente,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "clientes/form";
        }
        clienteService.guardar(cliente);
        redirectAttributes.addFlashAttribute("mensaje", "Cliente guardado correctamente");
        return "redirect:/clientes";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        clienteService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Cliente eliminado");
        return "redirect:/clientes";
    }
}
