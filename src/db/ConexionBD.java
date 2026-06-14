package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    // Datos para conectarse a la base de datos local
    // Cambiá "root" y "tu_password" por los tuyos de MySQL
    private static final String URL      = "jdbc:mysql://localhost:3306/gestion_alumnos";
    private static final String USER     = "root";
    private static final String PASSWORD = "admin";

    // Variable que guarda la conexión activa
    // Es static porque queremos UNA SOLA conexión para todo el programa
    private static Connection conexion = null;

    // Constructor privado: nadie puede hacer new ConexionBD() desde afuera
    // Esto es el patrón Singleton — garantiza una única instancia
    private ConexionBD() {}

    /**
     * Devuelve la conexión activa.
     * Si no existe todavía, la crea.
     * Si ya existe, devuelve la misma — no abre una nueva cada vez.
     */
    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                // DriverManager busca el driver de MySQL en el .jar que agregamos
                // y establece la conexión con los datos que le pasamos
                conexion = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✓ Conexión a la base de datos establecida.");
            }
        } catch (SQLException e) {
            System.out.println("✗ Error al conectar con la base de datos: " + e.getMessage());
        }
        return conexion;
    }

    /**
     * Cierra la conexión cuando el programa termina.
     * Importante: dejar conexiones abiertas consume recursos del servidor MySQL.
     */
    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("✓ Conexión cerrada correctamente.");
            }
        } catch (SQLException e) {
            System.out.println("✗ Error al cerrar la conexión: " + e.getMessage());
        }
    }
}