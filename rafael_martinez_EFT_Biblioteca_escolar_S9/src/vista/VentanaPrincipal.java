package vista;

import controlador.EstudianteController;
import modelo.Estudiante;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana principal (JFrame). Segun el rol del usuario arma las pestañas:
 *  - bibliotecario: libros, categorias, estudiantes, prestamos y reportes
 *  - estudiante: consulta de libros y sus propios prestamos/devoluciones
 * El menu superior da acceso rapido a cada modulo y permite cerrar sesion.
 */
public class VentanaPrincipal extends JFrame {

    private final Usuario usuario;
    private final JTabbedPane pestanas = new JTabbedPane();
    private final List<PanelBase> paneles = new ArrayList<>();

    public VentanaPrincipal(Usuario usuario) {
        super("Biblioteca Escolar - Sistema de gestión");
        this.usuario = usuario;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 560);
        setLocationRelativeTo(null);

        armarPestanas();
        setJMenuBar(crearMenu());

        JLabel barraEstado = new JLabel("  Sesión: " + usuario.getNombre() + "  |  Rol: " + usuario.getRol().valorBD());
        barraEstado.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(pestanas, BorderLayout.CENTER);
        add(barraEstado, BorderLayout.SOUTH);

        // cada vez que se cambia de pestaña se recargan sus datos (asi ven los cambios de las otras)
        pestanas.addChangeListener(e -> {
            int indice = pestanas.getSelectedIndex();
            if (indice >= 0) {
                paneles.get(indice).recargarDatos();
            }
        });
    }

    private void armarPestanas() {
        if (usuario.esBibliotecario()) {
            agregar("Libros", new PanelLibros(true));
            agregar("Categorías", new PanelCategorias());
            agregar("Estudiantes", new PanelEstudiantes());
            agregar("Préstamos y devoluciones", new PanelPrestamos(true, null));
            agregar("Reportes", new PanelReportes());
        } else {
            Estudiante vinculado = null;
            try {
                vinculado = new EstudianteController().buscarVinculado(usuario);
            } catch (SQLException ex) {
                Dialogos.errorSQL(this, "buscar tu ficha de estudiante", ex);
            }
            agregar("Consulta de libros", new PanelLibros(false));
            agregar("Mis préstamos y devoluciones", new PanelPrestamos(false, vinculado));
        }
    }

    private void agregar(String titulo, PanelBase panel) {
        pestanas.addTab(titulo, panel);
        paneles.add(panel);
    }

    private JMenuBar crearMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu menuModulos = new JMenu("Módulos");
        for (int i = 0; i < pestanas.getTabCount(); i++) {
            final int indice = i;
            JMenuItem item = new JMenuItem(pestanas.getTitleAt(i));
            item.addActionListener(e -> pestanas.setSelectedIndex(indice));
            menuModulos.add(item);
        }

        JMenu menuSesion = new JMenu("Sesión");
        JMenuItem cerrarSesion = new JMenuItem("Cerrar sesión");
        JMenuItem salir = new JMenuItem("Salir");
        cerrarSesion.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        salir.addActionListener(e -> System.exit(0));
        menuSesion.add(cerrarSesion);
        menuSesion.addSeparator();
        menuSesion.add(salir);

        JMenu menuAyuda = new JMenu("Ayuda");
        JMenuItem acerca = new JMenuItem("Acerca de");
        acerca.addActionListener(e -> Dialogos.info(this,
                "Biblioteca Escolar\nSistema de escritorio en Java (Swing + JDBC + MySQL)\n"
                        + "Patrones: MVC, DAO y Singleton. Préstamos con hilos y sincronización."));
        menuAyuda.add(acerca);

        barra.add(menuModulos);
        barra.add(menuSesion);
        barra.add(menuAyuda);
        return barra;
    }
}