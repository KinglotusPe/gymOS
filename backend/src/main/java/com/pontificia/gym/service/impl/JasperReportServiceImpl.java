package com.pontificia.gym.service.impl;

import com.pontificia.gym.service.JasperReportService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlDigester;
import net.sf.jasperreports.engine.xml.JRXmlDigesterFactory;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JasperReportServiceImpl implements JasperReportService {

    static {
        System.setProperty("net.sf.jasperreports.xml.validation", "false");
        System.setProperty("net.sf.jasperreports.compiler.xml.validation", "false");
        DefaultJasperReportsContext.getInstance().setProperty("net.sf.jasperreports.xml.validation", "false");
        DefaultJasperReportsContext.getInstance().setProperty("net.sf.jasperreports.compiler.xml.validation", "false");
    }

    private final Map<String, JasperReport> reportCache = new ConcurrentHashMap<>();

    @Override
    public byte[] generarReportePdf(String nombreReporte, Map<String, Object> parametros, Collection<?> datos) {
        try {
            JasperReport jasperReport = reportCache.computeIfAbsent(nombreReporte, nombre -> {
                try {
                    // 1. Intentar cargar binario precompilado (.jasper) para rendimiento inmediato (5ms)
                    String rutaJasper = "reports/" + nombre + ".jasper";
                    ClassPathResource jasperRes = new ClassPathResource(rutaJasper);
                    if (jasperRes.exists()) {
                        try (InputStream is = jasperRes.getInputStream()) {
                            return (JasperReport) JRLoader.loadObject(is);
                        }
                    }

                    // 2. Si no existe, compilar en caliente desde .jrxml
                    String rutaJrxml = "reports/" + nombre + ".jrxml";
                    ClassPathResource jrxmlRes = new ClassPathResource(rutaJrxml);
                    if (!jrxmlRes.exists()) {
                        throw new RuntimeException("No se encontro la plantilla de reporte: " + nombre);
                    }

                    DefaultJasperReportsContext ctx = DefaultJasperReportsContext.getInstance();
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    spf.setValidating(false);
                    spf.setNamespaceAware(true);
                    SAXParser saxParser = spf.newSAXParser();

                    JRXmlDigester digester = new JRXmlDigester(saxParser);
                    JRXmlDigesterFactory.configureDigester(ctx, digester);

                    JRXmlLoader loader = new JRXmlLoader(ctx, digester);
                    digester.push(loader);

                    try (InputStream is = jrxmlRes.getInputStream()) {
                        digester.parse(is);
                        Field designField = JRXmlLoader.class.getDeclaredField("jasperDesign");
                        designField.setAccessible(true);
                        JasperDesign design = (JasperDesign) designField.get(loader);
                        design.setLanguage("java");
                        return JasperCompileManager.getInstance(ctx).compile(design);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error compilando reporte Jasper: " + nombre, e);
                }
            });

            JRDataSource dataSource = (datos != null && !datos.isEmpty()) 
                    ? new JRBeanCollectionDataSource(datos) 
                    : new JREmptyDataSource();

            Map<String, Object> params = (parametros != null) ? parametros : new HashMap<>();
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF con JasperReports: " + e.getMessage(), e);
        }
    }
}
