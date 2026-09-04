import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AsistenciaService } from '../../services/asistencia.service';
import { FirebaseCloudService } from '../../services/firebase-cloud.service';
import { EscaneoResultado, ResumenAsistenciasHoy } from '../../models/asistencia.model';

@Component({
  selector: 'app-control-acceso',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './control-acceso.component.html',
  styleUrls: ['./control-acceso.component.css']
})
export class ControlAccesoComponent implements OnInit, AfterViewInit {

  @ViewChild('dniInput') dniInputElement!: ElementRef<HTMLInputElement>;

  dni: string = '';
  cargando: boolean = false;
  resultado: EscaneoResultado | null = null;
  historialMarcajes: EscaneoResultado[] = [];

  resumen: ResumenAsistenciasHoy = {
    totalAsistenciasHoy: 0,
    lockersLibres: 5,
    lockersOcupados: 0
  };

  constructor(
    private asistenciaService: AsistenciaService,
    private firebaseService: FirebaseCloudService
  ) {}

  ngOnInit(): void {
    this.cargarResumen();
  }

  ngAfterViewInit(): void {
    this.enfocarInput();
  }

  enfocarInput(): void {
    setTimeout(() => {
      if (this.dniInputElement) {
        this.dniInputElement.nativeElement.focus();
      }
    }, 100);
  }

  cargarResumen(): void {
    this.asistenciaService.obtenerResumen().subscribe({
      next: (data) => {
        this.resumen = data;
      },
      error: (err) => console.warn('Error cargando resumen:', err)
    });
  }

  procesarEscaneo(): void {
    if (!this.dni || this.dni.trim().length === 0) return;

    const dniLimpio = this.dni.trim();
    this.cargando = true;

    this.asistenciaService.escanearDni(dniLimpio).subscribe({
      next: (res) => {
        this.resultado = res;
        this.cargando = false;
        this.historialMarcajes.unshift(res);
        if (this.historialMarcajes.length > 6) {
          this.historialMarcajes.pop();
        }

        // 1. Reproducir sonido según resultado (Web Audio API)
        this.reproducirSonido(res.status);

        // 2. Sincronizar en la nube con Firebase
        this.firebaseService.sincronizarAccesoEnLaNube(res);

        // 3. Actualizar resumen de lockers y asistencias
        this.cargarResumen();

        // 4. Limpiar input y reenfocar para el siguiente escaneo
        this.dni = '';
        this.enfocarInput();
      },
      error: (err) => {
        console.error('Error al verificar acceso:', err);
        this.cargando = false;
        this.resultado = {
          status: 'NO_REGISTRADO',
          dni: dniLimpio
        };
        this.reproducirSonido('NO_REGISTRADO');
        this.dni = '';
        this.enfocarInput();
      }
    });
  }

  /**
   * Genera tonos de audio vía Web Audio API según el estado del escaneo
   */
  private reproducirSonido(status: string): void {
    try {
      const AudioCtx = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      if (!AudioCtx) return;
      const ctx = new AudioCtx();

      if (status === 'ACCESO_CONCEDIDO' || status === 'ENTRENADOR_ENTRADA') {
        // Tono ascendente agradable (Éxito)
        this.playTone(ctx, 523.25, 0.1, 0); // Do
        this.playTone(ctx, 659.25, 0.1, 0.1); // Mi
        this.playTone(ctx, 783.99, 0.2, 0.2); // Sol
      } else if (status === 'SALIDA' || status === 'ENTRENADOR_SALIDA') {
        // Tono neutro de salida
        this.playTone(ctx, 440.00, 0.15, 0);
        this.playTone(ctx, 349.23, 0.2, 0.15);
      } else {
        // Doble tono grave de alerta (Denegado / Vencido)
        this.playTone(ctx, 220.00, 0.15, 0);
        this.playTone(ctx, 196.00, 0.25, 0.18);
      }
    } catch {
      // Audio no soportado o silenciado por el navegador
    }
  }

  private playTone(ctx: AudioContext, freq: number, duration: number, delay: number): void {
    setTimeout(() => {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(freq, ctx.currentTime);
      gain.gain.setValueAtTime(0.15, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + duration);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start();
      osc.stop(ctx.currentTime + duration);
    }, delay * 1000);
  }
}
