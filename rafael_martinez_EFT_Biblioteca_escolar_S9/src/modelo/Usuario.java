package modelo;

import interfaces.Autenticable;

// persona que ingresa al sistema (bibliotecario o estudiante)
public class Usuario extends Persona implements Autenticable {

    private String contrasena;
    private Rol rol;

    public Usuario(String nombre, String rut, String correo, String contrasena, Rol rol) {
        this(0, nombre, rut, correo, contrasena, rol);
    }

    public Usuario(int id, String nombre, String rut, String correo, String contrasena, Rol rol) {
        super(id, nombre, rut, correo);
        this.contrasena = contrasena;
        this.rol = rol;
    }

    @Override
    public boolean validarCredenciales(String contrasenaIngresada) {
        return contrasena != null && contrasena.equals(contrasenaIngresada);
    }

    public boolean esBibliotecario() {
        return rol == Rol.BIBLIOTECARIO;
    }

    @Override
    public String getTipo() {
        return "Usuario";
    }

    @Override
    public String descripcion() {
        return super.descripcion() + " - rol " + rol.valorBD();
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}