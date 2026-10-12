package modelo;

// roles del sistema: el bibliotecario
public enum Rol {
    BIBLIOTECARIO,
    ESTUDIANTE;

    // convierte el texto guardado en la BD (ej: "bibliotecario") al enum, sin distinguir mayusculas
    public static Rol desdeTexto(String texto) {
        if (texto != null) {
            String t = texto.trim().toLowerCase();
            if (t.startsWith("biblio")) {
                return BIBLIOTECARIO;
            }
            if (t.startsWith("estud")) {
                return ESTUDIANTE;
            }
        }
        throw new IllegalArgumentException("Rol no reconocido: " + texto);
    }

    // valor que se guarda en la columna rol
    public String valorBD() {
        return name().toLowerCase();
    }
}
