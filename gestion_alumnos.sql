CREATE DATABASE IF NOT EXISTS gestion_alumnos;

USE gestion_alumnos;

CREATE TABLE IF NOT EXISTS alumnos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    ci VARCHAR(20) NOT NULL UNIQUE,
    fecha_nacimiento DATE NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS docentes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    ci VARCHAR(20) NOT NULL UNIQUE,
    especialidad VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS materias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    cupo_maximo INT NOT NULL,
    id_docente INT,
    CONSTRAINT fk_docente FOREIGN KEY (id_docente)
    REFERENCES docentes(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS inscripciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_alumno INT NOT NULL,
    id_materia INT NOT NULL,
    fecha_inscripcion DATE NOT NULL,
    CONSTRAINT unique_alumno_materia UNIQUE (id_alumno, id_materia),
    CONSTRAINT fk_alumno FOREIGN KEY (id_alumno)
    REFERENCES alumnos(id) ON DELETE CASCADE,
    CONSTRAINT fk_materia FOREIGN KEY (id_materia)
    REFERENCES materias(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS calificaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_inscripcion INT NOT NULL,
    nota DECIMAL(4,2) NOT NULL,
    fecha DATE NOT NULL,
    CONSTRAINT fk_inscripcion FOREIGN KEY (id_inscripcion)
    REFERENCES inscripciones(id) ON DELETE CASCADE
);