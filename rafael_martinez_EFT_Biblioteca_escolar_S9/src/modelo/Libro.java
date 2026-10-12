package modelo;

import interfaces.Prestable;

public class Libro implements Prestable {

    private int id;
    private String titulo;
    private String autor;
    private String isbn;
    private String editorial;
    private int stock;
    private int idCategoria;
    private String nombreCategoria; // solo para mostrar en tablas (viene del JOIN)

    public Libro(String titulo, String autor, String isbn, String editorial, int stock, int idCategoria) {
        this(0, titulo, autor, isbn, editorial, stock, idCategoria);
    }

    public Libro(int id, String titulo, String autor, String isbn, String editorial, int stock, int idCategoria) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.editorial = editorial;
        this.stock = stock;
        this.idCategoria = idCategoria;
    }

    @Override
    public boolean estaDisponible() {
        return stock > 0;
    }

    @Override
    public void descontarStock() {
        if (stock <= 0) {
            throw new IllegalStateException("El libro \"" + titulo + "\" no tiene stock.");
        }
        stock--;
    }

    @Override
    public void reponerStock() {
        stock++;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getEditorial() {
        return editorial;
    }

    public int getStock() {
        return stock;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    @Override
    public String toString() {
        return titulo + " - " + autor;
    }
}
