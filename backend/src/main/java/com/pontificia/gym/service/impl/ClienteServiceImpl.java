package com.pontificia.gym.service.impl;

import com.pontificia.gym.entity.Cliente;
import com.pontificia.gym.repository.ClienteRepository;
import com.pontificia.gym.service.ClienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorDni(String dni) {
        return clienteRepository.findByDni(dni);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorNombreOApellido(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return listarTodos();
        }
        return clienteRepository.findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(texto.trim(), texto.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Cliente> listarPaginado(int pagina, int tamanio, String ordenarPor, String direccion, String buscar) {
        org.springframework.data.domain.Sort sort = direccion.equalsIgnoreCase("desc") ? 
                org.springframework.data.domain.Sort.by(ordenarPor).descending() : 
                org.springframework.data.domain.Sort.by(ordenarPor).ascending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, pagina), tamanio, sort);

        if (buscar != null && !buscar.trim().isEmpty()) {
            return clienteRepository.findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(buscar.trim(), buscar.trim(), pageable);
        }
        return clienteRepository.findAll(pageable);
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        if (cliente.getFechaInscripcion() == null) {
            cliente.setFechaInscripcion(LocalDate.now());
        }
        return clienteRepository.save(cliente);
    }

    @Override
    public void eliminar(Long id) {
        clienteRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarTotal() {
        return clienteRepository.count();
    }
}
