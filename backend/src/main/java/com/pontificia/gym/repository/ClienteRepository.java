package com.pontificia.gym.repository;

import com.pontificia.gym.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository (DAO moderno): capa que se comunica con la base de datos.
 * Al extender JpaRepository heredamos save(), findAll(), findById(), delete(), etc.
 * No escribimos SQL manual.
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Query method: Spring Data JPA genera la consulta a partir del nombre
    Optional<Cliente> findByDni(String dni);

    List<Cliente> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
            String nombres, String apellidos);

    org.springframework.data.domain.Page<Cliente> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
            String nombres, String apellidos, org.springframework.data.domain.Pageable pageable);

    boolean existsByDni(String dni);
}
