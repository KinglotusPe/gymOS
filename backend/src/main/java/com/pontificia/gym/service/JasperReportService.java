package com.pontificia.gym.service;

import java.util.Collection;
import java.util.Map;

public interface JasperReportService {
    byte[] generarReportePdf(String nombreReporte, Map<String, Object> parametros, Collection<?> datos);
}
