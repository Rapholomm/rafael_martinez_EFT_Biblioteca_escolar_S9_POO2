package controlador;

import dao.EstudianteDAO;
import dao.ReporteDAO;
import modelo.Estudiante;
import modelo.ResultadoReporte;

import java.sql.SQLException;
import java.util.List;

public class ReporteController {

    private static final int TOP_LIBROS = 10;

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    public ResultadoReporte librosMasPrestados() throws SQLException {
        return reporteDAO.librosMasPrestados(TOP_LIBROS);
    }

    public ResultadoReporte historialPorEstudiante(Integer idEstudiante) throws ValidacionException, SQLException {
        return reporteDAO.historialPorEstudiante(Validador.seleccion(idEstudiante, "un estudiante"));
    }

    public ResultadoReporte librosEnPrestamo() throws SQLException {
        return reporteDAO.librosEnPrestamo();
    }

    public List<Estudiante> estudiantes() throws SQLException {
        return estudianteDAO.readAll();
    }
}