package NTI;

import java.util.Vector;
import java.util.Date;

public class Noticia {
    
    private Lectura lec;
    
    public Noticia() {
        this.lec = new Lectura();
    }
    
    public Vector<Tupla> getDatos(String simbolo) {
        return lec.obtenerDatosN(simbolo);
    }
}
