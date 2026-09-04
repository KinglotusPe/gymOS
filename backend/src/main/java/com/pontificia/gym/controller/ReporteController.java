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

    public ReporteController(JasperReportService jasperReportService,
                             PagoService pagoService,
                             ClienteService clienteService,
                             AuditoriaCajaService auditoriaCajaService) {
        this.jasperReportService = jasperReportService;
        this.pagoService = pagoService;
        this.clienteService = clienteService;
        this.auditoriaCajaService = auditoriaCajaService;
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
}
