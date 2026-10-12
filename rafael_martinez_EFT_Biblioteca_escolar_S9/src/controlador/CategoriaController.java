package controlador;

import dao.CategoriaDAO;
import modelo.Categoria;

import java.sql.SQLException;
import java.util.List;

public class CategoriaController {

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public List<Categoria> listar() throws SQLException {
        return categoriaDAO.readAll();
    }

    public Categoria registrar(String nombre) throws ValidacionException, SQLException {
        Categoria categoria = new Categoria(Validador.texto(nombre, "nombre", 100));
        categoriaDAO.create(categoria);
        return categoria;
    }

    public boolean modificar(Integer id, String nombre) throws ValidacionException, SQLException {
        int idCategoria = Validador.seleccion(id, "una categoría de la tabla");
        return categoriaDAO.update(new Categoria(idCategoria, Validador.texto(nombre, "nombre", 100)));
    }

    public boolean eliminar(Integer id) throws ValidacionException, SQLException {
        return categoriaDAO.delete(Validador.seleccion(id, "una categoría de la tabla"));
    }
}
