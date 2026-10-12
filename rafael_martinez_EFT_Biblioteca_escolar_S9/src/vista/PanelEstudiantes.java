package vista;

import controlador.EstudianteController;
import controlador.ValidacionException;
import modelo.Estudiante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class PanelEstudiantes extends PanelBase {

    private final EstudianteController controlador = new EstudianteController();

    private final JTextField campoNombre = new JTextField(18);
    private final JTextField campoRut = new JTextField(12);
    private final JTextField campoCurso = new JTextField(12);
    private final JTextField campoCorreo = new JTextField(20);
    private final JPasswordField campoContrasena = new JPasswordField(12);

    private final DefaultTableModel modeloTabla = crearModelo("ID", "Nombre", "RUT", "Curso", "Correo");
    private final JTable tabla = crearTabla(modeloTabla);

    public PanelEstudiantes() {
        JPanel norte = new JPanel();
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));

        JPanel formulario = new JPanel(new GridBagLayout());
        agregarCampo(formulario, "Nombre:", campoNombre, 0, 0);
        agregarCampo(formulario, "RUT:", campoRut, 1, 0);
        agregarCampo(formulario, "Curso:", campoCurso, 2, 0);
        agregarCampo(formulario, "Correo:", campoCorreo, 0, 1);
        agregarCampo(formulario, "Contraseña:", campoContrasena, 1, 1);
        norte.add(formulario);

        JLabel ayuda = new JLabel("  La contraseña es opcional: si la escribes, el estudiante también queda como usuario del sistema.");
        ayuda.setFont(ayuda.getFont().deriveFont(Font.ITALIC, 11f));
        norte.add(ayuda);

        JButton btnRegistrar = new JButton("Registrar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");
        norte.add(filaBotones(btnRegistrar, btnEditar, btnEliminar, btnLimpiar));

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        anchos(tabla, 40, 200, 110, 100, 230);

        btnRegistrar.addActionListener(e -> registrar());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                campoNombre.setText((String) valorSeleccionado(tabla, 1));
                campoRut.setText((String) valorSeleccionado(tabla, 2));
                campoCurso.setText((String) valorSeleccionado(tabla, 3));
                campoCorreo.setText((String) valorSeleccionado(tabla, 4));
                campoContrasena.setText("");
            }
        });
        recargarDatos();
    }

    private String contrasena() {
        return new String(campoContrasena.getPassword());
    }

    private void registrar() {
        try {
            Estudiante e = controlador.registrar(campoNombre.getText(), campoRut.getText(), campoCurso.getText(),
                    campoCorreo.getText(), contrasena());
            Dialogos.info(this, "Estudiante #" + e.getId() + " (" + e.getNombre() + ") registrado.");
            limpiar();
            recargarDatos();
        } catch (ValidacionException ex) {
            Dialogos.datoInvalido(this, ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "registrar el estudiante", ex);
        }
    }

    private void editar() {
        try {
            if (controlador.modificar(idSeleccionado(tabla), campoNombre.getText(), campoRut.getText(),
                    campoCurso.getText(), campoCorreo.getText(), contrasena())) {
                Dialogos.info(this, "Estudiante actualizado.");
            } else {
                Dialogos.datoInvalido(this, "El estudiante ya no existe.");
            }
            limpiar();
            recargarDatos();
        } catch (ValidacionException ex) {
            Dialogos.datoInvalido(this, ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "actualizar el estudiante", ex);
        }
    }

    private void eliminar() {
        Integer id = idSeleccionado(tabla);
        if (id != null && !Dialogos.confirmar(this, "¿Eliminar al estudiante #" + id + "?")) {
            return;
        }
        try {
            if (controlador.eliminar(id)) {
                Dialogos.info(this, "Estudiante eliminado.");
            }
            limpiar();
            recargarDatos();
        } catch (ValidacionException ex) {
            Dialogos.datoInvalido(this, ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "eliminar el estudiante", ex);
        }
    }

    private void limpiar() {
        campoNombre.setText("");
        campoRut.setText("");
        campoCurso.setText("");
        campoCorreo.setText("");
        campoContrasena.setText("");
        tabla.clearSelection();
        campoNombre.requestFocus();
    }

    @Override
    public void recargarDatos() {
        modeloTabla.setRowCount(0);
        try {
            for (Estudiante e : controlador.listar()) {
                modeloTabla.addRow(new Object[]{e.getId(), e.getNombre(), e.getRut(), e.getCurso(), e.getCorreo()});
            }
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "consultar los estudiantes", ex);
        }
    }
}