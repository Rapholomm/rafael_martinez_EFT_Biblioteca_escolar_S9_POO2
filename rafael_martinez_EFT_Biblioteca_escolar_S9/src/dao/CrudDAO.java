package dao;

import java.sql.SQLException;
import java.util.List;

// operaciones CRUD que implementa cada DAO (T = la entidad del modelo)
public interface CrudDAO<T> {

    void create(T entidad) throws SQLException;

    List<T> readAll() throws SQLException;

    // devuelve null si no existe
    T readById(int id) throws SQLException;

    boolean update(T entidad) throws SQLException;

    boolean delete(int id) throws SQLException;
}
