import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../services/cliente.service';
import { Cliente, PaginaClientes } from '../../models/cliente.model';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './clientes.component.html',
  styleUrls: ['./clientes.component.css']
})
export class ClientesComponent implements OnInit {

  pagina: number = 0;
  tamanio: number = 5;
  ordenarPor: string = 'id';
  direccion: string = 'desc';
  buscar: string = '';

  paginaData: PaginaClientes = {
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 5,
    number: 0,
    first: true,
    last: true
  };

  cargando: boolean = false;

  // Modal para agregar socio
  modalAbierto: boolean = false;
  nuevoSocio: Cliente = {
    dni: '',
    nombres: '',
    apellidos: '',
    edad: 25,
    telefono: '',
    fotoUrl: ''
  };
  mensajeError: string = '';
  mensajeExito: string = '';

  constructor(private clienteService: ClienteService) {}

  ngOnInit(): void {
    this.cargarSocios();
  }

  cargarSocios(): void {
    this.cargando = true;
    this.clienteService
      .listarPaginado(this.pagina, this.tamanio, this.ordenarPor, this.direccion, this.buscar)
      .subscribe({
        next: (data) => {
          this.paginaData = data;
          this.cargando = false;
        },
        error: (err) => {
          console.error('Error cargando socios paginados:', err);
          this.cargando = false;
        }
      });
  }

  cambiarOrden(columna: string): void {
    if (this.ordenarPor === columna) {
      this.direccion = this.direccion === 'asc' ? 'desc' : 'asc';
    } else {
      this.ordenarPor = columna;
      this.direccion = 'asc';
    }
    this.pagina = 0;
    this.cargarSocios();
  }

  buscarSocios(): void {
    this.pagina = 0;
    this.cargarSocios();
  }

  irAPagina(p: number): void {
    if (p >= 0 && p < this.paginaData.totalPages) {
      this.pagina = p;
      this.cargarSocios();
    }
  }

  abrirModal(): void {
    this.nuevoSocio = {
      dni: '',
      nombres: '',
      apellidos: '',
      edad: 25,
      telefono: '',
      fotoUrl: ''
    };
    this.mensajeError = '';
    this.mensajeExito = '';
    this.modalAbierto = true;
  }

  cerrarModal(): void {
    this.modalAbierto = false;
  }

  guardarSocio(): void {
    if (!this.nuevoSocio.dni || !this.nuevoSocio.nombres || !this.nuevoSocio.apellidos) {
      this.mensajeError = 'Por favor complete los campos obligatorios (DNI, Nombres, Apellidos).';
      return;
    }

    this.clienteService.guardar(this.nuevoSocio).subscribe({
      next: (creado) => {
        this.mensajeExito = `¡Socio ${creado.nombres} registrado con éxito!`;
        setTimeout(() => {
          this.cerrarModal();
          this.cargarSocios();
        }, 1200);
      },
      error: (err) => {
        this.mensajeError = err.error?.error || 'Error al registrar el socio';
      }
    });
  }

  eliminarSocio(id?: number): void {
    if (!id) return;
    if (confirm('¿Está seguro de eliminar a este socio?')) {
      this.clienteService.eliminar(id).subscribe({
        next: () => {
          this.cargarSocios();
        },
        error: (err) => {
          alert('Error eliminando socio: ' + (err.error?.error || err.message));
        }
      });
    }
  }
}
