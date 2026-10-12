package main;

import dao.CategoriaDAO;
import dao.EstudianteDAO;
import dao.LibroDAO;
import modelo.Categoria;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Prestamo;
import dao.PrestamoDAO;
import servicio.PrestamoException;
import servicio.ServicioPrestamos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Prueba de consistencia del stock (Paso 4 de la pauta): crea un libro con pocas copias y lanza
 * muchos hilos que intentan pedirlo al mismo tiempo. Si la sincronizacion funciona, exactamente
 * "stock" hilos lo consiguen y el resto recibe "sin stock"; el stock final nunca queda negativo.
 * Todo lo que crea (libro, estudiantes de prueba y prestamos) se borra al terminar.
 * <p>
 * Ejecutar desde IntelliJ: click derecho en esta clase > Run 'PruebaConcurrencia.main()'.
 */
public class PruebaConcurrencia {

    private static final int STOCK_INICIAL = 3;
    private static final int HILOS = 10;

    public static void main(String[] args) throws Exception {
        LibroDAO libroDAO = new LibroDAO();
        EstudianteDAO estudianteDAO = new EstudianteDAO();
        PrestamoDAO prestamoDAO = new PrestamoDAO();
        ServicioPrestamos servicio = ServicioPrestamos.getInstance();

        List<Categoria> categorias = new CategoriaDAO().readAll();
        if (categorias.isEmpty()) {
            System.out.println("Primero ejecuta los scripts SQL (no hay categorías).");
            return;
        }

        Libro libro = new Libro("LIBRO DE PRUEBA CONCURRENCIA", "Autor Prueba", "0000000000", "Editorial Prueba",
                STOCK_INICIAL, categorias.get(0).getId());
        libroDAO.create(libro);
        List<Estudiante> estudiantes = new ArrayList<>();

        try {
            // un estudiante distinto por hilo (asi no se activa el limite de prestamos por estudiante)
            for (int i = 1; i <= HILOS; i++) {
                Estudiante e = new Estudiante("Prueba " + i, "9999900" + i + "-K", "Curso Prueba", "prueba" + i + "@prueba.cl");
                estudianteDAO.create(e);
                estudiantes.add(e);
            }

            System.out.println("Libro con stock " + STOCK_INICIAL + " y " + HILOS + " hilos pidiéndolo a la vez...");
            AtomicInteger exitosos = new AtomicInteger();
            AtomicInteger rechazados = new AtomicInteger();
            CountDownLatch salida = new CountDownLatch(1);
            List<Thread> hilos = new ArrayList<>();

            for (Estudiante e : estudiantes) {
                Thread hilo = new Thread(() -> {
                    try {
                        salida.await(); // todos esperan la señal para partir juntos
                        servicio.registrarPrestamo(e.getId(), libro.getId());
                        exitosos.incrementAndGet();
                    } catch (PrestamoException ex) {
                        rechazados.incrementAndGet();
                    } catch (Exception ex) {
                        System.out.println("Error inesperado: " + ex);
                    }
                }, "hilo-prueba-" + e.getId());
                hilos.add(hilo);
                hilo.start();
            }
            salida.countDown();
            for (Thread hilo : hilos) {
                hilo.join();
            }

            int stockFinal = libroDAO.readById(libro.getId()).getStock();
            System.out.println("Préstamos concedidos: " + exitosos.get() + " (esperado " + STOCK_INICIAL + ")");
            System.out.println("Rechazados por falta de stock: " + rechazados.get() + " (esperado " + (HILOS - STOCK_INICIAL) + ")");
            System.out.println("Stock final: " + stockFinal + " (esperado 0, nunca negativo)");
            boolean ok = exitosos.get() == STOCK_INICIAL && stockFinal == 0;
            System.out.println(ok ? ">>> PRUEBA APROBADA: el stock se mantuvo consistente." : ">>> PRUEBA FALLIDA");
        } finally {
            // limpieza: se borran los prestamos de prueba, los estudiantes y el libro
            for (Prestamo p : prestamoDAO.readAll()) {
                if (p.getIdLibro() == libro.getId()) {
                    servicio.eliminarPrestamo(p.getId());
                }
            }
            for (Estudiante e : estudiantes) {
                estudianteDAO.delete(e.getId());
            }
            libroDAO.delete(libro.getId());
            System.out.println("Datos de prueba eliminados.");
        }
    }
}