import { Cliente } from './cliente.model';

export enum TipoMembresia {
  DIARIO = 'DIARIO',
  SEMANAL = 'SEMANAL',
  MENSUAL = 'MENSUAL',
  TRIMESTRAL = 'TRIMESTRAL',
  ANUAL = 'ANUAL',
  PERSONALIZADA = 'PERSONALIZADA'
}

export enum EstadoMembresia {
  ACTIVA = 'ACTIVA',
  VENCIDA = 'VENCIDA'
}

export interface Membresia {
  id?: number;
  cliente: Cliente;
  tipo: TipoMembresia;
  fechaInicio: string;
  fechaVencimiento: string;
  estado: EstadoMembresia;
}
