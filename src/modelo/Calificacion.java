package modelo;

public class Calificacion {

    private int id;
    private double nota;
    private String fecha;
    private Materia materia;


    public Calificacion(int id, double nota, String fecha, Materia materia) {
        this.id = id;
        this.nota = nota;
        this.fecha = fecha;
        this.materia = materia;
    }


    public Calificacion(double nota, String fecha, Materia materia) {
        this.nota = nota;
        this.fecha = fecha;
        this.materia = materia;
    }

    public int getId() {
        return id;
    }

    public double getNota() {
        return nota;
    }

    public void setNota(double nota) {
        this.nota = nota;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Materia getMateria() {
        return materia;
    }

    public boolean esAprobado() {
        return nota >= 6.0;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Materia: %-20s | Nota: %.2f | %s",
                id,
                materia.getNombre(),
                nota,
                esAprobado() ? "APROBADO" : "DESAPROBADO");
    }
}
