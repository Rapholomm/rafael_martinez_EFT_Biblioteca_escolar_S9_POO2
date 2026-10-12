package vista;

// muestra un texto legible y  guarda el id
public class ItemCombo {

    private final int id;
    private final String texto;

    public ItemCombo(int id, String texto) {
        this.id = id;
        this.texto = texto;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return texto;
    }
}