package excepciones;

public class AlumnoNoEncontradoException extends Exception {
    public AlumnoNoEncontradoException(String ci) {
        super("No se encontró ningún alumno con la CI: " + ci);
    }
}