package com.pontificia.gym.controller;

import com.pontificia.gym.dto.CajaResumenDto;
import com.pontificia.gym.entity.Cliente;
import com.pontificia.gym.entity.Pago;
import com.pontificia.gym.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final JasperReportService jasperReportService;
    private final PagoService pagoService;
    private final ClienteService clienteService;
    private final AuditoriaCajaService auditoriaCajaService;
    private final AsistenciaService asistenciaService;

    public ReporteController(JasperReportService jasperReportService,
                             PagoService pagoService,
                             ClienteService clienteService,
                             AuditoriaCajaService auditoriaCajaService,
                             AsistenciaService asistenciaService) {
        this.jasperReportService = jasperReportService;
        this.pagoService = pagoService;
        this.clienteService = clienteService;
        this.auditoriaCajaService = auditoriaCajaService;
        this.asistenciaService = asistenciaService;
    }

    @GetMapping("/boleta/{pagoId}")
    public ResponseEntity<byte[]> descargarBoletaPdf(@PathVariable Long pagoId) {
        Pago pago = pagoService.buscarPorId(pagoId);
        if (pago == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("EMPRESA_NOMBRE", "BRUTAL FITNESS GYM");
        params.put("EMPRESA_DIRECCION", "Jr. José Santos Chocano - Jesús Nazareno, Ayacucho");
        params.put("EMPRESA_RUC", "20601234567");
        params.put("CLIENTE_NOMBRE", pago.getCliente().getNombreCompleto());
        params.put("CLIENTE_DNI", pago.getCliente().getDni());
        params.put("PAGO_FECHA", pago.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        params.put("PAGO_METODO", pago.getMetodoPago().name());
        params.put("PAGO_NUMERO", String.format("B001-%06d", pago.getId()));
        params.put("CONCEPTO", "Pago de Membresía de Gimnasio — Servicio Deportivo");
        params.put("MONTO", pago.getMonto());

        byte[] pdfBytes = jasperReportService.generarReportePdf("boleta_pago", params, null);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=boleta_" + pago.getId() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/socios")
    public ResponseEntity<byte[]> descargarPadronSociosPdf() {
        List<Cliente> clientes = clienteService.listarTodos();

        Map<String, Object> params = new HashMap<>();
        params.put("FECHA_GENERACION", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        params.put("TOTAL_SOCIOS", (long) clientes.size());

        byte[] pdfBytes = jasperReportService.generarReportePdf("reporte_socios", params, clientes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=padron_socios.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/caja")
    public ResponseEntity<byte[]> descargarArqueoCajaPdf(
            @RequestParam(name = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(name = "fin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        LocalDate fechaInicio = (inicio != null) ? inicio : LocalDate.now();
        LocalDate fechaFin = (fin != null) ? fin : LocalDate.now();

        CajaResumenDto resumen = auditoriaCajaService.generarResumenCaja(fechaInicio, fechaFin, null);

        BigDecimal digital = resumen.getTotalYapePlin()
                .add(resumen.getTotalTarjeta())
                .add(resumen.getTotalTransferencia());

        Map<String, Object> params = new HashMap<>();
        params.put("FECHA_INICIO", fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        params.put("FECHA_FIN", fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        params.put("TOTAL_INGRESOS", resumen.getTotalGeneral());
        params.put("TOTAL_MEMBRESIAS", resumen.getTotalSuscripciones());
        params.put("TOTAL_TIENDA", resumen.getTotalTienda());
        params.put("TOTAL_EFECTIVO", resumen.getTotalEfectivo());
        params.put("TOTAL_DIGITAL", digital);

        byte[] pdfBytes = jasperReportService.generarReportePdf("cierre_caja", params, null);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=arqueo_caja.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/exportar/pagos.csv")
    public ResponseEntity<byte[]> exportarPagosCsv() {
        List<Pago> pagos = pagoService.listarTodos();
        StringBuilder sb = new StringBuilder("\uFEFF"); // BOM para compatibilidad con Excel
        sb.append("ID,Comprobante,DNI_Cliente,Nombre_Cliente,Monto_Soles,Fecha_Cobro,Metodo_Pago,Proxima_Fecha_Pago\n");

        for (Pago p : pagos) {
            sb.append(p.getId()).append(",")
              .append("REC-").append(String.format("%06d", p.getId())).append(",")
              .append("\"").append(p.getCliente() != null ? p.getCliente().getDni() : "").append("\",")
              .append("\"").append(p.getCliente() != null ? p.getCliente().getNombreCompleto().replace("\"", "\"\"") : "").append("\",")
              .append(p.getMonto()).append(",")
              .append(p.getFecha()).append(",")
              .append(p.getMetodoPago() != null ? p.getMetodoPago().name() : "").append(",")
              .append(p.getProximaFechaPago() != null ? p.getProximaFechaPago().toString() : "").append("\n");
        }

        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_pagos_" + LocalDate.now() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping("/exportar/clientes.csv")
    public ResponseEntity<byte[]> exportarClientesCsv() {
        List<Cliente> clientes = clienteService.listarTodos();
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("ID,DNI,Nombres,Apellidos,Telefono,Edad,Fecha_Inscripcion\n");

        for (Cliente c : clientes) {
            sb.append(c.getId()).append(",")
              .append("\"").append(c.getDni()).append("\",")
              .append("\"").append(c.getNombres() != null ? c.getNombres().replace("\"", "\"\"") : "").append("\",")
              .append("\"").append(c.getApellidos() != null ? c.getApellidos().replace("\"", "\"\"") : "").append("\",")
              .append("\"").append(c.getTelefono() != null ? c.getTelefono() : "").append("\",")
              .append(c.getEdad() != null ? c.getEdad() : "").append(",")
              .append(c.getFechaInscripcion() != null ? c.getFechaInscripcion().toString() : "").append("\n");
        }

        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=padron_clientes_" + LocalDate.now() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping("/exportar/asistencias.csv")
    public ResponseEntity<byte[]> exportarAsistenciasCsv() {
        List<com.pontificia.gym.entity.Asistencia> asistencias = asistenciaService.listarTodos();
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("ID,DNI_Cliente,Nombre_Cliente,Fecha_Hora\n");

        for (com.pontificia.gym.entity.Asistencia a : asistencias) {
            sb.append(a.getId()).append(",")
              .append("\"").append(a.getCliente() != null ? a.getCliente().getDni() : "").append("\",")
              .append("\"").append(a.getCliente() != null ? a.getCliente().getNombreCompleto().replace("\"", "\"\"") : "").append("\",")
              .append(a.getFechaHora() != null ? a.getFechaHora().toString() : "").append("\n");
        }

        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historial_asistencias_" + LocalDate.now() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping("/backup-sql")
    public ResponseEntity<byte[]> descargarBackupSql() {
        StringBuilder sb = new StringBuilder();
        sb.append("-- =====================================================\n");
        sb.append("-- BRUTAL FITNESS GYM - RESPALDO AUTOMATIZADO DE DATOS\n");
        sb.append("-- Fecha de Respaldo: ").append(java.time.LocalDateTime.now()).append("\n");
        sb.append("-- =====================================================\n\n");

        sb.append("-- TABLA: CLIENTES\n");
        for (Cliente c : clienteService.listarTodos()) {
            sb.append("INSERT INTO cliente (id, dni, nombres, apellidos, telefono, edad, fecha_inscripcion) VALUES (")
              .append(c.getId()).append(", ")
              .append("'").append(c.getDni()).append("', ")
              .append("'").append(c.getNombres().replace("'", "''")).append("', ")
              .append("'").append(c.getApellidos().replace("'", "''")).append("', ")
              .append(c.getTelefono() != null ? "'" + c.getTelefono() + "'" : "NULL").append(", ")
              .append(c.getEdad() != null ? c.getEdad() : "NULL").append(", ")
              .append(c.getFechaInscripcion() != null ? "'" + c.getFechaInscripcion() + "'" : "NULL").append(");\n");
        }

        sb.append("\n-- TABLA: PAGOS\n");
        for (Pago p : pagoService.listarTodos()) {
            sb.append("INSERT INTO pago (id, cliente_id, monto, fecha, metodo_pago, proxima_fecha_pago) VALUES (")
              .append(p.getId()).append(", ")
              .append(p.getCliente() != null ? p.getCliente().getId() : "NULL").append(", ")
              .append(p.getMonto()).append(", ")
              .append("'").append(p.getFecha()).append("', ")
              .append("'").append(p.getMetodoPago() != null ? p.getMetodoPago().name() : "EFECTIVO").append("', ")
              .append(p.getProximaFechaPago() != null ? "'" + p.getProximaFechaPago() + "'" : "NULL").append(");\n");
        }

        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=backup_brutal_fitness_" + LocalDate.now() + ".sql")
                .contentType(MediaType.parseMediaType("application/sql; charset=UTF-8"))
                .body(bytes);
    }
}
