package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;



//Clase abstracta
public abstract class PanelBase extends JPanel {

    protected PanelBase() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    public abstract void recargarDatos();

    protected static DefaultTableModel crearModelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    protected static JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(22);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        return tabla;
    }

    // anchos preferidos de las columnas (la primera, el ID, suele ir angosta)
    protected static void anchos(JTable tabla, int... anchos) {
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    // valor de la fila seleccionada (null si no hay), considerando que la tabla puede estar ordenada
    protected static Object valorSeleccionado(JTable tabla, int columna) {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? null : tabla.getModel().getValueAt(tabla.convertRowIndexToModel(fila), columna);
    }

    protected static Integer idSeleccionado(JTable tabla) {
        Object valor = valorSeleccionado(tabla, 0);
        return valor == null ? null : (Integer) valor;
    }

    protected static void agregarCampo(JPanel panel, String etiqueta, JComponent campo, int columna, int fila) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 6, 3, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = columna * 2;
        c.gridy = fila;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = columna * 2 + 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panel.add(campo, c);
    }

    protected static void seleccionarPorId(JComboBox<ItemCombo> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getId() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    protected static Integer idDeCombo(JComboBox<ItemCombo> combo) {
        ItemCombo item = (ItemCombo) combo.getSelectedItem();
        return item == null ? null : item.getId();
    }

    protected static JPanel filaBotones(JButton... botones) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        for (JButton boton : botones) {
            fila.add(boton);
        }
        return fila;
    }
}