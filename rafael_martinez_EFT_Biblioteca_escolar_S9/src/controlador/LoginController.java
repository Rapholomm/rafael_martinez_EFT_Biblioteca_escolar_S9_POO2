package controlador;

import dao.UsuarioDAO;
import modelo.Usuario;

import java.sql.SQLException;

// controlador del modulo de autenticacion
public class LoginController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /*
     *Valida que el usuario exista en la BD y que la contraseña coincida.

     */
    public Usuario autenticar(String identificador, String contrasena) throws ValidacionException, SQLException {
        String id = Validador.texto(identificador, "correo o RUT", 100);
        if (contrasena == null || contrasena.isEmpty()) {
            throw new ValidacionException("El campo contraseña es obligatorio.");
        }
        Usuario usuario = usuarioDAO.buscarPorCorreoORut(id);
        if (usuario == null || !usuario.validarCredenciales(contrasena)) {
            throw new ValidacionException("Usuario o contraseña incorrectos.");
        }
        return usuario;
    }
}
