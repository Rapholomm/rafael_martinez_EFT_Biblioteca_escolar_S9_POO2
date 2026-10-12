package vista;

import controlador.ReporteController;
import controlador.ValidacionException;
import modelo.Estudiante;
import modelo.ResultadoReporte;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class PanelReportes extends PanelBase {

    private static final String R_MAS_PRESTADOS = "Libros más prestados";
    private static final String R_HISTORIAL = "Historial por estudiante";
    private static final String R_EN_PRESTAMO = "Libros actualmente en préstamo";

    private final ReporteController controlador = new ReporteController();

    private final JComboBox<String> comboReporte =
            new JComboBox<>(new String[]{R_MAS_PRESTADOS, R_HISTORIAL, R_EN_PRESTAMO});
    private final JLabel etiquetaEstudiante = new JLabel("Estudiante:");
    private final JComboBox<ItemCombo> comboEstudiante = new JComboBox<>();
    private final JLabel etiquetaResumen = new JLabel(" ");
    private final DefaultTableModel modeloTabla = crearModelo();
    private final JTable tabla = crearTabla(modeloTabla);

    public PanelReportes() {
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filtros.add(new JLabel("Reporte:"));
        filtros.add(comboReporte);
        filtros.add(etiquetaEstudiante);
        filtros.add(comboEstudiante);
        JButton btnGenerar = new JButton("Generar");
        filtros.add(btnGenerar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(filtros, BorderLayout.NORTH);
        norte.add(etiquetaResumen, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        comboReporte.addActionListener(e -> actualizarVisibilidad());
        btnGenerar.addActionListener(e -> generar());
        actualizarVisibilidad();
        recargarDatos();
    }

    private void actualizarVisibilidad() {
        boolean esHistorial = R_HISTORIAL.equals(comboReporte.getSelectedItem());
        etiquetaEstudiante.setVisible(esHistorial);
        comboEstudiante.setVisible(esHistorial);
    }

    private void generar() {
        try {
            String reporte = (String) comboReporte.getSelectedItem();
            ResultadoReporte resultado;
            if (R_HISTORIAL.equals(reporte)) {
                resultado = controlador.historialPorEstudiante(idDeCombo(comboEstudiante));
            } else if (R_EN_PRESTAMO.equals(reporte)) {
                resultado = controlador.librosEnPrestamo();
            } else {
                resultado = controlador.librosMasPrestados();
            }
            mostrar(resultado);
        } catch (ValidacionException ex) {
            Dialogos.datoInvalido(this, ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "generar el reporte", ex);
        }
    }

    private void mostrar(ResultadoReporte resultado) {
        modeloTabla.setColumnIdentifiers(resultado.columnas());
        modeloTabla.setRowCount(0);
        for (Object[] fila : resultado.filas()) {
            modeloTabla.addRow(fila);
        }
        etiquetaResumen.setText("  " + resultado.titulo() + ": " + resultado.filas().size() + " registro(s)");
    }

    @Override
    public void recargarDatos() {
        Integer anterior = idDeCombo(comboEstudiante);
        comboEstudiante.removeAllItems();
        try {
            for (Estudiante e : controlador.estudiantes()) {
                comboEstudiante.addItem(new ItemCombo(e.getId(), e.getNombre() + " (" + e.getCurso() + ")"));
            }
            if (anterior != null) {
                seleccionarPorId(comboEstudiante, anterior);
            }
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "consultar los estudiantes", ex);
        }
    }
}
