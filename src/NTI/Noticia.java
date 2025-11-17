package NTI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.Date;

public class Noticia {
    
    Lectura lec = new Lectura();
    
    /**
     * (MODIFICADO)
     * Ahora acepta un 'simbolo' y lo pasa a los métodos de lectura y api.
     */
    public Vector<Tupla> getDatos(String simbolo) {
        Date fecha = new Date(); // fecha del sistema
        
        // (Pasa el símbolo a Lectura)
        Vector<Tupla> noticias = lec.obtenerDatosN(fecha, simbolo);

        // Si no hay noticias, ejecutamos flujo de API
        if (noticias.isEmpty()) {
            Api api = new Api("Finnhub");
            api.setDatos();
            System.out.println("No hay noticias para hoy. Ejecutando flujo de API para: " + simbolo);
            
            // (Pasa el símbolo a la Api)
            boolean exito = api.obtenerDatosN(simbolo); 
            
            if (exito) {
                // Recargamos las noticias recién insertadas
                noticias = lec.obtenerDatosN(fecha, simbolo);
            } else {
                System.err.println("No se obtuvieron noticias desde la API para: " + simbolo);
            }
        }

        return noticias;
    }
}