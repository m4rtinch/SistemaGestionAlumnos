package excepciones;

public class CupoExcedidoException extends Exception {
    public CupoExcedidoException(String nombreMateria) {
        super("La materia '" + nombreMateria + "' no tiene cupo disponible.");
    }
}