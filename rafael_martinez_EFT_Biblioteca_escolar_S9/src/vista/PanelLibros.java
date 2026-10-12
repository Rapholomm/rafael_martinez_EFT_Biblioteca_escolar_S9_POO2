package vista;

import controlador.LibroController;
import controlador.ValidacionException;
import modelo.Categoria;
import modelo.Libro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;



// gestion libros
// el bibliotecario ve el formulario completo (crud)
public class PanelLibros extends PanelBase {

    private final LibroController controlador = new LibroController();
    private final boolean puedeEditar;

    private final JTextField campoTitulo = new JTextField(18);
    private final JTextField campoAutor = new JTextField(18);
    private final JTextField campoIsbn = new JTextField(14);
    private final JTextField campoEditorial = new JTextField(18);
    private final JTextField campoStock = new JTextField(5);
    private final JComboBox<ItemCombo> comboCategoria = new JComboBox<>();
    private final JTextField campoBusqueda = new JTextField(20);

    private final DefaultTableModel modeloTabla =
            crearModelo("ID", "Título", "Autor", "ISBN", "Editorial", "Categoría", "Stock");
    private final JTable tabla = crearTabla(modeloTabla);

    public PanelLibros(boolean puedeEditar) {
        this.puedeEditar = puedeEditar;

        JPanel norte = new JPanel();
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));

        if (puedeEditar) {
            JPanel formulario = new JPanel(new GridBagLayout());
            agregarCampo(formulario, "Título:", campoTitulo, 0, 0);
            agregarCampo(formulario, "Autor:", campoAutor, 1, 0);
            agregarCampo(formulario, "ISBN:", campoIsbn, 2, 0);
            agregarCampo(formulario, "Editorial:", campoEditorial, 0, 1);
            agregarCampo(formulario, "Stock:", campoStock, 1, 1);
            agregarCampo(formulario, "Categoría:", comboCategoria, 2, 1);
            norte.add(formulario);

            JButton btnRegistrar = new JButton("Registrar");
            JButton btnEditar = new JButton("Editar");
            JButton btnEliminar = new JButton("Eliminar");
            JButton btnLimpiar = new JButton("Limpiar");
            norte.add(filaBotones(btnRegistrar, btnEditar, btnEliminar, btnLimpiar));

            btnRegistrar.addActionListener(e -> registrar());
            btnEditar.addActionListener(e -> editar());
            btnEliminar.addActionListener(e -> eliminar());
            btnLimpiar.addActionListener(e -> limpiar());
        }

        JButton btnBuscar = new JButton("Buscar");
        JButton btnTodos = new JButton("Ver todos");
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        busqueda.add(new JLabel("Buscar (título, autor o ISBN):"));
        busqueda.add(campoBusqueda);
        busqueda.add(btnBuscar);
        busqueda.add(btnTodos);
        norte.add(busqueda);

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        anchos(tabla, 40, 230, 170, 110, 110, 120, 50);

        btnBuscar.addActionListener(e -> cargarTabla());
        campoBusqueda.addActionListener(e -> cargarTabla());
        btnTodos.addActionListener(e -> {
            campoBusqueda.setText("");
            cargarTabla();
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarFilaEnFormulario();
            }
        });

        recargarDatos();
    }

    private void registrar() {
        try {
            Libro libro = controlador.registrar(campoTitulo.getText(), campoAutor.getText(), campoIsbn.getText(),
                    campoEditorial.getText(), campoStock.getText(), idDeCombo(comboCategoria));
            Dialogos.info(this, "Libro #" + libro.getId() + " registrado.");
            limpiar();
            cargarTabla();
        } catch (ValidacionException ex) {
            Dialogos.datoInvalido(this, ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "registrar el libro", ex);
        }
    }

    private void editar() {
        try {
            if (controlador.modificar(idSeleccionado(tabla), campoTitulo.getText(), campoAutor.getText(),
                    campoIsbn.getText(), campoEditorial.getText(), campoStock.getText(), idDeCombo(comboCategoria))) {
                Dialogos.info(this, "Libro actualizado.");
            } else {
                Dialogos.datoInvalido(this, "El libro ya no existe.");
            }
            limpiar();
            cargarTabla();
        } catch (ValidacionException ex) {
            Dialogos.datoInvalido(this, ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "actualizar el libro", ex);
        }
    }

    private void eliminar() {
        Integer id = idSeleccionado(tabla);
        if (id != null && !Dialogos.confirmar(this, "¿Eliminar el libro #" + id + "?")) {
            return;
        }
        try {
            if (controlador.eliminar(id)) {
                Dialogos.info(this, "Libro eliminado.");
            }
            limpiar();
            cargarTabla();
        } catch (ValidacionException ex) {
            Dialogos.datoInvalido(this, ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "eliminar el libro", ex);
        }
    }

    private void cargarFilaEnFormulario() {
        if (!puedeEditar || tabla.getSelectedRow() < 0) {
            return;
        }
        campoTitulo.setText((String) valorSeleccionado(tabla, 1));
        campoAutor.setText((String) valorSeleccionado(tabla, 2));
        campoIsbn.setText((String) valorSeleccionado(tabla, 3));
        campoEditorial.setText((String) valorSeleccionado(tabla, 4));
        campoStock.setText(String.valueOf(valorSeleccionado(tabla, 6)));
        String categoria = (String) valorSeleccionado(tabla, 5);
        for (int i = 0; i < comboCategoria.getItemCount(); i++) {
            if (comboCategoria.getItemAt(i).toString().equals(categoria)) {
                comboCategoria.setSelectedIndex(i);
                break;
            }
        }
    }

    private void limpiar() {
        campoTitulo.setText("");
        campoAutor.setText("");
        campoIsbn.setText("");
        campoEditorial.setText("");
        campoStock.setText("");
        tabla.clearSelection();
        campoTitulo.requestFocus();
    }

    private void cargarCategorias() {
        if (!puedeEditar) {
            return;
        }
        Integer anterior = idDeCombo(comboCategoria);
        comboCategoria.removeAllItems();
        try {
            for (Categoria c : controlador.listarCategorias()) {
                comboCategoria.addItem(new ItemCombo(c.getId(), c.getNombre()));
            }
            if (anterior != null) {
                seleccionarPorId(comboCategoria, anterior);
            }
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "consultar las categorías", ex);
        }
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        try {
            List<Libro> libros = controlador.buscar(campoBusqueda.getText());
            for (Libro l : libros) {
                modeloTabla.addRow(new Object[]{l.getId(), l.getTitulo(), l.getAutor(), l.getIsbn(),
                        l.getEditorial(), l.getNombreCategoria(), l.getStock()});
            }
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "consultar los libros", ex);
        }
    }

    @Override
    public void recargarDatos() {
        cargarCategorias();
        cargarTabla();
    }
}
