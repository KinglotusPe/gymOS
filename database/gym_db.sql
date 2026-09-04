/* ==========================================================
   BRUTAL FITNESS - SISTEMA DE GESTIÓN INTEGRAL PARA GIMNASIOS
   Esquema de Base de Datos Expandido (20 Tablas Normalizadas)
   Motor: MySQL 8.x (InnoDB, UTF-8 MB4)
   ========================================================== */

CREATE DATABASE IF NOT EXISTS gym_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE gym_db;
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 1. Eliminar tablas previas (en orden inverso de dependencias)
DROP TABLE IF EXISTS alquiler_casillero;
DROP TABLE IF EXISTS casillero;
DROP TABLE IF EXISTS reserva_clase;
DROP TABLE IF EXISTS clase_grupal;
DROP TABLE IF EXISTS disciplina;
DROP TABLE IF EXISTS venta_detalle;
DROP TABLE IF EXISTS venta;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS categoria_producto;
DROP TABLE IF EXISTS rutina_detalle;
DROP TABLE IF EXISTS rutina;
DROP TABLE IF EXISTS ejercicio;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS seguimiento_fisico;
DROP TABLE IF EXISTS asistencia;
DROP TABLE IF EXISTS pago;
DROP TABLE IF EXISTS membresia;
DROP TABLE IF EXISTS plan_membresia;
DROP TABLE IF EXISTS entrenador;
DROP TABLE IF EXISTS cliente;

/* ==========================================================
   1. MÓDULO DE SOCIOS Y PERSONAL
   ========================================================== */
CREATE TABLE cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(15) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    edad INT NULL,
    telefono VARCHAR(20) NULL,
    correo VARCHAR(100) NULL,
    fecha_inscripcion DATE NULL,
    foto_url VARCHAR(500) NULL
) ENGINE=InnoDB;

CREATE TABLE entrenador (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(15) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    especialidad VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NULL,
    correo VARCHAR(100) NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
) ENGINE=InnoDB;

/* ==========================================================
   2. MÓDULO DE PLANES, MEMBRESÍAS Y COBRANZAS
   ========================================================== */
CREATE TABLE plan_membresia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    duracion_dias INT NOT NULL,
    precio_base DECIMAL(10, 2) NOT NULL,
    descripcion VARCHAR(255) NULL,
    acceso_total BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE membresia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    plan_id BIGINT NULL,
    tipo ENUM('DIARIO', 'SEMANAL', 'MENSUAL', 'TRIMESTRAL', 'ANUAL', 'PERSONALIZADA') NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    estado ENUM('ACTIVA', 'VENCIDA') NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT fk_membresia_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_membresia_plan FOREIGN KEY (plan_id) REFERENCES plan_membresia(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE pago (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    monto DECIMAL(10, 2) NOT NULL,
    fecha DATE NOT NULL,
    metodo_pago ENUM('EFECTIVO', 'TARJETA', 'YAPE', 'PLIN', 'TRANSFERENCIA') NOT NULL,
    proxima_fecha_pago DATE NULL,
    CONSTRAINT fk_pago_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE asistencia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    fecha_hora DATETIME(6) NOT NULL,
    CONSTRAINT fk_asistencia_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE seguimiento_fisico (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    entrenador_id BIGINT NULL,
    fecha_registro DATE NOT NULL,
    peso_kg DOUBLE NOT NULL,
    altura_cm DOUBLE NULL,
    porcentaje_grasa DOUBLE NULL,
    masa_muscular DOUBLE NULL,
    objetivo VARCHAR(100) NULL,
    observaciones VARCHAR(255) NULL,
    CONSTRAINT fk_seguimiento_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_seguimiento_entrenador FOREIGN KEY (entrenador_id) REFERENCES entrenador(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

/* ==========================================================
   3. MÓDULO DE SEGURIDAD Y ACCESO
   ========================================================== */
CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    cliente_id BIGINT NULL,
    entrenador_id BIGINT NULL,
    CONSTRAINT fk_usuario_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_usuario_entrenador FOREIGN KEY (entrenador_id) REFERENCES entrenador(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

/* ==========================================================
   4. MÓDULO DE ENTRENAMIENTO Y RUTINAS (CABECERA - DETALLE)
   ========================================================== */
CREATE TABLE ejercicio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL UNIQUE,
    grupo_muscular VARCHAR(50) NOT NULL,
    equipamiento VARCHAR(60) NULL,
    nivel VARCHAR(30) DEFAULT 'Intermedio',
    instrucciones TEXT NULL,
    imagen_url VARCHAR(500) NULL,
    gif_url VARCHAR(500) NULL,
    categoria VARCHAR(50) NULL
) ENGINE=InnoDB;

CREATE TABLE rutina (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    entrenador_id BIGINT NULL,
    nombre VARCHAR(100) NOT NULL,
    dia_semana VARCHAR(50) NOT NULL,
    ejercicios TEXT NOT NULL,
    nivel VARCHAR(50) NULL DEFAULT 'Intermedio',
    observaciones VARCHAR(255) NULL,
    CONSTRAINT fk_rutina_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_rutina_entrenador FOREIGN KEY (entrenador_id) REFERENCES entrenador(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE rutina_detalle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rutina_id BIGINT NOT NULL,
    ejercicio_id BIGINT NOT NULL,
    series INT NOT NULL DEFAULT 4,
    repeticiones INT NOT NULL DEFAULT 12,
    peso_sugerido_kg DOUBLE NULL,
    descanso_segundos INT DEFAULT 60,
    CONSTRAINT fk_rd_rutina FOREIGN KEY (rutina_id) REFERENCES rutina(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_rd_ejercicio FOREIGN KEY (ejercicio_id) REFERENCES ejercicio(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

/* ==========================================================
   5. MÓDULO DE TIENDA / SUPLEMENTOS / PUNTO DE VENTA (POS)
   ========================================================== */
CREATE TABLE categoria_producto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NULL
) ENGINE=InnoDB;

CREATE TABLE producto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    categoria_id BIGINT NOT NULL,
    codigo_barra VARCHAR(50) NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(255) NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL DEFAULT 0,
    precio_compra DECIMAL(10, 2) NOT NULL,
    precio_venta DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    stock_actual INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 5,
    imagen_url VARCHAR(500) NULL,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria_producto(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE venta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NULL,
    usuario_id BIGINT NULL,
    fecha_hora DATETIME NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    metodo_pago VARCHAR(30) NOT NULL DEFAULT 'EFECTIVO',
    comprobante_numero VARCHAR(30) NULL,
    CONSTRAINT fk_venta_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_venta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE venta_detalle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    venta_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_vd_venta FOREIGN KEY (venta_id) REFERENCES venta(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_vd_producto FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

/* ==========================================================
   6. MÓDULO DE CLASES GRUPALES Y RESERVAS
   ========================================================== */
CREATE TABLE disciplina (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NULL,
    intensidad VARCHAR(30) DEFAULT 'Media'
) ENGINE=InnoDB;

CREATE TABLE clase_grupal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    disciplina_id BIGINT NOT NULL,
    entrenador_id BIGINT NOT NULL,
    salon VARCHAR(50) NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    cupo_maximo INT NOT NULL DEFAULT 20,
    CONSTRAINT fk_clase_disciplina FOREIGN KEY (disciplina_id) REFERENCES disciplina(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_clase_entrenador FOREIGN KEY (entrenador_id) REFERENCES entrenador(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE reserva_clase (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    clase_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    fecha_reserva DATETIME NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'CONFIRMADA',
    CONSTRAINT fk_reserva_clase FOREIGN KEY (clase_id) REFERENCES clase_grupal(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uk_cliente_clase (clase_id, cliente_id)
) ENGINE=InnoDB;

/* ==========================================================
   7. MÓDULO DE CASILLEROS / LOCKERS
   ========================================================== */
CREATE TABLE casillero (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(20) NOT NULL UNIQUE,
    ubicacion VARCHAR(100) NOT NULL,
    piso INT NOT NULL DEFAULT 1,
    estado VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE',
    ocupado_por_nombre VARCHAR(100) NULL,
    ocupado_por_dni VARCHAR(20) NULL,
    ocupado_por_tipo VARCHAR(30) NULL,
    fecha_ocupacion DATETIME NULL
) ENGINE=InnoDB;

CREATE TABLE alquiler_casillero (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    casillero_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    costo DECIMAL(10, 2) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT fk_alquiler_casillero FOREIGN KEY (casillero_id) REFERENCES casillero(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_alquiler_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

/* ==========================================================
   DATOS DE PRUEBA INTEGRALES
   ========================================================== */

-- Clientes
INSERT INTO cliente (dni, nombres, apellidos, edad, telefono, correo, fecha_inscripcion) VALUES
('72345678', 'Juan Carlos', 'Perez Lopez', 28, '987654321', 'juan.perez@gmail.com', CURDATE()),
('45891234', 'Maria Elena', 'Gomez Quispe', 24, '912345678', 'maria.gomez@gmail.com', CURDATE()),
('10293847', 'Carlos Alberto', 'Mendoza Ramos', 32, '955443322', 'carlos.mendoza@gmail.com', CURDATE());

-- Entrenadores
INSERT INTO entrenador (dni, nombres, apellidos, especialidad, telefono, correo, estado) VALUES
('40112233', 'Marco Antonio', 'Vargas Torres', 'Musculación y Fuerza', '998877665', 'marco@brutalfitness.pe', 'ACTIVO'),
('41223344', 'Lucia', 'Fernandez Huaman', 'Crossfit / Funcional', '988776655', 'lucia@brutalfitness.pe', 'ACTIVO');

-- Planes
INSERT INTO plan_membresia (nombre, duracion_dias, precio_base, descripcion, acceso_total) VALUES
('Plan Estudiantil', 30, 80.00, 'Acceso en horario diurno de 8am a 4pm', FALSE),
('Plan Mensual Libre', 30, 100.00, 'Acceso ilimitado a sala de pesas', TRUE),
('Plan Trimestral VIP', 90, 270.00, 'Acceso a pesas + 3 clases grupales/semana', TRUE),
('Plan Anual Brutal', 365, 950.00, 'Acceso ilimitado VIP todo el año + locker gratis', TRUE);

-- Membresias
INSERT INTO membresia (cliente_id, plan_id, tipo, fecha_inicio, fecha_vencimiento, estado) VALUES
(1, 2, 'MENSUAL', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 MONTH), 'ACTIVA'),
(2, 3, 'TRIMESTRAL', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 MONTH), 'ACTIVA'),
(3, 1, 'SEMANAL', DATE_SUB(CURDATE(), INTERVAL 2 WEEK), DATE_SUB(CURDATE(), INTERVAL 1 WEEK), 'VENCIDA');

-- Pagos
INSERT INTO pago (cliente_id, monto, fecha, metodo_pago, proxima_fecha_pago) VALUES
(1, 100.00, CURDATE(), 'YAPE', DATE_ADD(CURDATE(), INTERVAL 1 MONTH)),
(2, 270.00, CURDATE(), 'TARJETA', DATE_ADD(CURDATE(), INTERVAL 3 MONTH)),
(3, 35.00, DATE_SUB(CURDATE(), INTERVAL 2 WEEK), 'EFECTIVO', DATE_SUB(CURDATE(), INTERVAL 1 WEEK));

-- Asistencias
INSERT INTO asistencia (cliente_id, fecha_hora) VALUES
(1, NOW()),
(2, NOW());

-- Evaluaciones Fisicas
INSERT INTO seguimiento_fisico (cliente_id, entrenador_id, fecha_registro, peso_kg, altura_cm, porcentaje_grasa, masa_muscular, objetivo, observaciones) VALUES
(1, 1, CURDATE(), 76.5, 175.0, 18.0, 35.5, 'Hipertrofia / Ganancia Muscular', 'Progreso óptimo, buena respuesta a la carga.'),
(2, 2, CURDATE(), 58.0, 162.0, 21.0, 24.0, 'Definición / Pérdida de Grasa', 'Excelente resistencia en circuito funcional.');

-- Usuarios
INSERT INTO usuario (username, password, nombre, rol, activo, cliente_id, entrenador_id) VALUES
('admin', '$2a$10$eAccYoNO32CpArGPl9OxP.4jGzT7mBvHcqIe7tJ9P7KkV6OqN4qC6', 'César Luza (Administrador General)', 'ROLE_ADMIN', TRUE, NULL, NULL),
('recepcion', '$2a$10$eAccYoNO32CpArGPl9OxP.4jGzT7mBvHcqIe7tJ9P7KkV6OqN4qC6', 'Staff Recepción BRUTAL', 'ROLE_RECEPCIONISTA', TRUE, NULL, NULL),
('entrenador', '$2a$10$eAccYoNO32CpArGPl9OxP.4jGzT7mBvHcqIe7tJ9P7KkV6OqN4qC6', 'Prof. Marco Antonio Vargas', 'ROLE_ENTRENADOR', TRUE, NULL, 1),
('72345678', '$2a$10$eAccYoNO32CpArGPl9OxP.4jGzT7mBvHcqIe7tJ9P7KkV6OqN4qC6', 'Juan Carlos Perez Lopez', 'ROLE_CLIENTE', TRUE, 1, NULL);

-- Banco de Ejercicios con Animaciones GIF Reales
INSERT INTO ejercicio (id, nombre, grupo_muscular, equipamiento, nivel, instrucciones, imagen_url, gif_url, categoria) VALUES
(1, 'Press de Banca en Máquina Smith', 'Pecho', 'Máquina Smith', 'Intermedio', '1. Coloca la banca plana en el centro de la máquina Smith.\n2. Sujeta la barra con agarre ligeramente ancho.\n3. Desciende la barra controlando el peso hacia el pecho.\n4. Empuja contrayendo los pectorales.', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/pectorals/smith-wide-grip-bench-press.gif', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/pectorals/smith-wide-grip-bench-press.gif', 'Fuerza'),
(2, 'Fondos en Paralelas para Pecho', 'Pecho', 'Barras Paralelas', 'Intermedio', '1. Inclina el torso hacia adelante.\n2. Flexiona los codos a 90°.\n3. Empuja con fuerza con el pecho.', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/pectorals/weighted-straight-bar-dip.gif', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/pectorals/weighted-straight-bar-dip.gif', 'Fuerza'),
(3, 'Flexiones con Manos Abiertas', 'Pecho', 'Peso Corporal', 'Principiante', '1. Separa las manos más allá del ancho de hombros.\n2. Baja el pecho hasta rozar el suelo.\n3. Empuja manteniendo el core activo.', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/pectorals/wide-hand-push-up.gif', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/pectorals/wide-hand-push-up.gif', 'Resistencia'),
(4, 'Remo Inclinado en Máquina Smith', 'Espalda', 'Máquina Smith', 'Intermedio', '1. Inclina el torso a 45° manteniendo la espalda recta.\n2. Jala la barra hacia el abdomen bajo.\n3. Aprieta las escápulas.', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/upper-back/smith-bent-over-row.gif', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/upper-back/smith-bent-over-row.gif', 'Hipertrofia'),
(5, 'Remo Unilateral a una Mano', 'Espalda', 'Máquina Smith', 'Intermedio', '1. Conduce el codo hacia la cadera.\n2. Concéntrate en el dorsal ancho.\n3. Controla el descenso.', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/upper-back/smith-one-arm-row.gif', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/upper-back/smith-one-arm-row.gif', 'Aislamiento'),
(6, 'Remo Invertido en Suspensión (TRX)', 'Espalda', 'Correas TRX', 'Principiante', '1. Sujeta los manerales inclinándote hacia atrás.\n2. Tira del pecho hacia las manos.\n3. Baja despacio.', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/upper-back/suspended-row.gif', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/upper-back/suspended-row.gif', 'Funcional'),
(7, 'Sentadilla Búlgara con Banda', 'Piernas', 'Banda / Mancuerna', 'Intermedio', '1. Apoya el empeine en un banco trasero.\n2. Baja la rodilla delantera a 90°.\n3. Empuja con el talón.', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/quads/band-one-arm-single-leg-split-squat.gif', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/quads/band-one-arm-single-leg-split-squat.gif', 'Hipertrofia'),
(8, 'Saltos Pliométricos de Potencia', 'Piernas', 'Peso Corporal', 'Intermedio', '1. Carga energía elástica en los cuádriceps.\n2. Salta con máxima potencia.\n3. Aterriza en flexión suave.', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/quads/backward-jump.gif', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/quads/backward-jump.gif', 'Pliometría'),
(9, 'Press Militar de Hombros', 'Hombros', 'Banda / Mancuernas', 'Principiante', '1. Empuja la carga por encima de la cabeza.\n2. Extiende brazos sin arquear la espalda.\n3. Desciende con control.', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/delts/band-shoulder-press.gif', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/delts/band-shoulder-press.gif', 'Fuerza'),
(10, 'Elevaciones Laterales para Deltoides', 'Hombros', 'Banda / Mancuernas', 'Principiante', '1. Eleva los brazos hasta la altura de los hombros.\n2. Mantén 1 segundo en la contracción.\n3. Baja despacio.', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/delts/band-front-lateral-raise.gif', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/delts/band-front-lateral-raise.gif', 'Aislamiento'),
(11, 'Pájaros para Hombro Posterior', 'Hombros', 'Banda / Mancuernas', 'Intermedio', '1. Inclina el torso al frente.\n2. Abre los brazos hacia los costados apretando el deltoides posterior.\n3. Regresa suavemente.', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/delts/band-reverse-fly.gif', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/delts/band-reverse-fly.gif', 'Aislamiento'),
(12, 'Curl de Bíceps Alterno con Barra', 'Brazos', 'Barra / Mancuerna', 'Principiante', '1. Flexiona el codo levantando la carga hacia el pecho.\n2. Aprieta el bíceps arriba.\n3. Baja despacio.', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/biceps/barbell-alternate-biceps-curl.gif', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/biceps/barbell-alternate-biceps-curl.gif', 'Hipertrofia'),
(13, 'Curl Concentrado de Bíceps', 'Brazos', 'Mancuerna / Banda', 'Principiante', '1. Apoya el codo en la cara interna del muslo.\n2. Flexiona aislando el bíceps.\n3. Controla la bajada.', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/biceps/band-concentration-curl.gif', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/biceps/band-concentration-curl.gif', 'Aislamiento'),
(14, 'Bicicleta Abdominal (Air Bike Crunch)', 'Core', 'Colchoneta', 'Principiante', '1. Alterna codo hacia rodilla contraria.\n2. Mantén tensión constante en los oblicuos.\n3. Movimiento fluido.', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/abs/air-bike.gif', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/abs/air-bike.gif', 'Definición'),
(15, 'Abdominales Sit-Up Clásicos', 'Core', 'Colchoneta', 'Principiante', '1. Eleva el torso flexionando el abdomen.\n2. Llega hasta quedar sentado.\n3. Desciende vértebra por vértebra.', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/abs/3-4-sit-up.gif', 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/abs/3-4-sit-up.gif', 'Fuerza');

-- Rutinas
INSERT INTO rutina (cliente_id, entrenador_id, nombre, dia_semana, ejercicios, nivel, observaciones) VALUES
(1, 1, 'Fuerza e Hipertrofia A', 'Lunes - Pecho y Tríceps', '1. Press Banca: 4x10\n2. Press Inclinado: 3x12\n3. Extensiones Tríceps: 4x15', 'Intermedio', 'Descanso 90s entre series pesadas.'),
(1, 1, 'Fuerza e Hipertrofia B', 'Miércoles - Espalda y Bíceps', '1. Jalón al Pecho: 4x12\n2. Remo Barra: 4x10\n3. Curl Bíceps: 3x12', 'Intermedio', 'Cuidar técnica y retracción escapular.');

INSERT INTO rutina_detalle (rutina_id, ejercicio_id, series, repeticiones, peso_sugerido_kg, descanso_segundos) VALUES
(1, 1, 4, 10, 60.0, 90),
(1, 2, 3, 12, 0.0, 60),
(1, 3, 3, 15, 0.0, 45),
(2, 4, 4, 10, 50.0, 90),
(2, 5, 3, 12, 25.0, 60),
(2, 12, 3, 12, 20.0, 45),
(2, 13, 3, 15, 12.5, 45);

-- Tienda / Categorías y Productos
INSERT INTO categoria_producto (id, nombre, descripcion) VALUES
(1, 'Proteínas & Suplementos', 'Proteínas aisladas Whey, creatinas y aminoácidos'),
(2, 'Bebidas & Hidratación', 'Bebidas rehidratantes, energéticas y agua mineral'),
(3, 'Accesorios & Candados', 'Candados de casillero, shakers, toallas y straps'),
(4, 'Snacks & Barras', 'Barras hiperproteicas y frutos secos');

INSERT INTO producto (id, categoria_id, nombre, descripcion, precio_unitario, stock, imagen_url, codigo_barra, precio_compra, precio_venta, stock_actual, stock_minimo) VALUES
(1, 1, 'Proteína 100% Whey Gold Standard 2lb (Vainilla)', 'Proteína pura aislada de suero de leche para desarrollo muscular magro', 185.00, 15, 'https://images.unsplash.com/photo-1579722820308-d74e571900a9?w=400&auto=format&fit=crop&q=80', '775123456001', 130.00, 185.00, 15, 3),
(2, 1, 'Creatina Monohidratada Creapure 300g', 'Creatina 100% micronizada de máxima absorción y fuerza explosiva', 110.00, 20, 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d?w=400&auto=format&fit=crop&q=80', '775123456002', 75.00, 110.00, 20, 5),
(3, 1, 'Pre-Entreno C4 Explosive Energy 30 Serv', 'Fórmula de óxido nítrico, cafeína y beta-alanina para alta intensidad', 135.00, 12, 'https://images.unsplash.com/photo-1584017911766-d451b3d0e843?w=400&auto=format&fit=crop&q=80', '775123456003', 90.00, 135.00, 12, 3),
(4, 2, 'Bebida Rehidratante Gatorade 500ml (Mora)', 'Reposición rápida de electrolitos, sodio y carbohidratos en entreno', 4.50, 45, 'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400&auto=format&fit=crop&q=80', '775123456004', 2.80, 4.50, 45, 10),
(5, 2, 'Agua Mineral San Mateo 600ml (Sin Gas)', 'Agua purificada de manantial de montaña', 2.50, 60, 'https://images.unsplash.com/photo-1548839140-29a749e1bc4e?w=400&auto=format&fit=crop&q=80', '775123456005', 1.20, 2.50, 60, 15),
(6, 2, 'Bebida Energética Monster Energy 473ml', 'Bebida carbonatada con taurina, ginseng y vitaminas del complejo B', 9.00, 30, 'https://images.unsplash.com/photo-1622543925917-763c34d1a86e?w=400&auto=format&fit=crop&q=80', '775123456006', 5.50, 9.00, 30, 8),
(7, 3, 'Candado de Seguridad Combinación 4 Dígitos', 'Candado reforzado de aleación metálica antirrobo para casilleros y lockers', 22.00, 25, 'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=400&auto=format&fit=crop&q=80', '775123456007', 12.00, 22.00, 25, 5),
(8, 3, 'Shaker Botella Mezcladora BRUTAL 700ml', 'Vaso mezclador con rejilla batidora y compartimento para pastillas y polvo', 18.00, 35, 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=400&auto=format&fit=crop&q=80', '775123456008', 9.50, 18.00, 35, 5),
(9, 3, 'Toalla de Microfibra de Secado Rápido', 'Toalla ultra absorbente antibacteriana para máquinas y sudor', 15.00, 20, 'https://images.unsplash.com/photo-1616046229478-9901c5536a45?w=400&auto=format&fit=crop&q=80', '775123456009', 8.00, 15.00, 20, 5),
(10, 4, 'Barra de Proteína Quest Bar 60g (Chocolate)', '21g de proteína aislada con solo 1g de azúcar y alta fibra', 12.00, 40, 'https://images.unsplash.com/photo-1622484216850-2580536c1e55?w=400&auto=format&fit=crop&q=80', '775123456010', 7.50, 12.00, 40, 10);

-- Ventas POS
INSERT INTO venta (id, cliente_id, usuario_id, fecha_hora, total, metodo_pago, comprobante_numero) VALUES
(1, 1, 2, NOW(), 197.00, 'YAPE', 'TKT-000001');

INSERT INTO venta_detalle (venta_id, producto_id, cantidad, precio_unitario, subtotal) VALUES
(1, 1, 1, 185.00, 185.00),
(1, 10, 1, 12.00, 12.00);

-- Disciplinas y Clases Grupales
INSERT INTO disciplina (nombre, descripcion, intensidad) VALUES
('Spinning Indoor', 'Entrenamiento cardiovascular de alta intensidad en bicicleta fija', 'Alta'),
('Crossfit & WOD', 'Entrenamiento funcional de alta intensidad por intervalos', 'Muy Alta'),
('Boxeo Funcional', 'Técnicas de golpeo, resistencia y coordinación', 'Alta'),
('Yoga & Flexibilidad', 'Posturas, respiración y elongación muscular', 'Baja');

INSERT INTO clase_grupal (disciplina_id, entrenador_id, salon, fecha, hora_inicio, hora_fin, cupo_maximo) VALUES
(1, 2, 'Sala 1 - Ciclismo', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '07:00:00', '08:00:00', 20),
(2, 2, 'Box Principal Cross', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '18:30:00', '19:30:00', 15),
(3, 1, 'Sala de Contacto', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '19:00:00', '20:00:00', 12);

-- Reservas de Clases
INSERT INTO reserva_clase (clase_id, cliente_id, fecha_reserva, estado) VALUES
(1, 1, NOW(), 'CONFIRMADA'),
(1, 2, NOW(), 'CONFIRMADA');

-- Casilleros / Lockers
INSERT INTO casillero (numero, ubicacion, piso, estado, ocupado_por_nombre, ocupado_por_dni, ocupado_por_tipo, fecha_ocupacion) VALUES
('L-01', 'Vestidor Varones - Piso 1', 1, 'OCUPADO', 'Juan Carlos Pérez', '72345678', 'SOCIO', NOW()),
('L-02', 'Vestidor Varones - Piso 1', 1, 'DISPONIBLE', NULL, NULL, NULL, NULL),
('L-03', 'Vestidor Varones - Piso 1', 1, 'DISPONIBLE', NULL, NULL, NULL, NULL),
('L-04', 'Vestidor Damas - Piso 1', 1, 'DISPONIBLE', NULL, NULL, NULL, NULL),
('L-05', 'Vestidor Damas - Piso 1', 1, 'OCUPADO', 'María Elena Gómez', '73456789', 'SOCIO', NOW()),
('L-06', 'Vestidor Damas - Piso 1', 1, 'DISPONIBLE', NULL, NULL, NULL, NULL),
('L-07', 'Zona Musculación & Cardio - Piso 2', 2, 'DISPONIBLE', NULL, NULL, NULL, NULL),
('L-08', 'Zona Musculación & Cardio - Piso 2', 2, 'DISPONIBLE', NULL, NULL, NULL, NULL),
('L-09', 'Zona Musculación & Cardio - Piso 2', 2, 'DISPONIBLE', NULL, NULL, NULL, NULL),
('L-10', 'Zona Musculación & Cardio - Piso 2', 2, 'DISPONIBLE', NULL, NULL, NULL, NULL),
('L-11', 'Zona Musculación & Cardio - Piso 2', 2, 'DISPONIBLE', NULL, NULL, NULL, NULL),
('L-12', 'Zona Musculación & Cardio - Piso 2', 2, 'DISPONIBLE', NULL, NULL, NULL, NULL),
('L-13', 'Área Funcional & Spinning - Piso 3', 3, 'DISPONIBLE', NULL, NULL, NULL, NULL),
('L-14', 'Área Funcional & Spinning - Piso 3', 3, 'DISPONIBLE', NULL, NULL, NULL, NULL),
('L-15', 'Área Funcional & Spinning - Piso 3', 3, 'DISPONIBLE', NULL, NULL, NULL, NULL),
('L-16', 'Área Funcional & Spinning - Piso 3', 3, 'DISPONIBLE', NULL, NULL, NULL, NULL),
('L-17', 'Área Funcional & Spinning - Piso 3', 3, 'DISPONIBLE', NULL, NULL, NULL, NULL),
('L-18', 'Área Funcional & Spinning - Piso 3', 3, 'DISPONIBLE', NULL, NULL, NULL, NULL);

INSERT INTO alquiler_casillero (casillero_id, cliente_id, fecha_inicio, fecha_fin, costo, estado) VALUES
(1, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 MONTH), 30.00, 'ACTIVO');
