package com.pontificia.gym.controller.api;

import com.pontificia.gym.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardApiController {

    private final ClienteService clienteService;
    private final MembresiaService membresiaService;
    private final EntrenadorService entrenadorService;
    private final AsistenciaService asistenciaService;
    private final CasilleroService casilleroService;
    private final PagoService pagoService;

    public DashboardApiController(ClienteService clienteService,
                                  MembresiaService membresiaService,
                                  EntrenadorService entrenadorService,
                                  AsistenciaService asistenciaService,
                                  CasilleroService casilleroService,
                                  PagoService pagoService) {
        this.clienteService = clienteService;
        this.membresiaService = membresiaService;
        this.entrenadorService = entrenadorService;
        this.asistenciaService = asistenciaService;
        this.casilleroService = casilleroService;
        this.pagoService = pagoService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSocios", clienteService.contarTotal());
        stats.put("sociosActivos", membresiaService.contarActivas());
        stats.put("sociosVencidos", membresiaService.contarVencidas());
        stats.put("entrenadoresActivos", entrenadorService.contarActivos());
        stats.put("asistenciasHoy", asistenciaService.contarAsistenciasHoy());
        stats.put("lockersLibres", casilleroService.contarDisponibles());
        stats.put("lockersOcupados", casilleroService.contarOcupados());
        
        BigDecimal ingresos = pagoService.totalRecaudadoDelMes();
        stats.put("ingresosMes", ingresos != null ? ingresos : BigDecimal.ZERO);
        
        return ResponseEntity.ok(stats);
    }
}
