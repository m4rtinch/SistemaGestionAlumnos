package excepciones;

public class AlumnoDuplicadoException extends Exception {
    public AlumnoDuplicadoException(String ci) {
        super("Ya existe un alumno registrado con la CI: " + ci);
    }
}