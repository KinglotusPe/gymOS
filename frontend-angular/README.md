# 🚀 Brutal Fitness - Frontend Angular 17 SPA

## 🎓 Información Académica & Autoría
* **Institución:** Escuela de Educación Superior Tecnológica La Pontificia
* **Carrera:** Desarrollo de Sistemas de Información / Computación e Informática
* **Semestre:** 5to Semestre
* **Curso:** Programación de Aplicaciones Web
* **Autor / Estudiante:** César Daniel Luza Cordova
* **GitHub:** [@KinglotusPe](https://github.com/KinglotusPe)
* **Correo:** luzacordovacesar@gmail.com

---

## 📋 Cumplimiento del Sílabo - Unidad 4: Framework Angular, TypeScript y Servicios en la Nube con Firebase

Este módulo frontend ha sido desarrollado cumpliendo de forma estricta los indicadores y criterios de evaluación del sílabo oficial:

1. **Lenguaje TypeScript Moderno:**
   - Tipado estático riguroso en modelos e interfaces (`Cliente`, `PaginaClientes`, `Asistencia`, `EscaneoResultado`, `DashboardStats`, `Producto`).
   - Uso de clases de servicio inyectables (`@Injectable({ providedIn: 'root' })`).
   - Programación reactiva basada en Observables de RxJS (`Observable`, `catchError`, `tap`, `map`).

2. **Arquitectura Angular 17 Standalone:**
   - Implementación moderna sin `NgModule` monolítico, utilizando componentes autónomos (`standalone: true`).
   - Enrutamiento SPA dinámico (`app.routes.ts`) con `provideRouter` y `provideHttpClient`.
   - Flujo bidireccional mediante `[(ngModel)]` de `FormsModule` y directivas estructurales (`*ngIf`, `*ngFor`, `[ngClass]`).

3. **Módulos & Componentes Implementados:**
   - **`NavbarComponent`:** Barra de navegación superior con reloj en vivo sincronizado por segundo, estado del servidor y accesos directos.
   - **`DashboardComponent`:** Panel de analíticas en tiempo real (socios activos, asistencias de hoy, lockers libres, entrenadores en turno) con acceso directo a la descarga de reportes oficiales JasperReports en PDF.
   - **`ClientesComponent`:** Módulo de gestión de socios con **Paginación del lado del servidor** (`Pageable`), ordenamiento interactivo por columnas (`Sort`), búsqueda en vivo y modal para registro de nuevos socios con membresías.
   - **`ControlAccesoComponent`:** Terminal de torniquete inteligente para lectura de código de barras de DNI, asignación y liberación automática de casilleros, sintetizador de audio Web Audio API con frecuencias diferenciadas (acceso permitido, salida liberada, advertencia de vencimiento, error).

4. **Conexión a Servicios en la Nube con Firebase:**
   - Servicio `FirebaseCloudService` configurado con credenciales de Firebase Realtime Database.
   - Envío de telemetría y sincronización en tiempo real de marcajes de torniquete y eventos de asistencia a la nube (`https://gym-sistema-pontificia-default-rtdb.firebaseio.com`).

---

## 🛠️ Requisitos Previos e Instalación

### 1. Requisitos
- **Node.js**: v18.x o superior
- **NPM**: v9.x o superior
- **Angular CLI** (opcional para desarrollo global): `npm install -g @angular/cli`

### 2. Instalación de Dependencias
Abra un terminal en la carpeta `frontend-angular/` y ejecute:
```bash
npm install
```

### 3. Ejecución en Modo Desarrollo
Para levantar el servidor de desarrollo local con recarga en caliente:
```bash
npm start
# O alternativamente:
# ng serve --port 4200
```

Navegue en su navegador web a:
👉 `http://localhost:4200`

---

## 🔗 Integración con el Backend Spring Boot
La aplicación Angular se conecta de manera transparente con el backend RESTful Spring Boot en `http://localhost:8080`:

| Endpoint Spring Boot | Método | Componente Angular | Descripción |
| :--- | :---: | :--- | :--- |
| `/api/clientes/paginado` | `GET` | `ClientesComponent` | Listado paginado y ordenado (`Pageable`, `Sort`) |
| `/api/clientes` | `POST` | `ClientesComponent` | Alta de nuevo socio con plan |
| `/api/asistencias/escanear` | `POST` | `ControlAccesoComponent` | Escaneo de código DNI, control de torniquete y lockers |
| `/api/asistencias/hoy` | `GET` | `ControlAccesoComponent` | Historial de asistencias de la jornada |
| `/api/dashboard/stats` | `GET` | `DashboardComponent` | Métricas generales y contadores |
| `/reportes/socios` | `GET` | `DashboardComponent` | Descarga de PDF de Padrón General JasperReports |
| `/reportes/caja` | `GET` | `DashboardComponent` | Descarga de PDF de Cierre y Arqueo JasperReports |
| `/reportes/boleta/{id}` | `GET` | `DashboardComponent` | Descarga de PDF de Boleta de Pago JasperReports |

---

## 🎨 Estética & Diseño
* **Paleta Cyberpunk / Slate Dark:** Modo oscuro de alto contraste con tonos Deep Navy (`#0f172a`), Cyan neón (`#06b6d4`), Verde esmeralda (`#10b981`) y Púrpura eléctrico (`#a855f7`).
* **Glassmorphism:** Tarjetas y barras translúcidas con efecto `backdrop-filter: blur(16px)` y sombras profundas.
* **Micro-interacciones:** Indicadores de pulso CSS, transiciones de foco suaves y badges de estado dinámicos.
