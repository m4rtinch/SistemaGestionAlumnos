package modelo;

public class Docente extends Persona {

    private String especialidad;

    //  cuando cargamos desde la BD
    public Docente(int id, String nombre, String apellido,
                   String ci, String especialidad) {
        super(nombre, apellido, ci);
        this.especialidad = especialidad;
    }

    // Constructor sin id: cuando registramos un docente nuevo
    public Docente(String nombre, String apellido,
                   String ci, String especialidad) {
        super(nombre, apellido, ci);
        this.especialidad = especialidad;
    }

    public String getEspecialidad()         { return especialidad; }
    public void setEspecialidad(String esp) { this.especialidad = esp; }

    @Override
    public void mostrarInfo() {
        System.out.printf("Docente: %s %s | CI: %s | Especialidad: %s%n",
                nombre, apellido, ci,
                especialidad != null ? especialidad : "No especificada");
    }

    @Override
    public String toString() {
        return String.format("CI: %-12s | %-15s %-15s | %s",
                ci, nombre, apellido,
                especialidad != null ? especialidad : "-");
    }
}