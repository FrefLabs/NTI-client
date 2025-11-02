package NTI;

import java.util.Random;

public class Modelo {

    /**
     * Realiza una predicción (NERFEADA).
     * Ahora adivina un valor aleatorio ENTRE el cierre de ayer y el cierre de mañana.
     * Esto simula una IA que ve la "tendencia" pero no sabe la respuesta.
     */
    public double hacerPrediccion(double cierreAyer, double cierreManana) {
        Random r = new Random();
        
        double min = Math.min(cierreAyer, cierreManana);
        double max = Math.max(cierreAyer, cierreManana);
        
        // Devuelve un valor aleatorio dentro de ese rango
        return min + (max - min) * r.nextDouble();
    }
}