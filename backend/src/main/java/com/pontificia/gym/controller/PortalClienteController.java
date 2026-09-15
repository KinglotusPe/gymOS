package com.pontificia.gym.controller;

import com.pontificia.gym.entity.Cliente;
import com.pontificia.gym.entity.Membresia;
import com.pontificia.gym.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/portal")
public class PortalClienteController {

    private final UsuarioService usuarioService;
    private final ClienteService clienteService;
    private final MembresiaService membresiaService;
    private final PagoService pagoService;
    private final AsistenciaService asistenciaService;
    private final SeguimientoFisicoService seguimientoFisicoService;
    private final RutinaService rutinaService;
    private final ClaseGrupalService claseGrupalService;

    public PortalClienteController(UsuarioService usuarioService,
                                   ClienteService clienteService,
                                   MembresiaService membresiaService,
                                   PagoService pagoService,
                                   AsistenciaService asistenciaService,
                                   SeguimientoFisicoService seguimientoFisicoService,
                                   RutinaService rutinaService,
                                   ClaseGrupalService claseGrupalService) {
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
        this.membresiaService = membresiaService;
        this.pagoService = pagoService;
        this.asistenciaService = asistenciaService;
        this.seguimientoFisicoService = seguimientoFisicoService;
        this.rutinaService = rutinaService;
        this.claseGrupalService = claseGrupalService;
    }

    @GetMapping("/mi-cuenta")
    public String miCuenta(@org.springframework.web.bind.annotation.RequestParam(name = "clienteId", required = false) Long clienteId,
                           Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_RECEPCIONISTA"));

        // Buscar el cliente por su usuario vinculado o por su DNI
        Cliente cliente = usuarioService.buscarPorUsername(username)
                .map(u -> u.getCliente() != null ? u.getCliente() : clienteService.buscarPorDni(username).orElse(null))
                .orElseGet(() -> clienteService.buscarPorDni(username).orElse(null));

        // Si es administrador o recepcionista y no es un cliente, permitir previsualizar el portal con clienteId o el primer cliente
        if (cliente == null && esAdmin) {
            if (clienteId != null) {
                cliente = clienteService.buscarPorId(clienteId);
            }
            if (cliente == null) {
                cliente = clienteService.listarTodos().stream().findFirst().orElse(null);
            }
        }

        if (cliente == null) {
            return "redirect:/login";
        }

        if (cliente != null) {
            model.addAttribute("cliente", cliente);

            // Membresías
            List<Membresia> membresias = membresiaService.listarPorCliente(cliente.getId());
            model.addAttribute("membresias", membresias);

            Membresia activa = membresias.stream()
                    .filter(m -> "ACTIVA".equals(m.getEstado().name()))
                    .findFirst()
                    .orElse(null);
            model.addAttribute("membresiaActiva", activa);

            if (activa != null && activa.getFechaVencimiento() != null) {
                long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), activa.getFechaVencimiento());
                model.addAttribute("diasRestantes", Math.max(0, diasRestantes));
            } else {
                model.addAttribute("diasRestantes", 0);
            }

            // Pagos
            model.addAttribute("pagos", pagoService.listarPorCliente(cliente.getId()));

            // Asistencias
            List<com.pontificia.gym.entity.Asistencia> asistencias = asistenciaService.listarPorCliente(cliente.getId());
            model.addAttribute("asistencias", asistencias);

            // Seguimientos Físicos / Evaluaciones
            List<com.pontificia.gym.entity.SeguimientoFisico> seguimientos = seguimientoFisicoService.listarPorCliente(cliente.getId());
            model.addAttribute("seguimientos", seguimientos);

            // Rutinas de Entrenamiento Personalizadas
            List<com.pontificia.gym.entity.Rutina> rutinas = rutinaService.listarPorCliente(cliente.getId());
            model.addAttribute("rutinas", rutinas);

            // Reservas de Clases Grupales
            model.addAttribute("reservasClases", claseGrupalService.listarReservasPorCliente(cliente.getId()));

            // =========================================================================
            // METRICAS ESTILO OPENGYM: TIRA SEMANAL, RUTINA DE HOY, HEATMAP Y PESO
            // =========================================================================
            // 1. Día de la semana actual
            java.time.DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
            String nombreDiaHoy = switch (dayOfWeek) {
                case MONDAY -> "Lunes";
                case TUESDAY -> "Martes";
                case WEDNESDAY -> "Miércoles";
                case THURSDAY -> "Jueves";
                case FRIDAY -> "Viernes";
                case SATURDAY -> "Sábado";
                case SUNDAY -> "Domingo";
            };
            model.addAttribute("diaActualNombre", nombreDiaHoy);
            model.addAttribute("fechaHoyFormateada", LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", new java.util.Locale("es", "PE"))));

            // 2. Rutina para el día de hoy
            com.pontificia.gym.entity.Rutina rutinaHoy = rutinas.stream()
                    .filter(r -> r.getDiaSemana() != null &&
                            (r.getDiaSemana().toLowerCase().contains(nombreDiaHoy.toLowerCase()) ||
                             (nombreDiaHoy.equals("Miércoles") && r.getDiaSemana().toLowerCase().contains("miercoles"))))
                    .findFirst()
                    .orElse(!rutinas.isEmpty() ? rutinas.get(0) : null);
            model.addAttribute("rutinaHoy", rutinaHoy);

            // 3. Tira horizontal de los 7 días de la semana actual (Lunes a Domingo)
            LocalDate lunesSemana = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            java.util.List<java.util.Map<String, Object>> tiraSemanal = new java.util.ArrayList<>();
            java.util.Set<LocalDate> fechasAsistidas = asistencias.stream()
                    .map(a -> a.getFechaHora().toLocalDate())
                    .collect(java.util.stream.Collectors.toSet());

            String[] letrasDias = {"L", "M", "M", "J", "V", "S", "D"};
            String[] nombresCompletos = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
            for (int i = 0; i < 7; i++) {
                LocalDate d = lunesSemana.plusDays(i);
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("letra", letrasDias[i]);
                map.put("numero", d.getDayOfMonth());
                map.put("fecha", d.toString());
                map.put("esHoy", d.equals(LocalDate.now()));
                map.put("asistio", fechasAsistidas.contains(d));
                map.put("nombreDia", nombresCompletos[i]);
                tiraSemanal.add(map);
            }
            model.addAttribute("tiraSemanal", tiraSemanal);

            // 4. Métricas de Asistencia y Heatmap
            int totalAsistencias = asistencias.size();
            int asistenciasMes = (int) asistencias.stream()
                    .filter(a -> a.getFechaHora().getMonth() == LocalDate.now().getMonth() &&
                                 a.getFechaHora().getYear() == LocalDate.now().getYear())
                    .count();

            long semanasConAsistencia = asistencias.stream()
                    .map(a -> a.getFechaHora().getYear() + "-" + a.getFechaHora().get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR))
                    .distinct()
                    .count();
            model.addAttribute("totalAsistencias", totalAsistencias);
            model.addAttribute("asistenciasMes", asistenciasMes);
            model.addAttribute("rachaSemanas", Math.max(1, semanasConAsistencia));

            // Lista de fechas de asistencias en formato YYYY-MM-DD para el Heatmap anual
            String fechasAsistenciasCsv = asistencias.stream()
                    .map(a -> a.getFechaHora().toLocalDate().toString())
                    .distinct()
                    .sorted()
                    .collect(java.util.stream.Collectors.joining(","));
            model.addAttribute("fechasAsistenciasCsv", fechasAsistenciasCsv);

            // 5. Historial de Peso y Meta
            java.util.List<com.pontificia.gym.entity.SeguimientoFisico> segsOrdenados = seguimientos.stream()
                    .sorted(java.util.Comparator.comparing(com.pontificia.gym.entity.SeguimientoFisico::getFechaRegistro))
                    .toList();

            Double pesoActual = null;
            Double pesoAnterior = null;
            Double cambioPeso = 0.0;
            Double pesoMeta = null;

            if (!segsOrdenados.isEmpty()) {
                com.pontificia.gym.entity.SeguimientoFisico ultimo = segsOrdenados.get(segsOrdenados.size() - 1);
                pesoActual = ultimo.getPesoKg();
                if (segsOrdenados.size() > 1) {
                    pesoAnterior = segsOrdenados.get(segsOrdenados.size() - 2).getPesoKg();
                    cambioPeso = Math.round((pesoActual - pesoAnterior) * 10.0) / 10.0;
                }
                Double imc = ultimo.getImc();
                if (imc != null && imc > 25.0) {
                    pesoMeta = Math.round((pesoActual - 3.0) * 10.0) / 10.0;
                } else if (imc != null && imc < 20.0) {
                    pesoMeta = Math.round((pesoActual + 3.0) * 10.0) / 10.0;
                } else {
                    pesoMeta = Math.round((pesoActual - 1.5) * 10.0) / 10.0;
                }
            } else {
                // Valores de demostración si el socio es nuevo
                pesoActual = 74.5;
                pesoMeta = 72.0;
                cambioPeso = -0.5;
            }
            model.addAttribute("pesoActual", pesoActual);
            model.addAttribute("cambioPeso", cambioPeso);
            model.addAttribute("pesoMeta", pesoMeta);
        }

        return "portal/mi_cuenta";
    }
}
