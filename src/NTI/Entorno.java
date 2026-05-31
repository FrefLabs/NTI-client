package NTI;

public class Entorno {

    public String moneda;
    public boolean sfx;
    public boolean rdr;
    public int idModelo;
    public String estiloGrafica;
    public int volumenMusica;

    public boolean isLoaded = false;

    private Registro reg;
    private Lectura lec;

    public Entorno() {
        this.reg = new Registro();
        this.lec = new Lectura();
        cargarConfiguracionDesdeJSON();
    }

    public void cargarConfiguracionDesdeJSON() {
        String[] valores = lec.obtenerConfig(); 
        
        this.moneda = valores[0];
        this.sfx = Boolean.parseBoolean(valores[1]);
        this.rdr = Boolean.parseBoolean(valores[2]);
        this.idModelo = Integer.parseInt(valores[3]);
        this.estiloGrafica = valores[4];
        this.volumenMusica = Integer.parseInt(valores[5]);
        this.isLoaded = true;
    }

    public boolean guardarConfiguracion() {
        if (!this.isLoaded) {
            System.err.println("ADVERTENCIA: Se intentó guardar un objeto Entorno no inicializado. Forzando recarga desde config.json para prevenir sobreescritura.");
            cargarConfiguracionDesdeJSON();
        }
        boolean v = reg.actualizarCofig(
            this.moneda, 
            this.sfx, 
            this.rdr, 
            this.idModelo, 
            this.estiloGrafica,
            this.volumenMusica 
        );
        return v;
    }

    public boolean enviarNConfig(String mon, boolean sfx, boolean rdr, int idModelo, String estiloGrafica, int volumen) {
        this.moneda = mon;
        this.sfx = sfx;
        this.rdr = rdr;
        this.idModelo = idModelo;
        this.estiloGrafica = estiloGrafica;
        this.volumenMusica = volumen;

        return guardarConfiguracion();
    }

    public String[] conseguirConfig() {
        return new String[] {
            this.moneda,
            String.valueOf(this.sfx),
            String.valueOf(this.rdr),
            String.valueOf(this.idModelo),
            this.estiloGrafica,
            String.valueOf(this.volumenMusica)
        };
    }
}
