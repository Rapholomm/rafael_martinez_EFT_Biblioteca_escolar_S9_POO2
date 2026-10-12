package controlador;

import dao.EstudianteDAO;
import dao.UsuarioDAO;
import modelo.Estudiante;
import modelo.Rol;
import modelo.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador de estudiantes. Al registrar un estudiante se puede crear tambien su usuario de acceso
 * (rol estudiante) con el mismo RUT y correo, asi queda vinculado a su historial de prestamos.
 */
public class EstudianteController {

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public List<Estudiante> listar() throws SQLException {
        return estudianteDAO.readAll();
    }

    // ficha de estudiante que corresponde a un usuario (por rut o correo); null si no tiene
    public Estudiante buscarVinculado(Usuario usuario) throws SQLException {
        return estudianteDAO.buscarPorRutOCorreo(usuario.getRut(), usuario.getCorreo());
    }

    public Estudiante registrar(String nombre, String rut, String curso, String correo, String contrasena)
            throws ValidacionException, SQLException {
        Estudiante estudiante = construir(0, nombre, rut, curso, correo);
        String clave = contrasena == null ? "" : contrasena.trim();
        if (!clave.isEmpty()) {
            validarContrasena(clave);
        }
        estudianteDAO.create(estudiante);
        if (!clave.isEmpty()) {
            usuarioDAO.create(new Usuario(estudiante.getNombre(), estudiante.getRut(), estudiante.getCorreo(),
                    clave, Rol.ESTUDIANTE));
        }
        return estudiante;
    }

    // si se escribe una contraseña nueva, se crea o actualiza el usuario vinculado
    public boolean modificar(Integer id, String nombre, String rut, String curso, String correo, String contrasena)
            throws ValidacionException, SQLException {
        int idEstudiante = Validador.seleccion(id, "un estudiante de la tabla");
        Estudiante nuevo = construir(idEstudiante, nombre, rut, curso, correo);
        String clave = contrasena == null ? "" : contrasena.trim();
        if (!clave.isEmpty()) {
            validarContrasena(clave);
        }

        Estudiante anterior = estudianteDAO.readById(idEstudiante);
        if (anterior == null || !estudianteDAO.update(nuevo)) {
            return false;
        }
        Usuario usuario = usuarioDAO.buscarPorRut(anterior.getRut());
        if (usuario != null && usuario.getRol() == Rol.ESTUDIANTE) {
            usuario.setNombre(nuevo.getNombre());
            usuario.setRut(nuevo.getRut());
            usuario.setCorreo(nuevo.getCorreo());
            if (!clave.isEmpty()) {
                usuario.setContrasena(clave);
            }
            usuarioDAO.update(usuario);
        } else if (usuario == null && !clave.isEmpty()) {
            usuarioDAO.create(new Usuario(nuevo.getNombre(), nuevo.getRut(), nuevo.getCorreo(), clave, Rol.ESTUDIANTE));
        }
        return true;
    }

    public boolean eliminar(Integer id) throws ValidacionException, SQLException {
        int idEstudiante = Validador.seleccion(id, "un estudiante de la tabla");
        Estudiante estudiante = estudianteDAO.readById(idEstudiante);
        boolean eliminado = estudianteDAO.delete(idEstudiante);
        // el usuario de acceso se borra solo si el estudiante se pudo eliminar (si tiene prestamos, la FK lo impide)
        if (eliminado && estudiante != null) {
            Usuario usuario = usuarioDAO.buscarPorRut(estudiante.getRut());
            if (usuario != null && usuario.getRol() == Rol.ESTUDIANTE) {
                usuarioDAO.delete(usuario.getId());
            }
        }
        return eliminado;
    }

    private Estudiante construir(int id, String nombre, String rut, String curso, String correo) throws ValidacionException {
        return new Estudiante(id,
                Validador.nombrePersona(nombre, "nombre"),
                Validador.rut(rut),
                Validador.texto(curso, "curso", 20),
                Validador.correo(correo));
    }

    private void validarContrasena(String clave) throws ValidacionException {
        if (clave.length() < 4 || clave.length() > 100) {
            throw new ValidacionException("La contraseña debe tener entre 4 y 100 caracteres.");
        }
    }
}
