import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardService } from '../../services/dashboard.service';
import { AsistenciaService } from '../../services/asistencia.service';
import { FirebaseCloudService } from '../../services/firebase-cloud.service';
import { DashboardStats } from '../../models/dashboard-stats.model';
import { Asistencia } from '../../models/asistencia.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  stats: DashboardStats = {
    totalSocios: 0,
    sociosActivos: 0,
    sociosVencidos: 0,
    entrenadoresActivos: 0,
    asistenciasHoy: 0,
    lockersLibres: 0,
    lockersOcupados: 0,
    ingresosMes: 0
  };

  asistenciasRecientes: Asistencia[] = [];
  cargando: boolean = true;
  cloudSyncActivo: boolean = false;

  constructor(
    private dashboardService: DashboardService,
    private asistenciaService: AsistenciaService,
    private firebaseService: FirebaseCloudService
  ) {}

  ngOnInit(): void {
    this.cargarDatos();
    this.cloudSyncActivo = this.firebaseService.getCloudStatus();
  }

  cargarDatos(): void {
    this.cargando = true;
    this.dashboardService.obtenerStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error cargando estadísticas:', err);
        this.cargando = false;
      }
    });

    this.asistenciaService.listarHoy().subscribe({
      next: (data) => {
        this.asistenciasRecientes = data.slice(0, 8);
      },
      error: (err) => {
        console.warn('No se pudieron cargar asistencias recientes:', err);
      }
    });
  }
}
