package excepciones;

public class MateriaNoEncontradaException extends Exception {
    public MateriaNoEncontradaException(String codigo) {
        super("No se encontró ninguna materia con el código: " + codigo);
    }
}