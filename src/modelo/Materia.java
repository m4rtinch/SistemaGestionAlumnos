package modelo;

import java.util.ArrayList;

public class Materia {

    private int id;
    private String nombre;
    private String codigo;
    private int cupoMaximo;
    private ArrayList<Alumno> alumnosInscriptos;


    public Materia(int id, String nombre, String codigo, int cupoMaximo) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        this.cupoMaximo = cupoMaximo;
        this.alumnosInscriptos = new ArrayList<>();
    }


    public Materia(String nombre, String codigo, int cupoMaximo) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.cupoMaximo = cupoMaximo;
        this.alumnosInscriptos = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(int cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public ArrayList<Alumno> getAlumnosInscriptos() {
        return alumnosInscriptos;
    }

    public int getCupoDisponible() {
        return cupoMaximo - alumnosInscriptos.size();
    }


    public boolean tieneCupo() {
        return getCupoDisponible() > 0;
    }

    public void mostrarAlumnosInscriptos() {
        System.out.println("\n── Alumnos inscriptos en " + nombre + " ──");
        if (alumnosInscriptos.isEmpty()) {
            System.out.println("  (Sin alumnos inscriptos)");
            return;
        }
        for (Alumno a : alumnosInscriptos) {
            System.out.printf("  CI: %-12s | %s %s%n",
                    a.getCi(), a.getNombre(), a.getApellido());
        }
    }

    @Override
    public String toString() {
        return String.format("Código: %-8s | %-25s | Cupo: %d/%d",
                codigo, nombre, alumnosInscriptos.size(), cupoMaximo);
    }
}