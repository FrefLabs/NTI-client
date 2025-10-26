/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NTI;

/**
 *
 * @author Usuario
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.Date;

public class Noticia {
    
    Lectura lec = new Lectura();
    
    public Vector<Tupla> getDatos() {
        Date fecha = new Date(); // fecha del sistema
        Vector<Tupla> noticias = lec.obtenerDatosN(fecha);

        // Si no hay noticias, ejecutamos flujo de API
        if (noticias.isEmpty()) {
            Api api = new Api("Finnhub");
            api.setDatos();
            System.out.println("No hay noticias para hoy. Ejecutando flujo de API...");
            boolean exito = api.obtenerDatosN(); // aquí tu API inserta 2 noticias
            if (exito) {
                // Recargamos las noticias recién insertadas
                noticias = lec.obtenerDatosN(fecha);
            } else {
                System.err.println("No se obtuvieron noticias desde la API");
            }
        }

        return noticias;
    }
}