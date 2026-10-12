package modelo;

public class Estudiante extends Persona {

    private String curso;

    public Estudiante(String nombre, String rut, String curso, String correo) {
        this(0, nombre, rut, curso, correo);
    }

    public Estudiante(int id, String nombre, String rut, String curso, String correo) {
        super(id, nombre, rut, correo);
        this.curso = curso;
    }

    @Override
    public String getTipo() {
        return "Estudiante";
    }

    @Override
    public String descripcion() {
        return super.descripcion() + " - " + curso;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }
}
