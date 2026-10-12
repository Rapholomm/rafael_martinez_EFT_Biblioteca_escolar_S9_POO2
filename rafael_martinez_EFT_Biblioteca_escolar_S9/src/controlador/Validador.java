package controlador;

import java.util.regex.Pattern;

// validaciones reutilizadas por todos los controladores
public final class Validador {

    private static final Pattern NOMBRE = Pattern.compile("[\\p{L} .'-]+");
    private static final Pattern RUT = Pattern.compile("\\d{1,2}\\.?\\d{3}\\.?\\d{3}-[\\dkK]");
    private static final Pattern CORREO = Pattern.compile("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+");
    private static final Pattern ISBN = Pattern.compile("[\\d-]{10,17}[\\dxX]?");

    private Validador() {
    }

    public static String texto(String valor, String campo, int largoMaximo) throws ValidacionException {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty()) {
            throw new ValidacionException("El campo " + campo + " es obligatorio.");
        }
        if (limpio.length() > largoMaximo) {
            throw new ValidacionException("El campo " + campo + " no puede superar los " + largoMaximo + " caracteres.");
        }
        return limpio;
    }

    public static String nombrePersona(String valor, String campo) throws ValidacionException {
        String limpio = texto(valor, campo, 100);
        if (!NOMBRE.matcher(limpio).matches()) {
            throw new ValidacionException("El campo " + campo + " solo puede contener letras, espacios, puntos, apóstrofes o guiones.");
        }
        return limpio;
    }

    public static String rut(String valor) throws ValidacionException {
        String limpio = texto(valor, "RUT", 12);
        if (!RUT.matcher(limpio).matches()) {
            throw new ValidacionException("El RUT debe tener formato 12.345.678-5 (o 12345678-5).");
        }
        return limpio;
    }

    public static String correo(String valor) throws ValidacionException {
        String limpio = texto(valor, "correo", 100);
        if (!CORREO.matcher(limpio).matches()) {
            throw new ValidacionException("El correo no tiene un formato válido (ej: nombre@colegio.cl).");
        }
        return limpio;
    }

    public static String isbn(String valor) throws ValidacionException {
        String limpio = texto(valor, "ISBN", 20);
        String soloDigitos = limpio.replace("-", "");
        if (!ISBN.matcher(limpio).matches() || (soloDigitos.length() != 10 && soloDigitos.length() != 13)) {
            throw new ValidacionException("El ISBN debe tener 10 o 13 dígitos (puede llevar guiones).");
        }
        return limpio;
    }

    public static int entero(String valor, String campo, int minimo, int maximo) throws ValidacionException {
        String limpio = valor == null ? "" : valor.trim();
        try {
            int numero = Integer.parseInt(limpio);
            if (numero < minimo || numero > maximo) {
                throw new ValidacionException("El campo " + campo + " debe estar entre " + minimo + " y " + maximo + ".");
            }
            return numero;
        } catch (NumberFormatException ex) {
            throw new ValidacionException("El campo " + campo + " debe ser un número entero.");
        }
    }

    public static int seleccion(Integer id, String campo) throws ValidacionException {
        if (id == null) {
            throw new ValidacionException("Debes seleccionar " + campo + ".");
        }
        return id;
    }
}
