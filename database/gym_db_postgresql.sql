/* ==========================================================
   BRUTAL FITNESS - SISTEMA DE GESTIÓN INTEGRAL PARA GIMNASIOS
   Esquema de Base de Datos Expandido (20 Tablas Normalizadas)
   Motor: PostgreSQL 14+
   ========================================================== */

-- 1. Eliminar tablas previas (en orden inverso)
DROP TABLE IF EXISTS alquiler_casillero CASCADE;
DROP TABLE IF EXISTS casillero CASCADE;
DROP TABLE IF EXISTS reserva_clase CASCADE;
DROP TABLE IF EXISTS clase_grupal CASCADE;
DROP TABLE IF EXISTS disciplina CASCADE;
DROP TABLE IF EXISTS venta_detalle CASCADE;
DROP TABLE IF EXISTS venta CASCADE;
DROP TABLE IF EXISTS producto CASCADE;
DROP TABLE IF EXISTS categoria_producto CASCADE;
DROP TABLE IF EXISTS rutina_detalle CASCADE;
DROP TABLE IF EXISTS rutina CASCADE;
DROP TABLE IF EXISTS ejercicio CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;
DROP TABLE IF EXISTS seguimiento_fisico CASCADE;
DROP TABLE IF EXISTS asistencia CASCADE;
DROP TABLE IF EXISTS pago CASCADE;
DROP TABLE IF EXISTS membresia CASCADE;
DROP TABLE IF EXISTS plan_membresia CASCADE;
DROP TABLE IF EXISTS entrenador CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;

-- 2. Eliminar tipos enum si existen
DROP TYPE IF EXISTS tipo_membresia_enum CASCADE;
DROP TYPE IF EXISTS estado_membresia_enum CASCADE;
DROP TYPE IF EXISTS metodo_pago_enum CASCADE;

-- 3. Crear tipos ENUM
CREATE TYPE tipo_membresia_enum AS ENUM ('DIARIO', 'SEMANAL', 'MENSUAL', 'TRIMESTRAL', 'ANUAL', 'PERSONALIZADA');
CREATE TYPE estado_membresia_enum AS ENUM ('ACTIVA', 'VENCIDA');
CREATE TYPE metodo_pago_enum AS ENUM ('EFECTIVO', 'TARJETA', 'YAPE', 'PLIN', 'TRANSFERENCIA');

/* ==========================================================
   1. MÓDULO DE SOCIOS Y PERSONAL
   ========================================================== */
CREATE TABLE cliente (
    id BIGSERIAL PRIMARY KEY,
    dni VARCHAR(15) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    edad INTEGER NULL,
    telefono VARCHAR(20) NULL,
    correo VARCHAR(100) NULL,
    fecha_inscripcion DATE NULL
);

CREATE TABLE entrenador (
    id BIGSERIAL PRIMARY KEY,
    dni VARCHAR(15) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    especialidad VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NULL,
    correo VARCHAR(100) NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
);

/* ==========================================================
   2. MÓDULO DE PLANES, MEMBRESÍAS Y COBRANZAS
   ========================================================== */
CREATE TABLE plan_membresia (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    duracion_dias INTEGER NOT NULL,
    precio_base NUMERIC(10, 2) NOT NULL,
    descripcion VARCHAR(255) NULL,
    acceso_total BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE membresia (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    plan_id BIGINT NULL,
    tipo tipo_membresia_enum NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    estado estado_membresia_enum NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT fk_membresia_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_membresia_plan FOREIGN KEY (plan_id) REFERENCES plan_membresia(id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE pago (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    monto NUMERIC(10, 2) NOT NULL,
    fecha DATE NOT NULL,
    metodo_pago metodo_pago_enum NOT NULL,
    proxima_fecha_pago DATE NULL,
    CONSTRAINT fk_pago_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE asistencia (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    fecha_hora TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_asistencia_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE seguimiento_fisico (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    entrenador_id BIGINT NULL,
    fecha_registro DATE NOT NULL,
    peso_kg DOUBLE PRECISION NOT NULL,
    altura_cm DOUBLE PRECISION NULL,
    porcentaje_grasa DOUBLE PRECISION NULL,
    masa_muscular DOUBLE PRECISION NULL,
    objetivo VARCHAR(100) NULL,
    observaciones VARCHAR(255) NULL,
    CONSTRAINT fk_seguimiento_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_seguimiento_entrenador FOREIGN KEY (entrenador_id) REFERENCES entrenador(id) ON DELETE SET NULL ON UPDATE CASCADE
);

/* ==========================================================
   3. MÓDULO DE SEGURIDAD Y ACCESO
   ========================================================== */
CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    cliente_id BIGINT NULL,
    entrenador_id BIGINT NULL,
    CONSTRAINT fk_usuario_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_usuario_entrenador FOREIGN KEY (entrenador_id) REFERENCES entrenador(id) ON DELETE SET NULL ON UPDATE CASCADE
);

/* ==========================================================
   4. MÓDULO DE ENTRENAMIENTO Y RUTINAS (CABECERA - DETALLE)
   ========================================================== */
CREATE TABLE ejercicio (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    grupo_muscular VARCHAR(50) NOT NULL,
    descripcion TEXT NULL,
    nivel_dificultad VARCHAR(30) DEFAULT 'Intermedio'
);

CREATE TABLE rutina (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    entrenador_id BIGINT NULL,
    nombre VARCHAR(100) NOT NULL,
    dia_semana VARCHAR(50) NOT NULL,
    ejercicios TEXT NOT NULL,
    nivel VARCHAR(50) NULL DEFAULT 'Intermedio',
    observaciones VARCHAR(255) NULL,
    CONSTRAINT fk_rutina_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_rutina_entrenador FOREIGN KEY (entrenador_id) REFERENCES entrenador(id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE rutina_detalle (
    id BIGSERIAL PRIMARY KEY,
    rutina_id BIGINT NOT NULL,
    ejercicio_id BIGINT NOT NULL,
    series INTEGER NOT NULL DEFAULT 4,
    repeticiones INTEGER NOT NULL DEFAULT 12,
    peso_sugerido_kg DOUBLE PRECISION NULL,
    descanso_segundos INTEGER DEFAULT 60,
    CONSTRAINT fk_rd_rutina FOREIGN KEY (rutina_id) REFERENCES rutina(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_rd_ejercicio FOREIGN KEY (ejercicio_id) REFERENCES ejercicio(id) ON DELETE CASCADE ON UPDATE CASCADE
);

/* ==========================================================
   5. MÓDULO DE TIENDA / SUPLEMENTOS / PUNTO DE VENTA (POS)
   ========================================================== */
CREATE TABLE categoria_producto (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NULL
);

CREATE TABLE producto (
    id BIGSERIAL PRIMARY KEY,
    categoria_id BIGINT NOT NULL,
    codigo_barra VARCHAR(50) NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(255) NULL,
    precio_unitario NUMERIC(10, 2) NOT NULL DEFAULT 0,
    precio_compra NUMERIC(10, 2) NOT NULL,
    precio_venta NUMERIC(10, 2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    stock_actual INTEGER NOT NULL DEFAULT 0,
    stock_minimo INTEGER NOT NULL DEFAULT 5,
    imagen_url VARCHAR(500) NULL,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria_producto(id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE venta (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NULL,
    usuario_id BIGINT NULL,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total NUMERIC(10, 2) NOT NULL,
    metodo_pago VARCHAR(30) NOT NULL DEFAULT 'EFECTIVO',
    comprobante_numero VARCHAR(30) NULL,
    CONSTRAINT fk_venta_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_venta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE venta_detalle (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unitario NUMERIC(10, 2) NOT NULL,
    subtotal NUMERIC(10, 2) NOT NULL,
    CONSTRAINT fk_vd_venta FOREIGN KEY (venta_id) REFERENCES venta(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_vd_producto FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE RESTRICT ON UPDATE CASCADE
);

/* ==========================================================
   6. MÓDULO DE CLASES GRUPALES Y RESERVAS
   ========================================================== */
CREATE TABLE disciplina (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NULL,
    intensidad VARCHAR(30) DEFAULT 'Media'
);

CREATE TABLE clase_grupal (
    id BIGSERIAL PRIMARY KEY,
    disciplina_id BIGINT NOT NULL,
    entrenador_id BIGINT NOT NULL,
    salon VARCHAR(50) NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    cupo_maximo INTEGER NOT NULL DEFAULT 20,
    CONSTRAINT fk_clase_disciplina FOREIGN KEY (disciplina_id) REFERENCES disciplina(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_clase_entrenador FOREIGN KEY (entrenador_id) REFERENCES entrenador(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE reserva_clase (
    id BIGSERIAL PRIMARY KEY,
    clase_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    fecha_reserva TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'CONFIRMADA',
    CONSTRAINT fk_reserva_clase FOREIGN KEY (clase_id) REFERENCES clase_grupal(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (clase_id, cliente_id)
);

/* ==========================================================
   7. MÓDULO DE CASILLEROS / LOCKERS
   ========================================================== */
CREATE TABLE casillero (
    id BIGSERIAL PRIMARY KEY,
    numero VARCHAR(20) NOT NULL UNIQUE,
    ubicacion VARCHAR(100) NOT NULL,
    piso INTEGER NOT NULL DEFAULT 1,
    estado VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE',
    ocupado_por_nombre VARCHAR(100) NULL,
    ocupado_por_dni VARCHAR(20) NULL,
    ocupado_por_tipo VARCHAR(30) NULL,
    fecha_ocupacion TIMESTAMP NULL
);

CREATE TABLE alquiler_casillero (
    id BIGSERIAL PRIMARY KEY,
    casillero_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    costo NUMERIC(10, 2) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT fk_alquiler_casillero FOREIGN KEY (casillero_id) REFERENCES casillero(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_alquiler_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE ON UPDATE CASCADE
);

/* ==========================================================
   DATOS DE PRUEBA INTEGRALES
   ========================================================== */
INSERT INTO cliente (dni, nombres, apellidos, edad, telefono, correo, fecha_inscripcion) VALUES
('72345678', 'Juan Carlos', 'Perez Lopez', 28, '987654321', 'juan.perez@gmail.com', CURRENT_DATE),
('45891234', 'Maria Elena', 'Gomez Quispe', 24, '912345678', 'maria.gomez@gmail.com', CURRENT_DATE),
('10293847', 'Carlos Alberto', 'Mendoza Ramos', 32, '955443322', 'carlos.mendoza@gmail.com', CURRENT_DATE);

INSERT INTO entrenador (dni, nombres, apellidos, especialidad, telefono, correo, estado) VALUES
('40112233', 'Marco Antonio', 'Vargas Torres', 'Musculación y Fuerza', '998877665', 'marco@brutalfitness.pe', 'ACTIVO'),
('41223344', 'Lucia', 'Fernandez Huaman', 'Crossfit / Funcional', '988776655', 'lucia@brutalfitness.pe', 'ACTIVO');

INSERT INTO plan_membresia (nombre, duracion_dias, precio_base, descripcion, acceso_total) VALUES
('Plan Estudiantil', 30, 80.00, 'Acceso en horario diurno de 8am a 4pm', FALSE),
('Plan Mensual Libre', 30, 100.00, 'Acceso ilimitado a sala de pesas', TRUE),
('Plan Trimestral VIP', 90, 270.00, 'Acceso a pesas + 3 clases grupales/semana', TRUE),
('Plan Anual Brutal', 365, 950.00, 'Acceso ilimitado VIP todo el año + locker gratis', TRUE);

INSERT INTO membresia (cliente_id, plan_id, tipo, fecha_inicio, fecha_vencimiento, estado) VALUES
(1, 2, 'MENSUAL', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 month', 'ACTIVA'),
(2, 3, 'TRIMESTRAL', CURRENT_DATE, CURRENT_DATE + INTERVAL '3 months', 'ACTIVA'),
(3, 1, 'SEMANAL', CURRENT_DATE - INTERVAL '2 weeks', CURRENT_DATE - INTERVAL '1 week', 'VENCIDA');

INSERT INTO pago (cliente_id, monto, fecha, metodo_pago, proxima_fecha_pago) VALUES
(1, 100.00, CURRENT_DATE, 'YAPE', CURRENT_DATE + INTERVAL '1 month'),
(2, 270.00, CURRENT_DATE, 'TARJETA', CURRENT_DATE + INTERVAL '3 months'),
(3, 35.00, CURRENT_DATE - INTERVAL '2 weeks', 'EFECTIVO', CURRENT_DATE - INTERVAL '1 week');

INSERT INTO asistencia (cliente_id, fecha_hora) VALUES
(1, CURRENT_TIMESTAMP),
(2, CURRENT_TIMESTAMP);

INSERT INTO seguimiento_fisico (cliente_id, entrenador_id, fecha_registro, peso_kg, altura_cm, porcentaje_grasa, masa_muscular, objetivo, observaciones) VALUES
(1, 1, CURRENT_DATE, 76.5, 175.0, 18.0, 35.5, 'Hipertrofia / Ganancia Muscular', 'Progreso óptimo, buena respuesta a la carga.'),
(2, 2, CURRENT_DATE, 58.0, 162.0, 21.0, 24.0, 'Definición / Pérdida de Grasa', 'Excelente resistencia en circuito funcional.');

INSERT INTO usuario (username, password, nombre, rol, activo, cliente_id, entrenador_id) VALUES
('admin', '$2a$10$eAccYoNO32CpArGPl9OxP.4jGzT7mBvHcqIe7tJ9P7KkV6OqN4qC6', 'César Luza (Administrador General)', 'ROLE_ADMIN', TRUE, NULL, NULL),
('recepcion', '$2a$10$eAccYoNO32CpArGPl9OxP.4jGzT7mBvHcqIe7tJ9P7KkV6OqN4qC6', 'Staff Recepción BRUTAL', 'ROLE_RECEPCIONISTA', TRUE, NULL, NULL),
('entrenador', '$2a$10$eAccYoNO32CpArGPl9OxP.4jGzT7mBvHcqIe7tJ9P7KkV6OqN4qC6', 'Prof. Marco Antonio Vargas', 'ROLE_ENTRENADOR', TRUE, NULL, 1),
('72345678', '$2a$10$eAccYoNO32CpArGPl9OxP.4jGzT7mBvHcqIe7tJ9P7KkV6OqN4qC6', 'Juan Carlos Perez Lopez', 'ROLE_CLIENTE', TRUE, 1, NULL);

INSERT INTO ejercicio (nombre, grupo_muscular, descripcion, nivel_dificultad) VALUES
('Press de Banca Plano', 'Pecho', 'Empuje horizontal con barra para pectoral mayor', 'Intermedio'),
('Press Inclinado con Mancuernas', 'Pecho', 'Enfocado en el haz clavicular del pectoral', 'Intermedio'),
('Jalón al Pecho', 'Espalda', 'Tracción vertical en polea para dorsal ancho', 'Principiante'),
('Remo con Barra', 'Espalda', 'Tracción horizontal para grosor de espalda', 'Avanzado'),
('Sentadilla Libre con Barra', 'Piernas', 'Ejercicio compuesto para cuádriceps y glúteos', 'Avanzado'),
('Prensa Inclinada 45°', 'Piernas', 'Empuje de piernas en máquina guiada', 'Principiante'),
('Curl de Bíceps con Barra Z', 'Brazos', 'Flexión de codo con barra ergonómica', 'Principiante'),
('Extensión de Tríceps en Polea', 'Brazos', 'Aislamiento de las tres cabezas del tríceps', 'Principiante');

INSERT INTO rutina (cliente_id, entrenador_id, nombre, dia_semana, ejercicios, nivel, observaciones) VALUES
(1, 1, 'Fuerza e Hipertrofia A', 'Lunes - Pecho y Tríceps', '1. Press Banca: 4x10\n2. Press Inclinado: 3x12\n3. Extensiones Tríceps: 4x15', 'Intermedio', 'Descanso 90s entre series pesadas.'),
(1, 1, 'Fuerza e Hipertrofia B', 'Miércoles - Espalda y Bíceps', '1. Jalón al Pecho: 4x12\n2. Remo Barra: 4x10\n3. Curl Bíceps: 3x12', 'Intermedio', 'Cuidar técnica y retracción escapular.');

INSERT INTO rutina_detalle (rutina_id, ejercicio_id, series, repeticiones, peso_sugerido_kg, descanso_segundos) VALUES
(1, 1, 4, 10, 60.0, 90),
(1, 2, 3, 12, 22.5, 60),
(1, 8, 4, 15, 25.0, 45);

INSERT INTO categoria_producto (nombre, descripcion) VALUES
('Suplementos y Proteínas', 'Proteínas whey, creatinas y aminoácidos'),
('Bebidas y Energizantes', 'Agua mineral, isotónicos y pre-entrenos listos para tomar'),
('Accesorios Deportivos', 'Straps, guantillas, shakers y toallas');

INSERT INTO producto (categoria_id, codigo_barra, nombre, precio_compra, precio_venta, stock_actual, stock_minimo) VALUES
(1, '775001122334', 'Proteína Whey Gold Standard 2lb', 140.00, 190.00, 15, 3),
(1, '775001122335', 'Creatina Monohidratada Creapure 300g', 85.00, 120.00, 20, 5),
(2, '775001122336', 'Bebida Isotónica Gatorade 500ml', 2.50, 4.50, 50, 10),
(2, '775001122337', 'Agua San Mateo Sin Gas 600ml', 1.20, 2.50, 60, 12),
(3, '775001122338', 'Shaker Botella BRUTAL FITNESS 700ml', 12.00, 25.00, 30, 5);

INSERT INTO venta (cliente_id, usuario_id, fecha_hora, total, metodo_pago) VALUES
(1, 2, CURRENT_TIMESTAMP, 124.50, 'YAPE');

INSERT INTO venta_detalle (venta_id, producto_id, cantidad, precio_unitario, subtotal) VALUES
(1, 2, 1, 120.00, 120.00),
(1, 3, 1, 4.50, 4.50);

INSERT INTO disciplina (nombre, descripcion, intensidad) VALUES
('Spinning Indoor', 'Entrenamiento cardiovascular de alta intensidad en bicicleta fija', 'Alta'),
('Crossfit & WOD', 'Entrenamiento funcional de alta intensidad por intervalos', 'Muy Alta'),
('Boxeo Funcional', 'Técnicas de golpeo, resistencia y coordinación', 'Alta'),
('Yoga & Flexibilidad', 'Posturas, respiración y elongación muscular', 'Baja');

INSERT INTO clase_grupal (disciplina_id, entrenador_id, salon, fecha, hora_inicio, hora_fin, cupo_maximo) VALUES
(1, 2, 'Sala 1 - Ciclismo', CURRENT_DATE + INTERVAL '1 day', '07:00:00', '08:00:00', 20),
(2, 2, 'Box Principal Cross', CURRENT_DATE + INTERVAL '1 day', '18:30:00', '19:30:00', 15),
(3, 1, 'Sala de Contacto', CURRENT_DATE + INTERVAL '2 days', '19:00:00', '20:00:00', 12);

INSERT INTO reserva_clase (clase_id, cliente_id, fecha_reserva, estado) VALUES
(1, 1, CURRENT_TIMESTAMP, 'CONFIRMADA'),
(1, 2, CURRENT_TIMESTAMP, 'CONFIRMADA');

INSERT INTO casillero (numero, ubicacion, estado) VALUES
('L-01', 'Vestidor Varones - Piso 1', 'OCUPADO'),
('L-02', 'Vestidor Varones - Piso 1', 'DISPONIBLE'),
('L-03', 'Vestidor Varones - Piso 1', 'DISPONIBLE'),
('L-04', 'Vestidor Damas - Piso 1', 'DISPONIBLE'),
('L-05', 'Vestidor Damas - Piso 1', 'OCUPADO');

INSERT INTO alquiler_casillero (casillero_id, cliente_id, fecha_inicio, fecha_fin, costo, estado) VALUES
(1, 1, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 month', 30.00, 'ACTIVO');
