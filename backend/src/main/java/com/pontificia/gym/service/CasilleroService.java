package com.pontificia.gym.service;

import com.pontificia.gym.entity.Casillero;

import java.util.List;
import java.util.Optional;

public interface CasilleroService {
    List<Casillero> listarTodos();
    List<Casillero> listarDisponibles();
    Optional<Casillero> buscarPorId(Long id);
    Optional<Casillero> buscarPorDniOcupante(String dni);

    Casillero asignarCasilleroLibre(String nombre, String dni, String tipo);
    Casillero asignarCasilleroEspecifico(Long casilleroId, String nombre, String dni, String tipo);
    Optional<Casillero> liberarCasilleroPorDni(String dni);
    void liberarCasilleroPorId(Long id);
    void liberarTodos();
    Casillero guardar(Casillero casillero);

    long contarDisponibles();
    long contarOcupados();

    List<Casillero> listarPorPiso(Integer piso);
    List<Integer> listarPisos();
    void eliminar(Long id);
    boolean existePorNumero(String numero);

    void inicializarCasillerosPorDefecto();
}
