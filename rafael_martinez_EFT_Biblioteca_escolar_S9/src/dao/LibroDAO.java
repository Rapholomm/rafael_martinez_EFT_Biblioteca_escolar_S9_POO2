package dao;

import modelo.Libro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO implements CrudDAO<Libro> {

    private static final String SELECT_BASE =
            "SELECT l.id, l.titulo, l.autor, l.isbn, l.editorial, l.stock, l.id_categoria, c.nombre AS categoria "
                    + "FROM libros l LEFT JOIN categorias c ON c.id = l.id_categoria";

    @Override
    public void create(Libro libro) throws SQLException {
        String sql = "INSERT INTO libros (titulo, autor, isbn, editorial, stock, id_categoria) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, libro.getTitulo());
            statement.setString(2, libro.getAutor());
            statement.setString(3, libro.getIsbn());
            statement.setString(4, libro.getEditorial());
            statement.setInt(5, libro.getStock());
            statement.setInt(6, libro.getIdCategoria());
            statement.executeUpdate();
            try (ResultSet claves = statement.getGeneratedKeys()) {
                if (claves.next()) {
                    libro.setId(claves.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Libro> readAll() throws SQLException {
        return consultar(SELECT_BASE + " ORDER BY l.id");
    }

    // libros con al menos una copia, para el combo de prestamos
    public List<Libro> readDisponibles() throws SQLException {
        return consultar(SELECT_BASE + " WHERE l.stock > 0 ORDER BY l.titulo");
    }

    // busqueda por titulo, autor o isbn
    public List<Libro> buscar(String texto) throws SQLException {
        String sql = SELECT_BASE + " WHERE l.titulo LIKE ? OR l.autor LIKE ? OR l.isbn LIKE ? ORDER BY l.id";
        String patron = "%" + texto + "%";
        return consultar(sql, patron, patron, patron);
    }

    @Override
    public Libro readById(int id) throws SQLException {
        List<Libro> resultado = consultar(SELECT_BASE + " WHERE l.id = ?", String.valueOf(id));
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public boolean update(Libro libro) throws SQLException {
        String sql = "UPDATE libros SET titulo = ?, autor = ?, isbn = ?, editorial = ?, stock = ?, id_categoria = ? WHERE id = ?";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, libro.getTitulo());
            statement.setString(2, libro.getAutor());
            statement.setString(3, libro.getIsbn());
            statement.setString(4, libro.getEditorial());
            statement.setInt(5, libro.getStock());
            statement.setInt(6, libro.getIdCategoria());
            statement.setInt(7, libro.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM libros WHERE id = ?";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }


    public synchronized boolean descontarStock(Connection conexion, int idLibro) throws SQLException {
        String sql = "UPDATE libros SET stock = stock - 1 WHERE id = ? AND stock > 0";
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, idLibro);
            return statement.executeUpdate() > 0;
        }
    }

    // suma 1 al stock (devolucion o prestamo eliminado)
    public synchronized boolean reponerStock(Connection conexion, int idLibro) throws SQLException {
        String sql = "UPDATE libros SET stock = stock + 1 WHERE id = ?";
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, idLibro);
            return statement.executeUpdate() > 0;
        }
    }

    private List<Libro> consultar(String sql, String... parametros) throws SQLException {
        List<Libro> libros = new ArrayList<>();
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                statement.setString(i + 1, parametros[i]);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Libro libro = new Libro(rs.getInt("id"), rs.getString("titulo"), rs.getString("autor"),
                            rs.getString("isbn"), rs.getString("editorial"), rs.getInt("stock"),
                            rs.getInt("id_categoria"));
                    libro.setNombreCategoria(rs.getString("categoria"));
                    libros.add(libro);
                }
            }
        }
        return libros;
    }
}