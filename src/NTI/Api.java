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

    // Ahora devuelve boolean
    public boolean obtenerDatosN() {
        Vector<Tupla> noticias = form.formatDatosN(key);

        if (noticias.isEmpty()) {
            System.out.println("No se obtuvieron noticias.");
            return false;
        }

        // solo dos (las que necesitamos por ahora)
        int limite = Math.min(2, noticias.size());
        boolean exito = false;

        for (int i = 0; i < limite; i++) {
            Tupla n = noticias.get(i);
            boolean insertOk = reg.cargaDatosN(n);
            exito = exito || insertOk; // si alguna inserción fue exitosa, exito = true
        }

        return exito;
    }
}
