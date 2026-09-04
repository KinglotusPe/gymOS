import { Cliente } from './cliente.model';

export interface Asistencia {
  id?: number;
  cliente: Cliente;
  fechaHora: string;
}

export type EscaneoStatus =
  | 'ACCESO_CONCEDIDO'
  | 'SALIDA'
  | 'MEMBRESIA_VENCIDA'
  | 'NO_REGISTRADO'
  | 'ENTRENADOR_ENTRADA'
  | 'ENTRENADOR_SALIDA'
  | 'VACIO';

export interface EscaneoResultado {
  status: EscaneoStatus;
  perfil?: 'SOCIO' | 'ENTRENADOR';
  clienteId?: number;
  nombreCompleto?: string;
  dni?: string;
  fotoUrl?: string;
  plan?: string;
  diasRestantes?: number;
  fechaVencimiento?: string;
  casilleroAsignado?: string;
  casilleroLiberado?: string;
  horaEntrada?: string;
  horaSalida?: string;
  horaMarcaje?: string;
  tiempoTrabajado?: string;
  especialidad?: string;
}

export interface ResumenAsistenciasHoy {
  totalAsistenciasHoy: number;
  lockersLibres: number;
  lockersOcupados: number;
}
