package modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Prestamo {

    public static final String EN_PRESTAMO = "En préstamo";
    public static final String DEVUELTO = "Devuelto";
    public static final String ATRASADO = "Atrasado";

    private int id;
    private int idEstudiante;
    private int idLibro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaVencimiento; // columna fecha_devolucion de la BD
    private boolean devuelto;


    private String nombreEstudiante;
    private String tituloLibro;

    public Prestamo(int idEstudiante, int idLibro, LocalDate fechaPrestamo, LocalDate fechaVencimiento) {
        this(0, idEstudiante, idLibro, fechaPrestamo, fechaVencimiento, false);
    }

    public Prestamo(int id, int idEstudiante, int idLibro, LocalDate fechaPrestamo,
                    LocalDate fechaVencimiento, boolean devuelto) {
        this.id = id;
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = fechaVencimiento;
        this.devuelto = devuelto;
    }

    // dias que lleva pasado del vencimiento
    public int diasAtraso(LocalDate hoy) {
        if (fechaVencimiento == null || !hoy.isAfter(fechaVencimiento)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(fechaVencimiento, hoy);
    }

    public boolean estaAtrasado(LocalDate hoy) {
        return !devuelto && diasAtraso(hoy) > 0;
    }

    public String getEstado(LocalDate hoy) {
        return textoEstado(devuelto, fechaVencimiento, hoy);
    }

    // version para que los reportes (que no arman un Prestamo) usen la misma regla
    public static String textoEstado(boolean devuelto, LocalDate vencimiento, LocalDate hoy) {
        if (devuelto) {
            return DEVUELTO;
        }
        return (vencimiento != null && hoy.isAfter(vencimiento)) ? ATRASADO : EN_PRESTAMO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public boolean isDevuelto() {
        return devuelto;
    }

    public void setDevuelto(boolean devuelto) {
        this.devuelto = devuelto;
    }

    public String getNombreEstudiante() {
        return nombreEstudiante;
    }

    public void setNombreEstudiante(String nombreEstudiante) {
        this.nombreEstudiante = nombreEstudiante;
    }

    public String getTituloLibro() {
        return tituloLibro;
    }

    public void setTituloLibro(String tituloLibro) {
        this.tituloLibro = tituloLibro;
    }
}
