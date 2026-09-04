export interface Producto {
  id?: number;
  nombre: string;
  codigoBarras?: string;
  precioVenta: number;
  stockActual: number;
  imagenUrl?: string;
}

export interface ItemVenta {
  productoId: number;
  cantidad: number;
}
