package vista;

import controlador.LoginController;
import controlador.ValidacionException;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

// ventana de autenticacion
public class LoginFrame extends JFrame {

    private final LoginController controlador = new LoginController();
    private final JTextField campoUsuario = new JTextField(20);
    private final JPasswordField campoContrasena = new JPasswordField(20);

    public LoginFrame() {
        super("Biblioteca Escolar - Inicio de sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel titulo = new JLabel("Biblioteca Escolar", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        JLabel subtitulo = new JLabel("Ingresa con tu correo (o RUT) y contraseña", SwingConstants.CENTER);

        JPanel encabezado = new JPanel(new GridLayout(2, 1, 0, 4));
        encabezado.add(titulo);
        encabezado.add(subtitulo);

        JPanel formulario = new JPanel(new GridBagLayout());
        agregarFila(formulario, "Correo o RUT:", campoUsuario, 0);
        agregarFila(formulario, "Contraseña:", campoContrasena, 1);

        JButton btnIngresar = new JButton("Ingresar");
        JButton btnSalir = new JButton("Salir");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        botones.add(btnIngresar);
        botones.add(btnSalir);

        JPanel contenido = new JPanel(new BorderLayout(10, 14));
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(formulario, BorderLayout.CENTER);
        contenido.add(botones, BorderLayout.SOUTH);
        setContentPane(contenido);

        btnIngresar.addActionListener(e -> ingresar());
        btnSalir.addActionListener(e -> System.exit(0));
        getRootPane().setDefaultButton(btnIngresar); // Enter = ingresar

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void ingresar() {
        try {
            Usuario usuario = controlador.autenticar(campoUsuario.getText(), new String(campoContrasena.getPassword()));
            new VentanaPrincipal(usuario).setVisible(true);
            dispose();
        } catch (ValidacionException ex) {
            Dialogos.datoInvalido(this, ex.getMessage());
            campoContrasena.setText("");
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "iniciar sesión", ex);
        }
    }

    private static void agregarFila(JPanel panel, String etiqueta, JComponent campo, int fila) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.gridy = fila;
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panel.add(campo, c);
    }
}