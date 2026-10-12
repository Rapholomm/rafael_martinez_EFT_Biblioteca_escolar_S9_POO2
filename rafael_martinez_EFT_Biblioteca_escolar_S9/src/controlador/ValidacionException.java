package controlador;

// datos ingresados por el usuario que no cumplen las reglas (la vista muestra el mensaje)
public class ValidacionException extends Exception {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}

