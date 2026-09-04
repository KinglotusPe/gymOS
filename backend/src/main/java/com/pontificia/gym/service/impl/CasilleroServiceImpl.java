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
        if (casilleroRepository.count() == 0) {
            for (int i = 1; i <= 24; i++) {
                String num = String.format("L-%02d", i);
                int piso = (i <= 10) ? 1 : ((i <= 18) ? 2 : 3);
                String ubicacion = (i <= 10) ? "Vestidor Varones - Piso 1" : (i <= 18 ? "Vestidor Damas - Piso 2" : "Zona Funcional & Staff - Piso 3");
                casilleroRepository.save(new Casillero(null, num, ubicacion, piso, "DISPONIBLE"));
            }
        }
    }
}
