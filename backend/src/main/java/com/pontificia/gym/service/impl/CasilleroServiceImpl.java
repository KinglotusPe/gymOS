package com.pontificia.gym.service.impl;

import com.pontificia.gym.entity.Casillero;
import com.pontificia.gym.repository.CasilleroRepository;
import com.pontificia.gym.service.CasilleroService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CasilleroServiceImpl implements CasilleroService {

    private final CasilleroRepository casilleroRepository;

    public CasilleroServiceImpl(CasilleroRepository casilleroRepository) {
        this.casilleroRepository = casilleroRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Casillero> listarTodos() {
        return casilleroRepository.findAllByOrderByNumeroAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Casillero> listarDisponibles() {
        return casilleroRepository.findByEstado("DISPONIBLE");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Casillero> buscarPorId(Long id) {
        return casilleroRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Casillero> buscarPorDniOcupante(String dni) {
        if (dni == null || dni.trim().isEmpty()) return Optional.empty();
        return casilleroRepository.findByOcupadoPorDni(dni.trim());
    }

    @Override
    public Casillero asignarCasilleroLibre(String nombre, String dni, String tipo) {
        // Si ya tiene un casillero asignado, devolverlo
        Optional<Casillero> yaAsignado = buscarPorDniOcupante(dni);
        if (yaAsignado.isPresent()) {
            return yaAsignado.get();
        }

        List<Casillero> libres = listarDisponibles();
        if (libres.isEmpty()) {
            return null; // Todos ocupados
        }

        Casillero libre = libres.get(0);
        libre.setEstado("OCUPADO");
        libre.setOcupadoPorNombre(nombre);
        libre.setOcupadoPorDni(dni);
        libre.setOcupadoPorTipo(tipo != null ? tipo : "SOCIO");
        libre.setFechaOcupacion(LocalDateTime.now());

        return casilleroRepository.save(libre);
    }

    @Override
    public Casillero asignarCasilleroEspecifico(Long casilleroId, String nombre, String dni, String tipo) {
        Casillero casillero = casilleroRepository.findById(casilleroId)
                .orElseThrow(() -> new IllegalArgumentException("Casillero no encontrado ID: " + casilleroId));

        casillero.setEstado("OCUPADO");
        casillero.setOcupadoPorNombre(nombre);
        casillero.setOcupadoPorDni(dni);
        casillero.setOcupadoPorTipo(tipo != null ? tipo : "SOCIO");
        casillero.setFechaOcupacion(LocalDateTime.now());

        return casilleroRepository.save(casillero);
    }

    @Override
    public Optional<Casillero> liberarCasilleroPorDni(String dni) {
        Optional<Casillero> ocupado = buscarPorDniOcupante(dni);
        if (ocupado.isPresent()) {
            Casillero c = ocupado.get();
            c.setEstado("DISPONIBLE");
            c.setOcupadoPorNombre(null);
            c.setOcupadoPorDni(null);
            c.setOcupadoPorTipo(null);
            c.setFechaOcupacion(null);
            casilleroRepository.save(c);
            return Optional.of(c);
        }
        return Optional.empty();
    }

    @Override
    public void liberarCasilleroPorId(Long id) {
        casilleroRepository.findById(id).ifPresent(c -> {
            c.setEstado("DISPONIBLE");
            c.setOcupadoPorNombre(null);
            c.setOcupadoPorDni(null);
            c.setOcupadoPorTipo(null);
            c.setFechaOcupacion(null);
            casilleroRepository.save(c);
        });
    }

    @Override
    public void liberarTodos() {
        List<Casillero> ocupados = casilleroRepository.findByEstado("OCUPADO");
        for (Casillero c : ocupados) {
            c.setEstado("DISPONIBLE");
            c.setOcupadoPorNombre(null);
            c.setOcupadoPorDni(null);
            c.setOcupadoPorTipo(null);
            c.setFechaOcupacion(null);
            casilleroRepository.save(c);
        }
    }

    @Override
    public Casillero guardar(Casillero casillero) {
        return casilleroRepository.save(casillero);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarDisponibles() {
        return casilleroRepository.countByEstado("DISPONIBLE");
    }

    @Override
    @Transactional(readOnly = true)
    public long contarOcupados() {
        return casilleroRepository.countByEstado("OCUPADO");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Casillero> listarPorPiso(Integer piso) {
        if (piso == null || piso == 0) {
            return casilleroRepository.findAllByOrderByNumeroAsc();
        }
        return casilleroRepository.findByPisoOrderByNumeroAsc(piso);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> listarPisos() {
        List<Integer> pisos = casilleroRepository.findAll().stream()
                .map(Casillero::getPiso)
                .filter(p -> p != null && p > 0)
                .distinct()
                .sorted()
                .toList();
        if (pisos.isEmpty()) {
            return List.of(1, 2, 3);
        }
        return pisos;
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        casilleroRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNumero(String numero) {
        return casilleroRepository.existsByNumero(numero);
    }

    @Override
    public void inicializarCasillerosPorDefecto() {
        // 1. Corregir cualquier casillero existente con piso <= 0 o sin piso
        List<Casillero> existentes = casilleroRepository.findAll();
        for (Casillero c : existentes) {
            boolean modificado = false;
            if (c.getPiso() == null || c.getPiso() <= 0) {
                if (c.getUbicacion() != null && c.getUbicacion().contains("Piso 2")) {
                    c.setPiso(2);
                } else if (c.getUbicacion() != null && c.getUbicacion().contains("Piso 3")) {
                    c.setPiso(3);
                } else {
                    c.setPiso(1);
                }
                modificado = true;
            }

            // Si está ocupado pero no tiene datos de ocupante, asignar datos reales del gimnasio
            if ("OCUPADO".equalsIgnoreCase(c.getEstado()) && (c.getOcupadoPorNombre() == null || c.getOcupadoPorNombre().trim().isEmpty())) {
                if ("L-01".equalsIgnoreCase(c.getNumero())) {
                    c.setOcupadoPorNombre("Juan Carlos Pérez");
                    c.setOcupadoPorDni("72345678");
                    c.setOcupadoPorTipo("SOCIO");
                    c.setFechaOcupacion(LocalDateTime.now().minusMinutes(42));
                } else if ("L-05".equalsIgnoreCase(c.getNumero())) {
                    c.setOcupadoPorNombre("María Elena Gómez");
                    c.setOcupadoPorDni("73456789");
                    c.setOcupadoPorTipo("SOCIO");
                    c.setFechaOcupacion(LocalDateTime.now().minusMinutes(25));
                } else {
                    c.setOcupadoPorNombre("Socio Activo");
                    c.setOcupadoPorDni("70112233");
                    c.setOcupadoPorTipo("SOCIO");
                    c.setFechaOcupacion(LocalDateTime.now().minusMinutes(15));
                }
                modificado = true;
            }

            if (modificado) {
                casilleroRepository.save(c);
            }
        }

        // 2. Si hay menos de 18 casilleros, generar casilleros distribuidos en los pisos 1, 2 y 3 para un gimnasio completo
        if (casilleroRepository.count() < 18) {
            for (int i = 1; i <= 18; i++) {
                String num = String.format("L-%02d", i);
                if (!casilleroRepository.existsByNumero(num)) {
                    int piso = (i <= 6) ? 1 : ((i <= 12) ? 2 : 3);
                    String ubicacion = (i <= 6) 
                            ? ((i % 2 != 0) ? "Vestidor Varones - Piso 1" : "Vestidor Damas - Piso 1") 
                            : ((i <= 12) ? "Zona Musculación & Cardio - Piso 2" : "Área Funcional & Spinning - Piso 3");
                    casilleroRepository.save(new Casillero(null, num, ubicacion, piso, "DISPONIBLE"));
                }
            }
        }
    }
}
