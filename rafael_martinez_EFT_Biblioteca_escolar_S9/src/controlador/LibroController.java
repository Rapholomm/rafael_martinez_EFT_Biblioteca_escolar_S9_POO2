package controlador;

import dao.CategoriaDAO;
import dao.LibroDAO;
import modelo.Categoria;
import modelo.Libro;

import java.sql.SQLException;
import java.util.List;

public class LibroController {

    private static final int STOCK_MAXIMO = 1000;

    private final LibroDAO libroDAO = new LibroDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public List<Libro> listar() throws SQLException {
        return libroDAO.readAll();
    }

    public List<Libro> buscar(String texto) throws SQLException {
        String filtro = texto == null ? "" : texto.trim();
        return filtro.isEmpty() ? libroDAO.readAll() : libroDAO.buscar(filtro);
    }

    public List<Categoria> listarCategorias() throws SQLException {
        return categoriaDAO.readAll();
    }

    public Libro registrar(String titulo, String autor, String isbn, String editorial, String stock,
                           Integer idCategoria) throws ValidacionException, SQLException {
        Libro libro = construir(0, titulo, autor, isbn, editorial, stock, idCategoria);
        libroDAO.create(libro);
        return libro;
    }

    public boolean modificar(Integer id, String titulo, String autor, String isbn, String editorial, String stock,
                             Integer idCategoria) throws ValidacionException, SQLException {
        int idLibro = Validador.seleccion(id, "un libro de la tabla");
        return libroDAO.update(construir(idLibro, titulo, autor, isbn, editorial, stock, idCategoria));
    }

    public boolean eliminar(Integer id) throws ValidacionException, SQLException {
        return libroDAO.delete(Validador.seleccion(id, "un libro de la tabla"));
    }

    // valida todos los campos y arma el objeto
    private Libro construir(int id, String titulo, String autor, String isbn, String editorial, String stock,
                            Integer idCategoria) throws ValidacionException {
        return new Libro(id,
                Validador.texto(titulo, "título", 200),
                Validador.nombrePersona(autor, "autor"),
                Validador.isbn(isbn),
                Validador.texto(editorial, "editorial", 100),
                Validador.entero(stock, "stock", 0, STOCK_MAXIMO),
                Validador.seleccion(idCategoria, "una categoría"));
    }
}
