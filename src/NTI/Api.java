package NTI;

import java.util.Vector;

public class Api {

    private String nombre;
    private String key;
    private Lectura lec = new Lectura();
    private Formato form = new Formato();
    private Registro reg = new Registro();

    public Api(String nombre) {
        this.nombre = nombre;
    }

    public void setDatos() {
        this.key = lec.obtenerKey(nombre);
    }

    public String getKey() {
        return key;
    }

    /**
     * (MODIFICADO)
     * Ahora acepta un 'simbolo' y lo pasa a formato y registro.
     */
    public boolean obtenerDatosN(String simbolo) {
        // (Pasa el símbolo a Formato)
        Vector<Tupla> noticias = form.formatDatosN(key, simbolo);

        if (noticias.isEmpty()) {
            System.out.println("No se obtuvieron noticias.");
            return false;
        }

        int limite = Math.min(2, noticias.size());
        boolean exito = false;

        for (int i = 0; i < limite; i++) {
            Tupla n = noticias.get(i);
            // (Pasa la tupla Y el símbolo a Registro)
            boolean insertOk = reg.cargaDatosN(n, simbolo);
            exito = exito || insertOk; // si alguna inserción fue exitosa, exito = true
        }

        return exito;
    }
}