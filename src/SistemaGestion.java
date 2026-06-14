import db.ConexionBD;
import excepciones.*;
import modelo.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class SistemaGestion {

    // Scanner compartido por todo el programa para leer entrada del usuario
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Verificamos la conexión al arrancar el programa
        Connection con = ConexionBD.getConexion();
        if (con == null) {
            System.out.println("No se pudo conectar a la base de datos. Cerrando.");
            return;
        }

        // Mostramos el menú principal en un loop infinito
        // Solo se rompe cuando el usuario elige "Salir"
        boolean salir = false;
        while (!salir) {
            salir = mostrarMenuPrincipal();
        }

        // Cerramos la conexión al salir
        ConexionBD.cerrarConexion();
    }

    // ─────────────────────────────────────────────
    // MENÚ PRINCIPAL
    // ─────────────────────────────────────────────

    private static boolean mostrarMenuPrincipal() {
        System.out.println("\n=== SISTEMA DE GESTIÓN DE ALUMNOS ===");
        System.out.println("1. Gestión de Alumnos");
        System.out.println("2. Gestión de Materias");
        System.out.println("3. Inscripciones");
        System.out.println("4. Calificaciones");
        System.out.println("5. Consultas y Búsquedas");
        System.out.println("6. Gestión de Docentes");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");

        int opcion = leerEntero();

        switch (opcion) {
            case 1: gestionarAlumnos();      break;
            case 2: gestionarMaterias();     break;
            case 3: gestionarInscripciones(); break;
            case 4: gestionarCalificaciones(); break;
            case 5: gestionarConsultas();    break;
            case 6: gestionarDocentes(); break;
            case 0: return true; // Salir
            default: System.out.println("Opción inválida.");
        }
        return false;
    }

    // ─────────────────────────────────────────────
    // GESTIÓN DE ALUMNOS
    // ─────────────────────────────────────────────

    private static void gestionarAlumnos() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── Gestión de Alumnos ──");
            System.out.println("1. Registrar nuevo alumno");
            System.out.println("2. Modificar datos de un alumno");
            System.out.println("3. Eliminar alumno");
            System.out.println("4. Listar todos los alumnos");
            System.out.println("0. Volver");
            System.out.print("Seleccione una opción: ");

            switch (leerEntero()) {
                case 1: registrarAlumno();   break;
                case 2: modificarAlumno();   break;
                case 3: eliminarAlumno();    break;
                case 4: listarAlumnos();     break;
                case 0: volver = true;       break;
                default: System.out.println("Opción inválida.");
            }
        }
    }

    private static void registrarAlumno() {
        System.out.println("\n── Registrar Nuevo Alumno ──");
        System.out.print("Nombre        : "); String nombre   = scanner.nextLine();
        System.out.print("Apellido      : "); String apellido = scanner.nextLine();
        System.out.print("CI            : "); String ci       = scanner.nextLine();
        System.out.print("Fecha nac (YYYY-MM-DD): "); String fecha = scanner.nextLine();
        System.out.print("Email         : "); String email    = scanner.nextLine();

        try {
            // Verificamos que la CI no esté duplicada antes de insertar
            if (buscarAlumnoPorCi(ci) != null) {
                throw new AlumnoDuplicadoException(ci);
            }

            // Preparamos la consulta SQL con parámetros (?) para evitar SQL Injection
            String sql = "INSERT INTO alumnos (nombre, apellido, ci, fecha_nacimiento, email) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, ci);
            ps.setString(4, fecha);
            ps.setString(5, email);
            ps.executeUpdate();

            System.out.println("✓ Alumno registrado correctamente.");

        } catch (AlumnoDuplicadoException e) {
            System.out.println("✗ Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    private static void modificarAlumno() {
        System.out.println("\n── Modificar Alumno ──");
        System.out.print("Ingrese CI del alumno: ");
        String ci = scanner.nextLine();

        try {
            Alumno alumno = buscarAlumnoPorCi(ci);
            if (alumno == null) throw new AlumnoNoEncontradoException(ci);

            // Mostramos datos actuales
            alumno.mostrarInfo();

            System.out.print("Nuevo nombre   (Enter para mantener): "); String nombre   = scanner.nextLine();
            System.out.print("Nuevo apellido (Enter para mantener): "); String apellido = scanner.nextLine();
            System.out.print("Nuevo email    (Enter para mantener): "); String email    = scanner.nextLine();

            // Si el usuario no escribió nada, mantenemos el valor actual
            if (nombre.isEmpty())   nombre   = alumno.getNombre();
            if (apellido.isEmpty()) apellido = alumno.getApellido();
            if (email.isEmpty())    email    = alumno.getEmail();

            String sql = "UPDATE alumnos SET nombre=?, apellido=?, email=? WHERE ci=?";
            PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, email);
            ps.setString(4, ci);
            ps.executeUpdate();

            System.out.println("✓ Alumno actualizado correctamente.");

        } catch (AlumnoNoEncontradoException e) {
            System.out.println("✗ Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    private static void eliminarAlumno() {
        System.out.println("\n── Eliminar Alumno ──");
        System.out.print("Ingrese CI del alumno: ");
        String ci = scanner.nextLine();

        try {
            Alumno alumno = buscarAlumnoPorCi(ci);
            if (alumno == null) throw new AlumnoNoEncontradoException(ci);

            alumno.mostrarInfo();

            // Verificamos si tiene inscripciones activas
            String sqlCheck = "SELECT COUNT(*) FROM inscripciones i " +
                    "JOIN alumnos a ON i.id_alumno = a.id WHERE a.ci = ?";
            PreparedStatement psCheck = ConexionBD.getConexion().prepareStatement(sqlCheck);
            psCheck.setString(1, ci);
            ResultSet rs = psCheck.executeQuery();
            rs.next();
            int inscripciones = rs.getInt(1);

            if (inscripciones > 0) {
                System.out.println("⚠ Este alumno tiene " + inscripciones + " inscripción/es activa/s.");
                System.out.print("¿Desea eliminarlo de todas formas? (s/n): ");
                String confirmacion = scanner.nextLine();
                if (!confirmacion.equalsIgnoreCase("s")) {
                    System.out.println("Operación cancelada.");
                    return;
                }
            }

            String sql = "DELETE FROM alumnos WHERE ci = ?";
            PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
            ps.setString(1, ci);
            ps.executeUpdate();

            System.out.println("✓ Alumno eliminado correctamente.");

        } catch (AlumnoNoEncontradoException e) {
            System.out.println("✗ Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    private static void listarAlumnos() {
        System.out.println("\n── Listado de Alumnos ──");
        try {
            String sql = "SELECT * FROM alumnos ORDER BY apellido, nombre";
            Statement st = ConexionBD.getConexion().createStatement();
            ResultSet rs = st.executeQuery(sql);

            boolean hayAlumnos = false;
            while (rs.next()) {
                hayAlumnos = true;
                System.out.printf("CI: %-12s | %-15s %-15s | %s%n",
                        rs.getString("ci"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("email"));
            }
            if (!hayAlumnos) System.out.println("No hay alumnos registrados.");

        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // MÉTODOS AUXILIARES
    // ─────────────────────────────────────────────

    /**
     * Busca un alumno en la BD por su CI.
     * Devuelve un objeto Alumno si lo encuentra, o null si no existe.
     * Lo usamos en varios métodos para no repetir el mismo SELECT.
     */
    private static Alumno buscarAlumnoPorCi(String ci) throws SQLException {
        String sql = "SELECT * FROM alumnos WHERE ci = ?";
        PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
        ps.setString(1, ci);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Alumno(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("ci"),
                    rs.getString("fecha_nacimiento"),
                    rs.getString("email")
            );
        }
        return null; // No encontrado
    }

    // ─────────────────────────────────────────────
// GESTIÓN DE MATERIAS
// ─────────────────────────────────────────────

    private static void gestionarMaterias() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── Gestión de Materias ──");
            System.out.println("1. Registrar nueva materia");
            System.out.println("2. Modificar materia");
            System.out.println("3. Eliminar materia");
            System.out.println("4. Listar todas las materias");
            System.out.println("0. Volver");
            System.out.print("Seleccione una opción: ");

            switch (leerEntero()) {
                case 1: registrarMateria(); break;
                case 2: modificarMateria(); break;
                case 3: eliminarMateria();  break;
                case 4: listarMaterias();   break;
                case 0: volver = true;      break;
                default: System.out.println("Opción inválida.");
            }
        }
    }

    private static void registrarMateria() {
        System.out.println("\n── Registrar Nueva Materia ──");
        System.out.print("Nombre      : "); String nombre = scanner.nextLine();
        System.out.print("Código      : "); String codigo = scanner.nextLine();
        System.out.print("Cupo máximo : "); int cupo = leerEntero();

        try {
            // Verificamos que el código no esté duplicado
            if (buscarMateriaPorCodigo(codigo) != null) {
                System.out.println("✗ Error: Ya existe una materia con el código: " + codigo);
                return;
            }

            String sql = "INSERT INTO materias (nombre, codigo, cupo_maximo) VALUES (?, ?, ?)";
            PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, codigo);
            ps.setInt(3, cupo);
            ps.executeUpdate();

            System.out.println("✓ Materia registrada correctamente.");

        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    private static void modificarMateria() {
        System.out.println("\n── Modificar Materia ──");
        System.out.print("Ingrese código de la materia: ");
        String codigo = scanner.nextLine();

        try {
            Materia materia = buscarMateriaPorCodigo(codigo);
            if (materia == null) throw new MateriaNoEncontradaException(codigo);

            System.out.println("Materia actual: " + materia);
            System.out.print("Nuevo nombre (Enter para mantener): "); String nombre = scanner.nextLine();
            System.out.print("Nuevo cupo   (0 para mantener)   : "); int cupo = leerEntero();

            // Si no escribió nada, mantenemos el valor actual
            if (nombre.isEmpty()) nombre = materia.getNombre();
            if (cupo <= 0)        cupo   = materia.getCupoMaximo();

            String sql = "UPDATE materias SET nombre=?, cupo_maximo=? WHERE codigo=?";
            PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setInt(2, cupo);
            ps.setString(3, codigo);
            ps.executeUpdate();

            System.out.println("✓ Materia actualizada correctamente.");

        } catch (MateriaNoEncontradaException e) {
            System.out.println("✗ Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    private static void eliminarMateria() {
        System.out.println("\n── Eliminar Materia ──");
        System.out.print("Ingrese código de la materia: ");
        String codigo = scanner.nextLine();

        try {
            Materia materia = buscarMateriaPorCodigo(codigo);
            if (materia == null) throw new MateriaNoEncontradaException(codigo);

            // Verificamos que no tenga alumnos inscriptos
            String sqlCheck = "SELECT COUNT(*) FROM inscripciones i " +
                    "JOIN materias m ON i.id_materia = m.id WHERE m.codigo = ?";
            PreparedStatement psCheck = ConexionBD.getConexion().prepareStatement(sqlCheck);
            psCheck.setString(1, codigo);
            ResultSet rs = psCheck.executeQuery();
            rs.next();
            int inscriptos = rs.getInt(1);

            if (inscriptos > 0) {
                System.out.println("✗ No se puede eliminar: la materia tiene "
                        + inscriptos + " alumno/s inscripto/s.");
                return;
            }

            String sql = "DELETE FROM materias WHERE codigo = ?";
            PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
            ps.setString(1, codigo);
            ps.executeUpdate();

            System.out.println("✓ Materia eliminada correctamente.");

        } catch (MateriaNoEncontradaException e) {
            System.out.println("✗ Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    private static void listarMaterias() {
        System.out.println("\n── Listado de Materias ──");
        try {
            // Contamos los inscriptos de cada materia con un JOIN
            String sql = "SELECT m.nombre, m.codigo, m.cupo_maximo, " +
                    "COUNT(i.id) AS inscriptos " +
                    "FROM materias m " +
                    "LEFT JOIN inscripciones i ON m.id = i.id_materia " +
                    "GROUP BY m.id " +
                    "ORDER BY m.nombre";
            Statement st = ConexionBD.getConexion().createStatement();
            ResultSet rs = st.executeQuery(sql);

            boolean hayMaterias = false;
            while (rs.next()) {
                hayMaterias = true;
                System.out.printf("Código: %-8s | %-25s | Cupo: %d/%d%n",
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getInt("inscriptos"),
                        rs.getInt("cupo_maximo"));
            }
            if (!hayMaterias) System.out.println("No hay materias registradas.");

        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    // Busca una materia por su código — devuelve null si no existe
    private static Materia buscarMateriaPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM materias WHERE codigo = ?";
        PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
        ps.setString(1, codigo);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Materia(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("codigo"),
                    rs.getInt("cupo_maximo")
            );
        }
        return null;
    }
    // ─────────────────────────────────────────────
// GESTIÓN DE INSCRIPCIONES
// ─────────────────────────────────────────────

    private static void gestionarInscripciones() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── Gestión de Inscripciones ──");
            System.out.println("1. Inscribir alumno a una materia");
            System.out.println("2. Dar de baja a un alumno de una materia");
            System.out.println("3. Listar alumnos inscriptos en una materia");
            System.out.println("0. Volver");
            System.out.print("Seleccione una opción: ");

            switch (leerEntero()) {
                case 1: inscribirAlumno();         break;
                case 2: darDeBajaAlumno();         break;
                case 3: listarInscriptosMateria(); break;
                case 0: volver = true;             break;
                default: System.out.println("Opción inválida.");
            }
        }
    }

    private static void inscribirAlumno() {
        System.out.println("\n── Inscribir Alumno ──");
        System.out.print("CI del alumno        : "); String ci     = scanner.nextLine();
        System.out.print("Código de la materia : "); String codigo = scanner.nextLine();

        try {
            // Validación 1: el alumno existe?
            Alumno alumno = buscarAlumnoPorCi(ci);
            if (alumno == null) throw new AlumnoNoEncontradoException(ci);

            // Validación 2: la materia existe?
            Materia materia = buscarMateriaPorCodigo(codigo);
            if (materia == null) throw new MateriaNoEncontradaException(codigo);

            // Validación 3: hay cupo disponible?
            // Consultamos la BD para tener el dato actualizado
            String sqlCupo = "SELECT COUNT(*) FROM inscripciones WHERE id_materia = ?";
            PreparedStatement psCupo = ConexionBD.getConexion().prepareStatement(sqlCupo);
            psCupo.setInt(1, materia.getId());
            ResultSet rsCupo = psCupo.executeQuery();
            rsCupo.next();
            int inscriptosActuales = rsCupo.getInt(1);

            if (inscriptosActuales >= materia.getCupoMaximo()) {
                throw new CupoExcedidoException(materia.getNombre());
            }

            // Validación 4: el alumno ya está inscripto en esa materia?
            String sqlCheck = "SELECT COUNT(*) FROM inscripciones " +
                    "WHERE id_alumno = ? AND id_materia = ?";
            PreparedStatement psCheck = ConexionBD.getConexion().prepareStatement(sqlCheck);
            psCheck.setInt(1, alumno.getId());
            psCheck.setInt(2, materia.getId());
            ResultSet rsCheck = psCheck.executeQuery();
            rsCheck.next();
            if (rsCheck.getInt(1) > 0) {
                System.out.println("✗ El alumno ya está inscripto en esa materia.");
                return;
            }

            // Todas las validaciones pasaron — insertamos la inscripción
            String sql = "INSERT INTO inscripciones (id_alumno, id_materia, fecha_inscripcion) " +
                    "VALUES (?, ?, CURDATE())";
            PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
            ps.setInt(1, alumno.getId());
            ps.setInt(2, materia.getId());
            ps.executeUpdate();

            System.out.println("✓ Alumno inscripto correctamente en " + materia.getNombre());

        } catch (AlumnoNoEncontradoException | MateriaNoEncontradaException | CupoExcedidoException e) {
            System.out.println("✗ Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    private static void darDeBajaAlumno() {
        System.out.println("\n── Dar de Baja ──");
        System.out.print("CI del alumno        : "); String ci     = scanner.nextLine();
        System.out.print("Código de la materia : "); String codigo = scanner.nextLine();

        try {
            Alumno alumno = buscarAlumnoPorCi(ci);
            if (alumno == null) throw new AlumnoNoEncontradoException(ci);

            Materia materia = buscarMateriaPorCodigo(codigo);
            if (materia == null) throw new MateriaNoEncontradaException(codigo);

            // Verificamos que la inscripción exista
            String sqlCheck = "SELECT id FROM inscripciones " +
                    "WHERE id_alumno = ? AND id_materia = ?";
            PreparedStatement psCheck = ConexionBD.getConexion().prepareStatement(sqlCheck);
            psCheck.setInt(1, alumno.getId());
            psCheck.setInt(2, materia.getId());
            ResultSet rs = psCheck.executeQuery();

            if (!rs.next()) {
                System.out.println("✗ El alumno no está inscripto en esa materia.");
                return;
            }

            // Pedimos confirmación antes de eliminar
            System.out.println("⚠ Se eliminarán también las calificaciones asociadas.");
            System.out.print("¿Confirma la baja? (s/n): ");
            String confirmacion = scanner.nextLine();
            if (!confirmacion.equalsIgnoreCase("s")) {
                System.out.println("Operación cancelada.");
                return;
            }

            // Al borrar la inscripción, las calificaciones se borran en cascada (ON DELETE CASCADE)
            String sql = "DELETE FROM inscripciones WHERE id_alumno = ? AND id_materia = ?";
            PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
            ps.setInt(1, alumno.getId());
            ps.setInt(2, materia.getId());
            ps.executeUpdate();

            System.out.println("✓ Baja realizada correctamente.");

        } catch (AlumnoNoEncontradoException | MateriaNoEncontradaException e) {
            System.out.println("✗ Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    private static void listarInscriptosMateria() {
        System.out.println("\n── Alumnos Inscriptos ──");
        System.out.print("Código de la materia: ");
        String codigo = scanner.nextLine();

        try {
            Materia materia = buscarMateriaPorCodigo(codigo);
            if (materia == null) throw new MateriaNoEncontradaException(codigo);

            // JOIN entre inscripciones y alumnos para traer los datos del alumno
            String sql = "SELECT a.ci, a.nombre, a.apellido " +
                    "FROM inscripciones i " +
                    "JOIN alumnos a ON i.id_alumno = a.id " +
                    "JOIN materias m ON i.id_materia = m.id " +
                    "WHERE m.codigo = ? " +
                    "ORDER BY a.apellido, a.nombre";
            PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();

            System.out.println("Materia: " + materia.getNombre());
            boolean hayInscriptos = false;
            while (rs.next()) {
                hayInscriptos = true;
                System.out.printf("  CI: %-12s | %s %s%n",
                        rs.getString("ci"),
                        rs.getString("nombre"),
                        rs.getString("apellido"));
            }
            if (!hayInscriptos) System.out.println("  (Sin alumnos inscriptos)");

        } catch (MateriaNoEncontradaException e) {
            System.out.println("✗ Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }
    // ─────────────────────────────────────────────
// GESTIÓN DE CALIFICACIONES
// ─────────────────────────────────────────────

    private static void gestionarCalificaciones() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── Gestión de Calificaciones ──");
            System.out.println("1. Cargar nota");
            System.out.println("2. Modificar nota");
            System.out.println("3. Eliminar nota");
            System.out.println("4. Ver notas de un alumno");
            System.out.println("0. Volver");
            System.out.print("Seleccione una opción: ");

            switch (leerEntero()) {
                case 1: cargarNota();        break;
                case 2: modificarNota();     break;
                case 3: eliminarNota();      break;
                case 4: verNotasAlumno();    break;
                case 0: volver = true;       break;
                default: System.out.println("Opción inválida.");
            }
        }
    }

    private static void cargarNota() {
        System.out.println("\n── Cargar Nota ──");
        System.out.print("CI del alumno        : "); String ci     = scanner.nextLine();
        System.out.print("Código de la materia : "); String codigo = scanner.nextLine();

        try {
            Alumno alumno = buscarAlumnoPorCi(ci);
            if (alumno == null) throw new AlumnoNoEncontradoException(ci);

            Materia materia = buscarMateriaPorCodigo(codigo);
            if (materia == null) throw new MateriaNoEncontradaException(codigo);

            // Verificamos que el alumno esté inscripto en esa materia
            // y obtenemos el id de la inscripción que necesitamos para insertar la nota
            String sqlInsc = "SELECT id FROM inscripciones " +
                    "WHERE id_alumno = ? AND id_materia = ?";
            PreparedStatement psInsc = ConexionBD.getConexion().prepareStatement(sqlInsc);
            psInsc.setInt(1, alumno.getId());
            psInsc.setInt(2, materia.getId());
            ResultSet rsInsc = psInsc.executeQuery();

            if (!rsInsc.next()) {
                System.out.println("✗ El alumno no está inscripto en esa materia.");
                return;
            }
            int idInscripcion = rsInsc.getInt("id");

            // Pedimos la nota y validamos que esté entre 1 y 12
            System.out.print("Nota (1-12): ");
            double nota = Double.parseDouble(scanner.nextLine().trim());
            if (nota < 1 || nota > 12) {
                System.out.println("✗ La nota debe estar entre 1 y 12.");
                return;
            }

            String sql = "INSERT INTO calificaciones (id_inscripcion, nota, fecha) " +
                    "VALUES (?, ?, CURDATE())";
            PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
            ps.setInt(1, idInscripcion);
            ps.setDouble(2, nota);
            ps.executeUpdate();

            System.out.println("✓ Nota cargada correctamente.");

        } catch (AlumnoNoEncontradoException | MateriaNoEncontradaException e) {
            System.out.println("✗ Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("✗ Error: ingresá un número válido.");
        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    private static void modificarNota() {
        System.out.println("\n── Modificar Nota ──");
        System.out.print("ID de la nota a modificar: ");
        int idNota = leerEntero();

        try {
            // Verificamos que la nota exista
            String sqlCheck = "SELECT id, nota FROM calificaciones WHERE id = ?";
            PreparedStatement psCheck = ConexionBD.getConexion().prepareStatement(sqlCheck);
            psCheck.setInt(1, idNota);
            ResultSet rs = psCheck.executeQuery();

            if (!rs.next()) {
                System.out.println("✗ No existe una nota con el ID: " + idNota);
                return;
            }

            System.out.println("Nota actual: " + rs.getDouble("nota"));
            System.out.print("Nueva nota (1-12): ");
            double nuevaNota = Double.parseDouble(scanner.nextLine().trim());

            if (nuevaNota < 1 || nuevaNota > 12) {
                System.out.println("✗ La nota debe estar entre 1 y 12.");
                return;
            }

            String sql = "UPDATE calificaciones SET nota = ?, fecha = CURDATE() WHERE id = ?";
            PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
            ps.setDouble(1, nuevaNota);
            ps.setInt(2, idNota);
            ps.executeUpdate();

            System.out.println("✓ Nota modificada correctamente.");

        } catch (NumberFormatException e) {
            System.out.println("✗ Error: ingresá un número válido.");
        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    private static void eliminarNota() {
        System.out.println("\n── Eliminar Nota ──");
        System.out.print("ID de la nota a eliminar: ");
        int idNota = leerEntero();

        try {
            // Verificamos que exista
            String sqlCheck = "SELECT id, nota FROM calificaciones WHERE id = ?";
            PreparedStatement psCheck = ConexionBD.getConexion().prepareStatement(sqlCheck);
            psCheck.setInt(1, idNota);
            ResultSet rs = psCheck.executeQuery();

            if (!rs.next()) {
                System.out.println("✗ No existe una nota con el ID: " + idNota);
                return;
            }

            System.out.println("Nota a eliminar: " + rs.getDouble("nota"));
            System.out.print("¿Confirma la eliminación? (s/n): ");
            String confirmacion = scanner.nextLine();
            if (!confirmacion.equalsIgnoreCase("s")) {
                System.out.println("Operación cancelada.");
                return;
            }

            String sql = "DELETE FROM calificaciones WHERE id = ?";
            PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
            ps.setInt(1, idNota);
            ps.executeUpdate();

            System.out.println("✓ Nota eliminada correctamente.");

        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    private static void verNotasAlumno() {
        System.out.println("\n── Notas del Alumno ──");
        System.out.print("CI del alumno: ");
        String ci = scanner.nextLine();

        try {
            Alumno alumno = buscarAlumnoPorCi(ci);
            if (alumno == null) throw new AlumnoNoEncontradoException(ci);

            System.out.printf("%nAlumno: %s %s | CI: %s%n",
                    alumno.getNombre(), alumno.getApellido(), alumno.getCi());

            // Traemos todas las materias en las que está inscripto el alumno
            String sqlMaterias = "SELECT m.nombre, m.codigo, i.id as id_insc " +
                    "FROM inscripciones i " +
                    "JOIN materias m ON i.id_materia = m.id " +
                    "WHERE i.id_alumno = ? " +
                    "ORDER BY m.nombre";
            PreparedStatement psMaterias = ConexionBD.getConexion().prepareStatement(sqlMaterias);
            psMaterias.setInt(1, alumno.getId());
            ResultSet rsMaterias = psMaterias.executeQuery();

            boolean tieneInscripciones = false;
            while (rsMaterias.next()) {
                tieneInscripciones = true;
                System.out.println("\nMateria: " + rsMaterias.getString("nombre"));

                // Para cada materia traemos sus calificaciones
                String sqlNotas = "SELECT id, nota, fecha FROM calificaciones " +
                        "WHERE id_inscripcion = ? ORDER BY fecha";
                PreparedStatement psNotas = ConexionBD.getConexion().prepareStatement(sqlNotas);
                psNotas.setInt(1, rsMaterias.getInt("id_insc"));
                ResultSet rsNotas = psNotas.executeQuery();

                boolean tieneNotas = false;
                while (rsNotas.next()) {
                    tieneNotas = true;
                    double nota = rsNotas.getDouble("nota");
                    System.out.printf("  ID: %d | Nota: %.2f | Fecha: %s | %s%n",
                            rsNotas.getInt("id"),
                            nota,
                            rsNotas.getString("fecha"),
                            nota >= 6 ? "APROBADO" : "DESAPROBADO");
                }
                if (!tieneNotas) System.out.println("  - Sin notas registradas");
            }

            if (!tieneInscripciones) System.out.println("El alumno no tiene inscripciones.");

        } catch (AlumnoNoEncontradoException e) {
            System.out.println("✗ Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }
// ─────────────────────────────────────────────
// CONSULTAS Y BÚSQUEDAS
// ─────────────────────────────────────────────

    private static void gestionarConsultas() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── Consultas y Búsquedas ──");
            System.out.println("1. Buscar alumno por CI");
            System.out.println("2. Buscar alumnos por nombre o apellido");
            System.out.println("3. Listar alumnos por estado académico");
            System.out.println("0. Volver");
            System.out.print("Seleccione una opción: ");

            switch (leerEntero()) {
                case 1: buscarPorCi();             break;
                case 2: buscarPorNombreApellido(); break;
                case 3: listarPorEstado();         break;
                case 0: volver = true;             break;
                default: System.out.println("Opción inválida.");
            }
        }
    }

    private static void buscarPorCi() {
        System.out.println("\n── Buscar por CI ──");
        System.out.print("Ingrese CI: ");
        String ci = scanner.nextLine();

        try {
            Alumno alumno = buscarAlumnoPorCi(ci);
            if (alumno == null) throw new AlumnoNoEncontradoException(ci);

            // Mostramos datos del alumno
            System.out.println("\n========================================");
            System.out.printf("  Alumno  : %s %s%n", alumno.getNombre(), alumno.getApellido());
            System.out.printf("  CI      : %s%n", alumno.getCi());
            System.out.printf("  Email   : %s%n", alumno.getEmail());
            System.out.printf("  Nac.    : %s%n", alumno.getFechaNacimiento());

            // Mostramos sus materias y notas
            String sql = "SELECT m.nombre, m.codigo, i.id as id_insc " +
                    "FROM inscripciones i " +
                    "JOIN materias m ON i.id_materia = m.id " +
                    "WHERE i.id_alumno = ? ORDER BY m.nombre";
            PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
            ps.setInt(1, alumno.getId());
            ResultSet rs = ps.executeQuery();

            boolean tieneInscripciones = false;
            while (rs.next()) {
                tieneInscripciones = true;
                System.out.println("\n  Materia: " + rs.getString("nombre") +
                        " (" + rs.getString("codigo") + ")");

                String sqlNotas = "SELECT nota, fecha FROM calificaciones " +
                        "WHERE id_inscripcion = ? ORDER BY fecha";
                PreparedStatement psNotas = ConexionBD.getConexion().prepareStatement(sqlNotas);
                psNotas.setInt(1, rs.getInt("id_insc"));
                ResultSet rsNotas = psNotas.executeQuery();

                boolean tieneNotas = false;
                while (rsNotas.next()) {
                    tieneNotas = true;
                    double nota = rsNotas.getDouble("nota");
                    System.out.printf("    - Nota: %.2f | %s | %s%n",
                            nota,
                            rsNotas.getString("fecha"),
                            nota >= 6 ? "APROBADO" : "DESAPROBADO");
                }
                if (!tieneNotas) System.out.println("    - Sin notas registradas");
            }
            if (!tieneInscripciones) System.out.println("\n  Sin inscripciones.");
            System.out.println("========================================");

        } catch (AlumnoNoEncontradoException e) {
            System.out.println("✗ Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    private static void buscarPorNombreApellido() {
        System.out.println("\n── Buscar por Nombre o Apellido ──");
        System.out.print("Ingrese texto a buscar: ");
        String texto = scanner.nextLine();

        try {
            // LIKE con % busca la cadena en cualquier posición
            // LOWER() hace que la búsqueda no distinga mayúsculas de minúsculas
            String sql = "SELECT * FROM alumnos " +
                    "WHERE LOWER(nombre) LIKE LOWER(?) " +
                    "OR LOWER(apellido) LIKE LOWER(?) " +
                    "ORDER BY apellido, nombre";
            PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
            String patron = "%" + texto + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            ResultSet rs = ps.executeQuery();

            boolean hayResultados = false;
            System.out.println();
            while (rs.next()) {
                hayResultados = true;
                System.out.printf("CI: %-12s | %-15s %-15s | %s%n",
                        rs.getString("ci"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("email"));
            }
            if (!hayResultados) System.out.println("No se encontraron alumnos.");

        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    private static void listarPorEstado() {
        System.out.println("\n── Listar por Estado Académico ──");
        System.out.println("1. Aprobado (nota >= 6) en al menos una materia");
        System.out.println("2. Desaprobado (nota < 6) en al menos una materia");
        System.out.println("3. Sin calificaciones");
        System.out.print("Seleccione filtro: ");
        int opcion = leerEntero();

        try {
            String sql = "";

            switch (opcion) {
                case 1:
                    // Alumnos que tienen AL MENOS UNA nota aprobada
                    sql = "SELECT DISTINCT a.ci, a.nombre, a.apellido " +
                            "FROM alumnos a " +
                            "JOIN inscripciones i ON a.id = i.id_alumno " +
                            "JOIN calificaciones c ON i.id = c.id_inscripcion " +
                            "WHERE c.nota >= 6 ORDER BY a.apellido, a.nombre";
                    break;
                case 2:
                    // Alumnos que tienen AL MENOS UNA nota desaprobada
                    sql = "SELECT DISTINCT a.ci, a.nombre, a.apellido " +
                            "FROM alumnos a " +
                            "JOIN inscripciones i ON a.id = i.id_alumno " +
                            "JOIN calificaciones c ON i.id = c.id_inscripcion " +
                            "WHERE c.nota < 6 ORDER BY a.apellido, a.nombre";
                    break;
                case 3:
                    // Alumnos que no tienen ninguna calificación registrada
                    sql = "SELECT DISTINCT a.ci, a.nombre, a.apellido " +
                            "FROM alumnos a " +
                            "JOIN inscripciones i ON a.id = i.id_alumno " +
                            "WHERE i.id NOT IN (SELECT id_inscripcion FROM calificaciones) " +
                            "ORDER BY a.apellido, a.nombre";
                    break;
                default:
                    System.out.println("Opción inválida.");
                    return;
            }

            Statement st = ConexionBD.getConexion().createStatement();
            ResultSet rs = st.executeQuery(sql);

            boolean hayResultados = false;
            System.out.println();
            while (rs.next()) {
                hayResultados = true;
                System.out.printf("CI: %-12s | %-15s %-15s%n",
                        rs.getString("ci"),
                        rs.getString("nombre"),
                        rs.getString("apellido"));
            }
            if (!hayResultados) System.out.println("No se encontraron alumnos.");

        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    /**
     * Lee un número entero del teclado.
     * Si el usuario escribe algo que no es número, devuelve -1
     * en lugar de crashear el programa.
     */
    // ─────────────────────────────────────────────
// GESTIÓN DE DOCENTES
// ─────────────────────────────────────────────

    private static void gestionarDocentes() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── Gestión de Docentes ──");
            System.out.println("1. Registrar nuevo docente");
            System.out.println("2. Modificar docente");
            System.out.println("3. Eliminar docente");
            System.out.println("4. Listar todos los docentes");
            System.out.println("0. Volver");
            System.out.print("Seleccione una opción: ");

            switch (leerEntero()) {
                case 1: registrarDocente(); break;
                case 2: modificarDocente(); break;
                case 3: eliminarDocente();  break;
                case 4: listarDocentes();   break;
                case 0: volver = true;      break;
                default: System.out.println("Opción inválida.");
            }
        }
    }

    private static void registrarDocente() {
        System.out.println("\n── Registrar Nuevo Docente ──");
        System.out.print("Nombre        : "); String nombre       = scanner.nextLine();
        System.out.print("Apellido      : "); String apellido     = scanner.nextLine();
        System.out.print("CI            : "); String ci           = scanner.nextLine();
        System.out.print("Especialidad  : "); String especialidad = scanner.nextLine();

        try {
            // Verificamos que la CI no esté duplicada
            if (buscarDocentePorCi(ci) != null) {
                System.out.println("✗ Error: Ya existe un docente con la CI: " + ci);
                return;
            }

            String sql = "INSERT INTO docentes (nombre, apellido, ci, especialidad) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, ci);
            ps.setString(4, especialidad);
            ps.executeUpdate();

            System.out.println("✓ Docente registrado correctamente.");

        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    private static void modificarDocente() {
        System.out.println("\n── Modificar Docente ──");
        System.out.print("Ingrese CI del docente: ");
        String ci = scanner.nextLine();

        try {
            Docente docente = buscarDocentePorCi(ci);
            if (docente == null) {
                System.out.println("✗ No se encontró ningún docente con la CI: " + ci);
                return;
            }

            docente.mostrarInfo();

            System.out.print("Nuevo nombre       (Enter para mantener): "); String nombre       = scanner.nextLine();
            System.out.print("Nuevo apellido     (Enter para mantener): "); String apellido     = scanner.nextLine();
            System.out.print("Nueva especialidad (Enter para mantener): "); String especialidad = scanner.nextLine();

            if (nombre.isEmpty())       nombre       = docente.getNombre();
            if (apellido.isEmpty())     apellido     = docente.getApellido();
            if (especialidad.isEmpty()) especialidad = docente.getEspecialidad();

            String sql = "UPDATE docentes SET nombre=?, apellido=?, especialidad=? WHERE ci=?";
            PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, especialidad);
            ps.setString(4, ci);
            ps.executeUpdate();

            System.out.println("✓ Docente actualizado correctamente.");

        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    private static void eliminarDocente() {
        System.out.println("\n── Eliminar Docente ──");
        System.out.print("Ingrese CI del docente: ");
        String ci = scanner.nextLine();

        try {
            Docente docente = buscarDocentePorCi(ci);
            if (docente == null) {
                System.out.println("✗ No se encontró ningún docente con la CI: " + ci);
                return;
            }

            docente.mostrarInfo();

            // Verificamos si tiene materias asignadas
            String sqlCheck = "SELECT COUNT(*) FROM materias WHERE id_docente = ?";
            PreparedStatement psCheck = ConexionBD.getConexion().prepareStatement(sqlCheck);
            psCheck.setInt(1, docente.getId());
            ResultSet rs = psCheck.executeQuery();
            rs.next();
            int materias = rs.getInt(1);

            if (materias > 0) {
                System.out.println("⚠ Este docente tiene " + materias + " materia/s asignada/s.");
                System.out.println("  Al eliminarlo, las materias quedarán sin docente.");
            }

            System.out.print("¿Confirma la eliminación? (s/n): ");
            String confirmacion = scanner.nextLine();
            if (!confirmacion.equalsIgnoreCase("s")) {
                System.out.println("Operación cancelada.");
                return;
            }

            String sql = "DELETE FROM docentes WHERE ci = ?";
            PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
            ps.setString(1, ci);
            ps.executeUpdate();

            System.out.println("✓ Docente eliminado correctamente.");

        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    private static void listarDocentes() {
        System.out.println("\n── Listado de Docentes ──");
        try {
            String sql = "SELECT * FROM docentes ORDER BY apellido, nombre";
            Statement st = ConexionBD.getConexion().createStatement();
            ResultSet rs = st.executeQuery(sql);

            boolean hayDocentes = false;
            while (rs.next()) {
                hayDocentes = true;
                System.out.printf("CI: %-12s | %-15s %-15s | %s%n",
                        rs.getString("ci"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("especialidad") != null ? rs.getString("especialidad") : "-");
            }
            if (!hayDocentes) System.out.println("No hay docentes registrados.");

        } catch (SQLException e) {
            System.out.println("✗ Error de base de datos: " + e.getMessage());
        }
    }

    // Busca un docente por CI — devuelve null si no existe
    private static Docente buscarDocentePorCi(String ci) throws SQLException {
        String sql = "SELECT * FROM docentes WHERE ci = ?";
        PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql);
        ps.setString(1, ci);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Docente(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("ci"),
                    rs.getString("especialidad")
            );
        }
        return null;
    }
    private static int leerEntero() {
        try {
            String linea = scanner.nextLine();
            return Integer.parseInt(linea.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
