package servicio;

import dao.DatabaseConnection;
import dao.EstudianteDAO;
import dao.LibroDAO;
import dao.PrestamoDAO;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Prestamo;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;


// prestamo y devoluviones
public class ServicioPrestamos {

    public static final int DIAS_PRESTAMO = 7;
    public static final int MAX_PRESTAMOS_ACTIVOS = 3;

    private static ServicioPrestamos instancia;

    private final LibroDAO libroDAO = new LibroDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();

    private ServicioPrestamos() {
    }

    public static synchronized ServicioPrestamos getInstance() {
        if (instancia == null) {
            instancia = new ServicioPrestamos();
        }
        return instancia;
    }

    // codigo que se ejecuta dentro de una transaccion
    private interface Operacion<T> {
        T ejecutar(Connection conexion) throws SQLException, PrestamoException;
    }


    // registra el prestamo
    public synchronized Prestamo registrarPrestamo(int idEstudiante, int idLibro) throws SQLException, PrestamoException {
        return enTransaccion(conexion -> {
            Estudiante estudiante = estudianteDAO.readById(idEstudiante);
            if (estudiante == null) {
                throw new PrestamoException("El estudiante seleccionado ya no existe.");
            }
            Libro libro = libroDAO.readById(idLibro);
            if (libro == null) {
                throw new PrestamoException("El libro seleccionado ya no existe.");
            }
            if (prestamoDAO.contarActivos(idEstudiante) >= MAX_PRESTAMOS_ACTIVOS) {
                throw new PrestamoException(estudiante.getNombre() + " ya tiene " + MAX_PRESTAMOS_ACTIVOS
                        + " libros sin devolver (máximo permitido).");
            }
            if (prestamoDAO.tieneActivo(idEstudiante, idLibro)) {
                throw new PrestamoException(estudiante.getNombre() + " ya tiene en préstamo \"" + libro.getTitulo() + "\".");
            }
            if (!libroDAO.descontarStock(conexion, idLibro)) {
                throw new PrestamoException("No hay stock disponible de \"" + libro.getTitulo() + "\".");
            }

            LocalDate hoy = LocalDate.now();
            Prestamo prestamo = new Prestamo(idEstudiante, idLibro, hoy, hoy.plusDays(DIAS_PRESTAMO));
            prestamo.setNombreEstudiante(estudiante.getNombre());
            prestamo.setTituloLibro(libro.getTitulo());
            prestamoDAO.create(conexion, prestamo);
            return prestamo;
        });
    }


    // se regigistra la devolucion
    public synchronized int registrarDevolucion(int idPrestamo) throws SQLException, PrestamoException {
        Prestamo prestamo = prestamoDAO.readById(idPrestamo);
        if (prestamo == null) {
            throw new PrestamoException("El préstamo #" + idPrestamo + " ya no existe.");
        }
        if (prestamo.isDevuelto()) {
            throw new PrestamoException("El préstamo #" + idPrestamo + " ya fue devuelto.");
        }

        enTransaccion(conexion -> {
            if (!prestamoDAO.marcarDevuelto(conexion, idPrestamo)) {
                throw new PrestamoException("El préstamo #" + idPrestamo + " ya fue devuelto.");
            }
            libroDAO.reponerStock(conexion, prestamo.getIdLibro());
            return null;
        });

        int atraso = prestamo.diasAtraso(LocalDate.now());
        if (atraso > 0) {
            RegistroAtrasos.registrar(prestamo, atraso);
        }
        return atraso;
    }

    // elimina un prestamo; si seguia sin devolver, la copia vuelve al stock
    public synchronized void eliminarPrestamo(int idPrestamo) throws SQLException, PrestamoException {
        Prestamo prestamo = prestamoDAO.readById(idPrestamo);
        if (prestamo == null) {
            throw new PrestamoException("El préstamo #" + idPrestamo + " ya no existe.");
        }
        enTransaccion(conexion -> {
            if (!prestamo.isDevuelto()) {
                libroDAO.reponerStock(conexion, prestamo.getIdLibro());
            }
            prestamoDAO.delete(conexion, idPrestamo);
            return null;
        });
    }

    // commit si todo sale bien, rollback si algo falla
    // (limitacion conocida: como hay una sola conexion, una escritura de otra pestaña hecha justo
    // durante la transaccion se confirmaria o revertiria junto con ella; las ventanas son de milisegundos)
    private <T> T enTransaccion(Operacion<T> operacion) throws SQLException, PrestamoException {
        Connection conexion = DatabaseConnection.getInstance().getConnection();
        boolean autoCommitOriginal = conexion.getAutoCommit();
        try {
            conexion.setAutoCommit(false);
            T resultado = operacion.ejecutar(conexion);
            conexion.commit();
            return resultado;
        } catch (SQLException | PrestamoException | RuntimeException ex) {
            conexion.rollback();
            throw ex;
        } finally {
            conexion.setAutoCommit(autoCommitOriginal);
        }
    }
}