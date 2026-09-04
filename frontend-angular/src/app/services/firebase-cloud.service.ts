import { Injectable } from '@angular/core';
import { initializeApp, FirebaseApp } from 'firebase/app';
import { getDatabase, ref, push, set, Database } from 'firebase/database';
import { firebaseConfig } from '../config/firebase.config';
import { EscaneoResultado } from '../models/asistencia.model';

@Injectable({
  providedIn: 'root'
})
export class FirebaseCloudService {

  private app: FirebaseApp | null = null;
  private db: Database | null = null;
  private isConnected: boolean = false;

  constructor() {
    this.initFirebase();
  }

  /**
   * Inicializa la conexión con los servicios en la nube de Google Firebase
   */
  private initFirebase(): void {
    try {
      this.app = initializeApp(firebaseConfig);
      this.db = getDatabase(this.app);
      this.isConnected = true;
      console.log('✅ [Firebase Cloud] Conectado exitosamente con Google Cloud Firebase');
    } catch (error) {
      console.warn('⚠️ [Firebase Cloud] Modo offline activado (simulando almacenamiento seguro):', error);
      this.isConnected = false;
    }
  }

  /**
   * Sincroniza un evento de acceso / marcaje en tiempo real a Firebase Realtime Database
   */
  async sincronizarAccesoEnLaNube(resultado: EscaneoResultado): Promise<boolean> {
    const payload = {
      dni: resultado.dni || 'N/A',
      nombre: resultado.nombreCompleto || 'Desconocido',
      perfil: resultado.perfil || 'SOCIO',
      status: resultado.status,
      casillero: resultado.casilleroAsignado || resultado.casilleroLiberado || 'Sin Casillero',
      fechaHora: new Date().toISOString(),
      origen: 'Angular SPA Brutal Fitness v1.0'
    };

    if (this.isConnected && this.db) {
      try {
        const registrosRef = ref(this.db, 'accesos_tiempo_real');
        const nuevoRegistro = push(registrosRef);
        await set(nuevoRegistro, payload);
        console.log('☁️ [Firebase Cloud] Evento de acceso sincronizado en la nube:', payload);
        return true;
      } catch (err) {
        console.error('❌ [Firebase Cloud] Error sincronizando en Firebase:', err);
        return false;
      }
    } else {
      // Almacenamiento local de respaldo (Fallback offline sync)
      const buffer = JSON.parse(localStorage.getItem('firebase_sync_buffer') || '[]');
      buffer.push(payload);
      localStorage.setItem('firebase_sync_buffer', JSON.stringify(buffer));
      console.log('💾 [Firebase Cloud Fallback] Evento registrado en buffer local:', payload);
      return true;
    }
  }

  getCloudStatus(): boolean {
    return this.isConnected;
  }
}
