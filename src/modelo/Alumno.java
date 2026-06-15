package modelo;

import interfaces.Inscribible;

import java.util.ArrayList;

public class Alumno extends Persona implements Inscribible {

    private String fechaNacimiento;
    private String email;
    private int id;
    private ArrayList<Materia> materiasInscriptas;
    private ArrayList<Calificacion> calificaciones;


    public Alumno(int id, String nombre, String apellido, String ci,
                  String fechaNacimiento, String email) {
        super(nombre, apellido, ci);
        this.id = id;
        this.fechaNacimiento = fechaNacimiento;
        this.email = email;
        this.materiasInscriptas = new ArrayList<>();
        this.calificaciones = new ArrayList<>();
    }


    public Alumno(String nombre, String apellido, String ci,
                  String fechaNacimiento, String email) {
        super(nombre, apellido, ci);
        this.fechaNacimiento = fechaNacimiento;
        this.email = email;
        this.materiasInscriptas = new ArrayList<>();
        this.calificaciones = new ArrayList<>();
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public ArrayList<Materia> getMateriasInscriptas() {
        return materiasInscriptas;
    }

    public ArrayList<Calificacion> getCalificaciones() {
        return calificaciones;
    }

    // La CI no tiene setter: nunca se modifica
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    // Implementación de Inscribible
    // Solo actualiza la lista en memoria, la BD se maneja por separado
    @Override
    public void inscribirse(Materia m) {
        if (!materiasInscriptas.contains(m)) {
            materiasInscriptas.add(m);
        }
    }

    @Override
    public void darseDeBaja(Materia m) {
        materiasInscriptas.remove(m);
    }

    @Override
    public void mostrarInfo() {
        System.out.println("========================================");
        System.out.printf("  Alumno  : %s %s%n", nombre, apellido);
        System.out.printf("  CI      : %s%n", ci);
        System.out.printf("  Email   : %s%n", email);
        System.out.printf("  Nac.    : %s%n", fechaNacimiento);

        if (materiasInscriptas.isEmpty()) {
            System.out.println("  Materias: (sin inscripciones)");
        } else {
            System.out.println("  Materias:");
            for (Materia m : materiasInscriptas) {
                System.out.printf("    → %s (%s)%n", m.getNombre(), m.getCodigo());
                boolean tieneNotas = false;
                for (Calificacion c : calificaciones) {
                    if (c.getMateria().getCodigo().equals(m.getCodigo())) {
                        System.out.printf("        Nota: %.2f (%s)%n",
                                c.getNota(),
                                c.esAprobado() ? "APROBADO" : "DESAPROBADO");
                        tieneNotas = true;
                    }
                }
                if (!tieneNotas) {
                    System.out.println("        Sin notas registradas");
                }
            }
        }
        System.out.println("========================================");
    }

    @Override
    public String toString() {
        return String.format("CI: %-12s | %-15s %-15s | %s",
                ci, nombre, apellido, email);
    }
}