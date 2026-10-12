package vista;

import controlador.PrestamoController;
import controlador.ValidacionException;
import interfaces.OperacionListener;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Prestamo;
import servicio.HiloPrestamo;
import servicio.PrestamoException;
import servicio.ServicioPrestamos;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;



//el biliotecario elige estudiante y libro
    // el estudainte solo presta a si mismo y ve sus prestamos
public class PanelPrestamos extends PanelBase {

    private static final String F_TODOS = "Todos";
    private static final String F_ACTIVOS = "En préstamo";
    private static final String F_ATRASADOS = "Atrasados";
    private static final String F_DEVUELTOS = "Devueltos";

    private final PrestamoController controlador = new PrestamoController();
    private final boolean esBibliotecario;
    private final Estudiante estudianteActual; // solo para el rol estudiante (puede ser null si no esta vinculado)

    private final JComboBox<ItemCombo> comboEstudiante = new JComboBox<>();
    private final JComboBox<ItemCombo> comboLibro = new JComboBox<>();
    private final JComboBox<String> comboFiltro = new JComboBox<>(new String[]{F_TODOS, F_ACTIVOS, F_ATRASADOS, F_DEVUELTOS});
    private final JButton btnPrestar = new JButton("Registrar préstamo");
    private final JButton btnDevolver = new JButton("Registrar devolución");
    private final JButton btnEliminar = new JButton("Eliminar préstamo");
    private final JButton btnActualizar = new JButton("Actualizar");
    private final JProgressBar barra = new JProgressBar();
    private final JLabel etiquetaEstado = new JLabel(" ");

    private final DefaultTableModel modeloTabla =
            crearModelo("ID", "Estudiante", "Libro", "Fecha préstamo", "Vencimiento", "Estado");
    private final JTable tabla = crearTabla(modeloTabla);

    public PanelPrestamos(boolean esBibliotecario, Estudiante estudianteActual) {
        this.esBibliotecario = esBibliotecario;
        this.estudianteActual = estudianteActual;

        JPanel norte = new JPanel();
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));

        JPanel formulario = new JPanel(new GridBagLayout());
        agregarCampo(formulario, "Estudiante:", comboEstudiante, 0, 0);
        agregarCampo(formulario, "Libro disponible:", comboLibro, 1, 0);
        norte.add(formulario);

        JLabel plazo = new JLabel("  Vencimiento automático: " + ServicioPrestamos.DIAS_PRESTAMO
                + " días desde hoy. Máximo " + ServicioPrestamos.MAX_PRESTAMOS_ACTIVOS + " libros sin devolver por estudiante.");
        plazo.setFont(plazo.getFont().deriveFont(Font.ITALIC, 11f));
        norte.add(plazo);

        JPanel botones = filaBotones(btnPrestar, btnDevolver);
        if (esBibliotecario) {
            botones.add(btnEliminar);
        }
        botones.add(btnActualizar);
        botones.add(new JLabel("  Mostrar:"));
        botones.add(comboFiltro);
        norte.add(botones);

        barra.setIndeterminate(true);
        barra.setVisible(false);
        JPanel estado = new JPanel(new BorderLayout(8, 0));
        estado.add(etiquetaEstado, BorderLayout.CENTER);
        estado.add(barra, BorderLayout.EAST);
        norte.add(estado);

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        anchos(tabla, 40, 180, 300, 100, 100, 100);

        // colores segun estado
        tabla.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel, boolean foco, int fila, int col) {
                Component c = super.getTableCellRendererComponent(t, valor, sel, foco, fila, col);
                if (!sel) {
                    if (Prestamo.ATRASADO.equals(valor)) {
                        c.setForeground(new Color(190, 30, 30));
                    } else if (Prestamo.DEVUELTO.equals(valor)) {
                        c.setForeground(new Color(30, 120, 50));
                    } else {
                        c.setForeground(t.getForeground());
                    }
                }
                return c;
            }
        });

        btnPrestar.addActionListener(e -> registrarPrestamo());
        btnDevolver.addActionListener(e -> registrarDevolucion());
        btnEliminar.addActionListener(e -> eliminar());
        btnActualizar.addActionListener(e -> recargarDatos());
        comboFiltro.addActionListener(e -> cargarTabla());

        if (!esBibliotecario && estudianteActual == null) {
            etiquetaEstado.setText("Tu usuario no está vinculado a un estudiante (mismo RUT o correo): no puedes pedir libros.");
            btnPrestar.setEnabled(false);
            btnDevolver.setEnabled(false);
        }
        recargarDatos();
    }



    private void registrarPrestamo() {
        try {
            HiloPrestamo hilo = controlador.registrarEnSegundoPlano(idDeCombo(comboEstudiante), idDeCombo(comboLibro),
                    new OperacionListener<>() {
                        // estos metodos corren en el hilo de trabajo: se vuelve a la EDT para tocar la interfaz
                        @Override
                        public void alExito(Prestamo p) {
                            SwingUtilities.invokeLater(() -> {
                                terminarProceso();
                                Dialogos.info(PanelPrestamos.this, "Préstamo #" + p.getId() + " registrado.\n"
                                        + p.getNombreEstudiante() + " debe devolver \"" + p.getTituloLibro()
                                        + "\" el " + p.getFechaVencimiento() + ".");
                                recargarDatos();
                            });
                        }

                        @Override
                        public void alFallar(String mensaje) {
                            SwingUtilities.invokeLater(() -> {
                                terminarProceso();
                                Dialogos.datoInvalido(PanelPrestamos.this, mensaje);
                                recargarDatos();
                            });
                        }
                    });
            iniciarProceso("Registrando préstamo en segundo plano (" + hilo.getName() + ")... la ventana sigue disponible.");
        } catch (ValidacionException ex) {
            Dialogos.datoInvalido(this, ex.getMessage());
        }
    }

    private void registrarDevolucion() {
        try {
            int atraso = controlador.devolver(idSeleccionado(tabla));
            if (atraso > 0) {
                Dialogos.info(this, "Devolución registrada CON ATRASO de " + atraso + " día(s).\n"
                        + "Se dejó constancia en " + servicio.RegistroAtrasos.ARCHIVO + ".");
            } else {
                Dialogos.info(this, "Devolución registrada a tiempo. El libro volvió al stock.");
            }
            recargarDatos();
        } catch (ValidacionException | PrestamoException ex) {
            Dialogos.datoInvalido(this, ex.getMessage());
            recargarDatos();
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "registrar la devolución", ex);
        }
    }

    private void eliminar() {
        Integer id = idSeleccionado(tabla);
        if (id != null && !Dialogos.confirmar(this, "¿Eliminar el préstamo #" + id + "?\n(Si no estaba devuelto, la copia vuelve al stock.)")) {
            return;
        }
        try {
            controlador.eliminar(id);
            Dialogos.info(this, "Préstamo eliminado.");
            recargarDatos();
        } catch (ValidacionException | PrestamoException ex) {
            Dialogos.datoInvalido(this, ex.getMessage());
            recargarDatos();
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "eliminar el préstamo", ex);
        }
    }

    //  estado  mientras corre el hilo

    private void iniciarProceso(String mensaje) {
        etiquetaEstado.setText(mensaje);
        barra.setVisible(true);
        btnPrestar.setEnabled(false);
    }

    private void terminarProceso() {
        etiquetaEstado.setText(" ");
        barra.setVisible(false);
        btnPrestar.setEnabled(esBibliotecario || estudianteActual != null);
    }

    // ---- carga de datos ----

    @Override
    public void recargarDatos() {
        cargarCombos();
        cargarTabla();
    }

    private void cargarCombos() {
        Integer estudianteAnterior = idDeCombo(comboEstudiante);
        Integer libroAnterior = idDeCombo(comboLibro);
        comboEstudiante.removeAllItems();
        comboLibro.removeAllItems();
        try {
            if (esBibliotecario) {
                for (Estudiante e : controlador.estudiantes()) {
                    comboEstudiante.addItem(new ItemCombo(e.getId(), e.getNombre() + " (" + e.getCurso() + ")"));
                }
                if (estudianteAnterior != null) {
                    seleccionarPorId(comboEstudiante, estudianteAnterior);
                }
            } else if (estudianteActual != null) {
                comboEstudiante.addItem(new ItemCombo(estudianteActual.getId(),
                        estudianteActual.getNombre() + " (" + estudianteActual.getCurso() + ")"));
            }
            comboEstudiante.setEnabled(esBibliotecario);

            for (Libro l : controlador.librosDisponibles()) {
                comboLibro.addItem(new ItemCombo(l.getId(), l.getTitulo() + " - " + l.getAutor() + " (stock: " + l.getStock() + ")"));
            }
            if (libroAnterior != null) {
                seleccionarPorId(comboLibro, libroAnterior);
            }
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "consultar estudiantes y libros", ex);
        }
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        try {
            List<Prestamo> prestamos;
            if (esBibliotecario) {
                prestamos = controlador.listar();
            } else if (estudianteActual != null) {
                prestamos = controlador.listarPorEstudiante(estudianteActual.getId());
            } else {
                return;
            }
            LocalDate hoy = LocalDate.now();
            String filtro = (String) comboFiltro.getSelectedItem();
            for (Prestamo p : prestamos) {
                String estado = p.getEstado(hoy);
                boolean mostrar = F_TODOS.equals(filtro)
                        || (F_ACTIVOS.equals(filtro) && !p.isDevuelto())
                        || (F_ATRASADOS.equals(filtro) && Prestamo.ATRASADO.equals(estado))
                        || (F_DEVUELTOS.equals(filtro) && p.isDevuelto());
                if (mostrar) {
                    modeloTabla.addRow(new Object[]{p.getId(), p.getNombreEstudiante(), p.getTituloLibro(),
                            p.getFechaPrestamo(), p.getFechaVencimiento(), estado});
                }
            }
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "consultar los préstamos", ex);
        }
    }
}
