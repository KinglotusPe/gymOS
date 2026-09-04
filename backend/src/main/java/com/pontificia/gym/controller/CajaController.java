package com.pontificia.gym.controller;

import com.pontificia.gym.dto.CajaResumenDto;
import com.pontificia.gym.service.AuditoriaCajaService;
import com.pontificia.gym.service.UsuarioService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import com.pontificia.gym.entity.Usuario;

@Controller
@RequestMapping("/caja")
public class CajaController {

    private final AuditoriaCajaService auditoriaCajaService;
    private final UsuarioService usuarioService;

    public CajaController(AuditoriaCajaService auditoriaCajaService, UsuarioService usuarioService) {
        this.auditoriaCajaService = auditoriaCajaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String vistaArqueoCaja(Model model,
                                 @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                 @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                 @RequestParam(value = "cajero", required = false) String cajero) {

        if (fechaInicio == null) fechaInicio = LocalDate.now();
        if (fechaFin == null) fechaFin = LocalDate.now();

        CajaResumenDto resumen = auditoriaCajaService.generarResumenCaja(fechaInicio, fechaFin, cajero);

        model.addAttribute("resumen", resumen);
        // Filtrar únicamente personal administrativo / cajeros (excluir clientes)
        List<Usuario> cajeros = usuarioService.listarTodos().stream()
                .filter(u -> u.getRol() != com.pontificia.gym.entity.Rol.ROLE_CLIENTE)
                .toList();
        model.addAttribute("usuarios", cajeros);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("cajeroSeleccionado", cajero);

        return "caja/arqueo";
    }

    @GetMapping("/exportar-excel")
    public ResponseEntity<InputStreamResource> exportarExcel(
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(value = "cajero", required = false) String cajero) {

        if (fechaInicio == null) fechaInicio = LocalDate.now();
        if (fechaFin == null) fechaFin = LocalDate.now();

        CajaResumenDto resumen = auditoriaCajaService.generarResumenCaja(fechaInicio, fechaFin, cajero);
        ByteArrayInputStream in = auditoriaCajaService.generarExcelReporte(resumen);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=arqueo_caja_" + fechaInicio + "_" + fechaFin + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
