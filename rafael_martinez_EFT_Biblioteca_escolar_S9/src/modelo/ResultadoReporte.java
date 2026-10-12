package modelo;

import java.util.List;


public record ResultadoReporte(String titulo, String[] columnas, List<Object[]> filas) {
}
