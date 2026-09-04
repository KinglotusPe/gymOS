export interface Cliente {
  id?: number;
  dni: string;
  nombres: string;
  apellidos: string;
  nombreCompleto?: string;
  edad?: number;
  telefono?: string;
  fotoUrl?: string;
  fotoUrlOrDefault?: string;
  fechaInscripcion?: string;
}

export interface PaginaClientes {
  content: Cliente[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
