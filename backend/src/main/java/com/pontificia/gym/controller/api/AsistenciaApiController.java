package com.pontificia.gym.controller.api;

import com.pontificia.gym.entity.Asistencia;
import com.pontificia.gym.entity.Casillero;
import com.pontificia.gym.entity.Cliente;
import com.pontificia.gym.entity.Entrenador;
import com.pontificia.gym.entity.Membresia;
import com.pontificia.gym.service.AsistenciaService;
import com.pontificia.gym.service.CasilleroService;
import com.pontificia.gym.service.ClienteService;
import com.pontificia.gym.service.EntrenadorService;
import com.pontificia.gym.service.MembresiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/asistencias")
@CrossOrigin(origins = "*")
public class AsistenciaApiController {

    private final AsistenciaService asistenciaService;
    private final ClienteService clienteService;
    private final EntrenadorService entrenadorService;
    private final MembresiaService membresiaService;
    private final CasilleroService casilleroService;

    private static final Map<Long, LocalDateTime> marcajeEntradaEntrenadores = new HashMap<>();

    public AsistenciaApiController(AsistenciaService asistenciaService,
                                   ClienteService clienteService,
                                   EntrenadorService entrenadorService,
                                   MembresiaService membresiaService,
                                   CasilleroService casilleroService) {
        this.asistenciaService = asistenciaService;
        this.clienteService = clienteService;
        this.entrenadorService = entrenadorService;
        this.membresiaService = membresiaService;
        this.casilleroService = casilleroService;
    }

    @GetMapping("/hoy")
    public ResponseEntity<List<Asistencia>> listarHoy() {
        return ResponseEntity.ok(asistenciaService.listarHoy());
    }

    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Object>> resumenHoy() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAsistenciasHoy", asistenciaService.contarAsistenciasHoy());
        stats.put("lockersLibres", casilleroService.contarDisponibles());
        stats.put("lockersOcupados", casilleroService.contarOcupados());
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/escanear")
    public ResponseEntity<Map<String, Object>> escanearDni(@RequestBody Map<String, String> payload) {
        String dni = payload.get("dni");
        Map<String, Object> resp = new HashMap<>();

        if (dni == null || dni.trim().isEmpty()) {
            resp.put("status", "VACIO");
            return ResponseEntity.badRequest().body(resp);
        }

        String dniLimpio = dni.trim();

        // 1. Entrenador
        Optional<Entrenador> optEntrenador = entrenadorService.buscarPorDni(dniLimpio);
        if (optEntrenador.isPresent()) {
            Entrenador coach = optEntrenador.get();
            LocalDateTime ahora = LocalDateTime.now();

            resp.put("perfil", "ENTRENADOR");
            resp.put("nombreCompleto", coach.getNombreCompleto());
            resp.put("dni", coach.getDni());
            resp.put("especialidad", coach.getEspecialidad());
            resp.put("fotoUrl", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop&q=80");

            if (!marcajeEntradaEntrenadores.containsKey(coach.getId())) {
                marcajeEntradaEntrenadores.put(coach.getId(), ahora);
                Casillero locker = casilleroService.asignarCasilleroLibre(coach.getNombreCompleto(), coach.getDni(), "ENTRENADOR");
                resp.put("status", "ENTRENADOR_ENTRADA");
                resp.put("horaMarcaje", ahora.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                resp.put("casilleroAsignado", locker != null ? locker.getNumero() : "Sin Locker Libre");
            } else {
                LocalDateTime entrada = marcajeEntradaEntrenadores.remove(coach.getId());
                long minutos = ChronoUnit.MINUTES.between(entrada, ahora);
                long horas = minutos / 60;
                long minRestantes = minutos % 60;

                Optional<Casillero> liberado = casilleroService.liberarCasilleroPorDni(coach.getDni());

                resp.put("status", "ENTRENADOR_SALIDA");
                resp.put("horaEntrada", entrada.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                resp.put("horaSalida", ahora.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                resp.put("tiempoTrabajado", horas + "h " + minRestantes + "m");
                resp.put("casilleroLiberado", liberado.map(Casillero::getNumero).orElse(null));
            }
            return ResponseEntity.ok(resp);
        }

        // 2. Socio / Cliente
        Optional<Cliente> optCliente = clienteService.buscarPorDni(dniLimpio);
        if (optCliente.isEmpty()) {
            resp.put("status", "NO_REGISTRADO");
            resp.put("dni", dniLimpio);
            return ResponseEntity.ok(resp);
        }

        Cliente cliente = optCliente.get();
        resp.put("perfil", "SOCIO");
        resp.put("clienteId", cliente.getId());
        resp.put("nombreCompleto", cliente.getNombreCompleto());
        resp.put("dni", cliente.getDni());
        resp.put("fotoUrl", cliente.getFotoUrlOrDefault());

        // Salida si ya tenía casillero
        Optional<Casillero> lockerOcupado = casilleroService.buscarPorDniOcupante(cliente.getDni());
        if (lockerOcupado.isPresent()) {
            Casillero locker = lockerOcupado.get();
            casilleroService.liberarCasilleroPorDni(cliente.getDni());
            resp.put("status", "SALIDA");
            resp.put("casilleroLiberado", locker.getNumero());
            resp.put("horaSalida", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            return ResponseEntity.ok(resp);
        }

        // Entrada: verificar membresía activa
        List<Membresia> membresias = membresiaService.listarPorCliente(cliente.getId());
        LocalDate hoy = LocalDate.now();

        Optional<Membresia> memActiva = membresias.stream()
                .filter(m -> m.getEstado() == com.pontificia.gym.entity.EstadoMembresia.ACTIVA)
                .filter(m -> m.getFechaVencimiento() != null && !m.getFechaVencimiento().isBefore(hoy))
                .findFirst();

        if (memActiva.isPresent()) {
            Membresia m = memActiva.get();
            long diasRestantes = ChronoUnit.DAYS.between(hoy, m.getFechaVencimiento());

            Asistencia asistencia = new Asistencia();
            asistencia.setCliente(cliente);
            asistencia.setFechaHora(LocalDateTime.now());
            asistenciaService.guardar(asistencia);

            Casillero locker = casilleroService.asignarCasilleroLibre(cliente.getNombreCompleto(), cliente.getDni(), "SOCIO");

            resp.put("status", "ACCESO_CONCEDIDO");
            resp.put("plan", m.getTipo() != null ? m.getTipo().name() : "GENERAL");
            resp.put("diasRestantes", diasRestantes);
            resp.put("fechaVencimiento", m.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            resp.put("casilleroAsignado", locker != null ? locker.getNumero() : "Sin Locker Libre");
            resp.put("horaEntrada", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        } else {
            Optional<Membresia> memVencida = membresias.stream()
                    .filter(m -> m.getFechaVencimiento() != null)
                    .max(Comparator.comparing(Membresia::getFechaVencimiento));

            resp.put("status", "MEMBRESIA_VENCIDA");
            if (memVencida.isPresent()) {
                resp.put("plan", memVencida.get().getTipo() != null ? memVencida.get().getTipo().name() : "GENERAL");
                resp.put("fechaVencimiento", memVencida.get().getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else {
                resp.put("plan", "SIN PLAN PREVIO");
                resp.put("fechaVencimiento", "Nunca se inscribió");
            }
        }

        return ResponseEntity.ok(resp);
    }
}
