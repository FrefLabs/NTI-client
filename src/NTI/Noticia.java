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

public class Noticia {
    
    
    public static final String ARCHIVO_NOTICIA = "noticias.txt";
    
    public Vector<String[]> obtenerDatosNoticias() {
    Vector<String[]> vectorNoticias = new Vector<>();
    String linea;

    try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_NOTICIA))) {
        while ((linea = br.readLine()) != null) {
            // Saltar líneas vacías si las hubiera
            if (linea.trim().isEmpty()) {
                continue;
            }

            String[] fila = separarLinea(linea);
            vectorNoticias.add(fila);
        }
    } catch (IOException e) {
        System.out.println("Error al leer el archivo: " + e.getMessage());
    }
    
    return vectorNoticias;
    }

    
    private String[] separarLinea(String linea) {
        Vector<String> campos = new Vector<>();
        boolean entreComillas = false;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);

            if (c == '\"') {
                // Cambia el estado al encontrar comillas
                entreComillas = !entreComillas;
            } else if (c == ';' && !entreComillas) {
                // Nuevo campo encontrado
                campos.add(sb.toString().trim());
                sb.setLength(0); // Reinicia el StringBuilder
            } else {
                sb.append(c);
            }
        }

        // Agregar el último campo al final
        campos.add(sb.toString().trim());

        // Convertir Vector a array
        String[] arrayCampos = new String[campos.size()];
        for (int i = 0; i < campos.size(); i++) {
            arrayCampos[i] = campos.get(i);
        }

        return arrayCampos;
    }
}
