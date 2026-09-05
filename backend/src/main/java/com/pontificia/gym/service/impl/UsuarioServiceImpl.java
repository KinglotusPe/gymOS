package com.pontificia.gym.service.impl;

import com.pontificia.gym.entity.Rol;
import com.pontificia.gym.entity.Usuario;
import com.pontificia.gym.repository.ClienteRepository;
import com.pontificia.gym.repository.EntrenadorRepository;
import com.pontificia.gym.repository.UsuarioRepository;
import com.pontificia.gym.service.UsuarioService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              ClienteRepository clienteRepository,
                              EntrenadorRepository entrenadorRepository,
                              @Lazy PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.entrenadorRepository = entrenadorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        if (usuario.getPassword() != null && !usuario.getPassword().startsWith("$2a$") && !usuario.getPassword().startsWith("$2b$")) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        // Sanitizar asociaciones según el rol
        if (usuario.getRol() == Rol.ROLE_ADMIN || usuario.getRol() == Rol.ROLE_RECEPCIONISTA) {
            usuario.setCliente(null);
            usuario.setEntrenador(null);
        } else if (usuario.getRol() == Rol.ROLE_CLIENTE) {
            usuario.setEntrenador(null);
        } else if (usuario.getRol() == Rol.ROLE_ENTRENADOR) {
            usuario.setCliente(null);
        }

        return usuarioRepository.save(usuario);
    }

    @Override
    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    public void inicializarUsuariosPorDefecto() {
        // 1. Admin General
        Usuario admin = usuarioRepository.findByUsername("admin").orElse(new Usuario());
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNombre("César Luza (Administrador General)");
        admin.setRol(Rol.ROLE_ADMIN);
        admin.setActivo(true);
        usuarioRepository.save(admin);

        // 2. Recepcionista / Trabajador
        Usuario recepcion = usuarioRepository.findByUsername("recepcion").orElse(new Usuario());
        recepcion.setUsername("recepcion");
        recepcion.setPassword(passwordEncoder.encode("recepcion123"));
        recepcion.setNombre("Staff Recepción BRUTAL");
        recepcion.setRol(Rol.ROLE_RECEPCIONISTA);
        recepcion.setActivo(true);
        usuarioRepository.save(recepcion);

        // 3. Entrenador Staff
        Usuario entrenador = usuarioRepository.findByUsername("entrenador").orElse(new Usuario());
        entrenador.setUsername("entrenador");
        entrenador.setPassword(passwordEncoder.encode("entrenador123"));
        entrenador.setNombre("Prof. Marco Antonio Vargas");
        entrenador.setRol(Rol.ROLE_ENTRENADOR);
        entrenador.setActivo(true);
        entrenadorRepository.findAll().stream().findFirst().ifPresent(entrenador::setEntrenador);
        usuarioRepository.save(entrenador);

        // 4. Cliente / Socio (ejemplo DNI: 72345678)
        Usuario clienteUser = usuarioRepository.findByUsername("72345678").orElse(new Usuario());
        clienteUser.setUsername("72345678");
        clienteUser.setPassword(passwordEncoder.encode("cliente123"));
        clienteUser.setNombre("Juan Carlos Perez Lopez");
        clienteUser.setRol(Rol.ROLE_CLIENTE);
        clienteUser.setActivo(true);
        clienteRepository.findByDni("72345678").ifPresent(clienteUser::setCliente);
        usuarioRepository.save(clienteUser);
    }

    @Override
    public boolean cambiarPassword(String username, String actual, String nueva) {
        if (username == null || actual == null || nueva == null || nueva.trim().isEmpty()) {
            return false;
        }

        Optional<Usuario> opt = usuarioRepository.findByUsername(username);
        if (opt.isEmpty()) {
            return false;
        }

        Usuario u = opt.get();
        if (!passwordEncoder.matches(actual, u.getPassword())) {
            return false;
        }

        u.setPassword(passwordEncoder.encode(nueva));
        usuarioRepository.save(u);
        return true;
    }
}
