package com.pontificia.gym.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "casillero")
public class Casillero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String numero; // Ej: "L-01", "L-02", "01", "02"

    @Column(length = 100)
    private String ubicacion; // "Vestuario Masculino", "Vestuario Femenino", "Zona General"

    @Column(nullable = false, length = 30)
    private String estado = "DISPONIBLE"; // "DISPONIBLE", "OCUPADO", "MANTENIMIENTO"

    @Column(nullable = false)
    private Integer piso = 1; // 1, 2, 3, etc.

    @Column(name = "ocupado_por_nombre", length = 150)
    private String ocupadoPorNombre;

    @Column(name = "ocupado_por_dni", length = 30)
    private String ocupadoPorDni;

    @Column(name = "ocupado_por_tipo", length = 30)
    private String ocupadoPorTipo; // "SOCIO", "ENTRENADOR", "RECEPCION"

    @Column(name = "fecha_ocupacion")
    private LocalDateTime fechaOcupacion;

    public Casillero() {
    }

    public Casillero(Long id, String numero, String ubicacion, String estado) {
        this(id, numero, ubicacion, 1, estado);
    }

    public Casillero(Long id, String numero, String ubicacion, Integer piso, String estado) {
        this.id = id;
        this.numero = numero;
        this.ubicacion = ubicacion;
        this.piso = (piso != null && piso > 0) ? piso : 1;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getOcupadoPorNombre() {
        return ocupadoPorNombre;
    }

    public void setOcupadoPorNombre(String ocupadoPorNombre) {
        this.ocupadoPorNombre = ocupadoPorNombre;
    }

    public String getOcupadoPorDni() {
        return ocupadoPorDni;
    }

    public void setOcupadoPorDni(String ocupadoPorDni) {
        this.ocupadoPorDni = ocupadoPorDni;
    }

    public String getOcupadoPorTipo() {
        return ocupadoPorTipo;
    }

    public void setOcupadoPorTipo(String ocupadoPorTipo) {
        this.ocupadoPorTipo = ocupadoPorTipo;
    }

    public LocalDateTime getFechaOcupacion() {
        return fechaOcupacion;
    }

    public void setFechaOcupacion(LocalDateTime fechaOcupacion) {
        this.fechaOcupacion = fechaOcupacion;
    }

    public Integer getPiso() {
        return piso != null ? piso : 1;
    }

    public void setPiso(Integer piso) {
        this.piso = (piso != null && piso > 0) ? piso : 1;
    }

    public boolean isDisponible() {
        return "DISPONIBLE".equalsIgnoreCase(this.estado);
    }
}
