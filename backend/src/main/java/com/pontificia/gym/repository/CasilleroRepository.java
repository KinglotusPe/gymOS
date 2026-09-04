package com.pontificia.gym.repository;

import com.pontificia.gym.entity.Casillero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CasilleroRepository extends JpaRepository<Casillero, Long> {
    List<Casillero> findAllByOrderByNumeroAsc();
    List<Casillero> findByPisoOrderByNumeroAsc(Integer piso);
    List<Casillero> findByEstado(String estado);
    Optional<Casillero> findByNumero(String numero);
    boolean existsByNumero(String numero);
    Optional<Casillero> findByOcupadoPorDni(String dni);
    long countByEstado(String estado);
}
