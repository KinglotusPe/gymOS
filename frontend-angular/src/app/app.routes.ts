import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ClientesComponent } from './components/clientes/clientes.component';
import { ControlAccesoComponent } from './components/control-acceso/control-acceso.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent, title: 'Brutal Fitness - Dashboard Central' },
  { path: 'clientes', component: ClientesComponent, title: 'Brutal Fitness - Gestión de Socios' },
  { path: 'control-acceso', component: ControlAccesoComponent, title: 'Brutal Fitness - Torniquete & Acceso' },
  { path: '**', redirectTo: 'dashboard' }
];
