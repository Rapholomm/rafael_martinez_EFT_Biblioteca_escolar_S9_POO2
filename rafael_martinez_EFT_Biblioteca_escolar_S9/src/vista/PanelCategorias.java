package vista;

import controlador.CategoriaController;
import controlador.ValidacionException;
import modelo.Categoria;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class PanelCategorias extends PanelBase {

    private final CategoriaController controlador = new CategoriaController();

    private final JTextField campoNombre = new JTextField(25);
    private final DefaultTableModel modeloTabla = crearModelo("ID", "Nombre");
    private final JTable tabla = crearTabla(modeloTabla);

    public PanelCategorias() {
        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);
        JButton btnRegistrar = new JButton("Registrar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");
        formulario.add(btnRegistrar);
        formulario.add(btnEditar);
        formulario.add(btnEliminar);
        formulario.add(btnLimpiar);
        add(formulario, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        anchos(tabla, 50, 300);

        btnRegistrar.addActionListener(e -> registrar());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                campoNombre.setText((String) valorSeleccionado(tabla, 1));
            }
        });
        recargarDatos();
    }

    private void registrar() {
        try {
            Categoria c = controlador.registrar(campoNombre.getText());
            Dialogos.info(this, "Categoría #" + c.getId() + " (" + c.getNombre() + ") registrada.");
            limpiar();
            recargarDatos();
        } catch (ValidacionException ex) {
            Dialogos.datoInvalido(this, ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "registrar la categoría", ex);
        }
    }

    private void editar() {
        try {
            if (controlador.modificar(idSeleccionado(tabla), campoNombre.getText())) {
                Dialogos.info(this, "Categoría actualizada.");
            } else {
                Dialogos.datoInvalido(this, "La categoría ya no existe.");
            }
            limpiar();
            recargarDatos();
        } catch (ValidacionException ex) {
            Dialogos.datoInvalido(this, ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "actualizar la categoría", ex);
        }
    }

    private void eliminar() {
        Integer id = idSeleccionado(tabla);
        if (id != null && !Dialogos.confirmar(this, "¿Eliminar la categoría #" + id + "?")) {
            return;
        }
        try {
            if (controlador.eliminar(id)) {
                Dialogos.info(this, "Categoría eliminada.");
            }
            limpiar();
            recargarDatos();
        } catch (ValidacionException ex) {
            Dialogos.datoInvalido(this, ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "eliminar la categoría", ex);
        }
    }

    private void limpiar() {
        campoNombre.setText("");
        tabla.clearSelection();
        campoNombre.requestFocus();
    }

    @Override
    public void recargarDatos() {
        modeloTabla.setRowCount(0);
        try {
            for (Categoria c : controlador.listar()) {
                modeloTabla.addRow(new Object[]{c.getId(), c.getNombre()});
            }
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "consultar las categorías", ex);
        }
    }
}