package com.pontificia.gym.service;

import com.pontificia.gym.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<Usuario> listarTodos();
    Optional<Usuario> buscarPorId(Long id);
    Optional<Usuario> buscarPorUsername(String username);
    Usuario guardar(Usuario usuario);
    void eliminar(Long id);
    void inicializarUsuariosPorDefecto();
    boolean cambiarPassword(String username, String actual, String nueva);
}
