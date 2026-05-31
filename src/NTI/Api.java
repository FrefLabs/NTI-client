package NTI;

import java.util.Vector;

public class Api {

    private String nombreApi;
    private String key;
    private Formato form;
    private Registro reg;
    private Lectura lec; // (Lectura se usa para obtener la key)

    public Api(String nombreApi) {
        this.nombreApi = nombreApi;
        this.form = new Formato();
        this.reg = new Registro();
        this.lec = new Lectura();
        // (Llama a setDatos() para cargar la key al crear la Api)
        setDatos(); 
    }

    public void setDatos() {
        this.key = lec.obtenerKey(this.nombreApi); 
    }
}
