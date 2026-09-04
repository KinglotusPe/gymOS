package com.pontificia.gym.service;

import com.pontificia.gym.entity.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteService {
    List<Cliente> listarTodos();
    Cliente buscarPorId(Long id);
    Optional<Cliente> buscarPorDni(String dni);
    List<Cliente> buscarPorNombreOApellido(String texto);
    org.springframework.data.domain.Page<Cliente> listarPaginado(int pagina, int tamanio, String ordenarPor, String direccion, String buscar);
    Cliente guardar(Cliente cliente);
    void eliminar(Long id);
    long contarTotal();
}
