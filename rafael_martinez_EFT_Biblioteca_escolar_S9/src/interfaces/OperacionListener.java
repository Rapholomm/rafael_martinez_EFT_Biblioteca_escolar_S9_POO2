package interfaces;

// callback para avisar el resultado de una operacion que corre en un hilo aparte.
// ojo: se llama desde el hilo de trabajo, la vista debe pasar a la EDT con SwingUtilities.invokeLate

public interface OperacionListener<T> {

    void alExito(T resultado);

    void alFallar(String mensaje);
}
