package interfaces;

// contrato para lo que se puede prestar: controla su propio stock

public interface Prestable {

    boolean estaDisponible();

    void descontarStock();

    void reponerStock();
}
