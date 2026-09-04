import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Asistencia, EscaneoResultado, ResumenAsistenciasHoy } from '../models/asistencia.model';

@Injectable({
  providedIn: 'root'
})
export class AsistenciaService {

  private readonly apiUrl = 'http://localhost:8080/api/asistencias';

  constructor(private http: HttpClient) {}

  listarHoy(): Observable<Asistencia[]> {
    return this.http.get<Asistencia[]>(`${this.apiUrl}/hoy`);
  }

  obtenerResumen(): Observable<ResumenAsistenciasHoy> {
    return this.http.get<ResumenAsistenciasHoy>(`${this.apiUrl}/resumen`);
  }

  escanearDni(dni: string): Observable<EscaneoResultado> {
    return this.http.post<EscaneoResultado>(`${this.apiUrl}/escanear`, { dni });
  }
}
