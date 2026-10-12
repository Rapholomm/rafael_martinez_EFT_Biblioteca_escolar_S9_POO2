package controlador;

import dao.EstudianteDAO;
import dao.LibroDAO;
import dao.PrestamoDAO;
import interfaces.OperacionListener;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Prestamo;
import servicio.HiloPrestamo;
import servicio.PrestamoException;
import servicio.ServicioPrestamos;

import java.sql.SQLException;
import java.util.List;

public class PrestamoController {

    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final LibroDAO libroDAO = new LibroDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final ServicioPrestamos servicio = ServicioPrestamos.getInstance();

    public List<Prestamo> listar() throws SQLException {
        return prestamoDAO.readAll();
    }

    public List<Prestamo> listarPorEstudiante(int idEstudiante) throws SQLException {
        return prestamoDAO.readByEstudiante(idEstudiante);
    }

    public List<Libro> librosDisponibles() throws SQLException {
        return libroDAO.readDisponibles();
    }

    public List<Estudiante> estudiantes() throws SQLException {
        return estudianteDAO.readAll();
    }

    /**
     * Valida y lanza el hilo que registra el prestamo. Vuelve de inmediato; el resultado
     * llega despues por el listener (desde el hilo de trabajo).
     */
    public HiloPrestamo registrarEnSegundoPlano(Integer idEstudiante, Integer idLibro,
                                                OperacionListener<Prestamo> listener) throws ValidacionException {
        int estudiante = Validador.seleccion(idEstudiante, "un estudiante");
        int libro = Validador.seleccion(idLibro, "un libro");
        HiloPrestamo hilo = new HiloPrestamo(estudiante, libro, listener);
        hilo.start();
        return hilo;
    }

    // devuelve los dias de atraso (0 = a tiempo)
    public int devolver(Integer idPrestamo) throws ValidacionException, PrestamoException, SQLException {
        return servicio.registrarDevolucion(Validador.seleccion(idPrestamo, "un préstamo de la tabla"));
    }

    public void eliminar(Integer idPrestamo) throws ValidacionException, PrestamoException, SQLException {
        servicio.eliminarPrestamo(Validador.seleccion(idPrestamo, "un préstamo de la tabla"));
    }
}
