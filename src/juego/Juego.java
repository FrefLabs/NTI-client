package juego;

import NTI.Formato;
import NTI.Lectura;
import NTI.Registro;
import NTI.Accion;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

public class Juego {
    
    private String nombreJugador;
    private String modoJuego;
    private int puntosJugador;
    private int puntosIA;
    private int rondaActual;

    private String fecha;
    private double openHoy; 
    private double cierreAyer;
    private double altoAyer; 
    private double bajoAyer;
    private long volumenAyer;
    private double cierreHoy;
    private double cierreManana; 

    private Lectura lectura;
    private Registro registro;

    public Juego(Lectura lectura, Registro registro) {
        this.lectura = lectura;
        this.registro = registro;
    }

    public boolean ingresarNombre(String nombre) {
        if (Formato.validarNombre(nombre)) {
            this.nombreJugador = nombre;
            return true;
        }
        return false;
    }

    public void ingresarModo(String modo) {
        this.modoJuego = modo;
    }

    public void setPuntos() {
        this.puntosJugador = 1000;
        this.puntosIA = 1000;
        this.rondaActual = 0; 
    }

    public void forzarDerrota() {
        this.puntosJugador = 0;
    }

    public boolean generarNuevaRonda(String simbolo) {
        this.rondaActual++;
        
        Map<String, Object> gameData = lectura.getDatosParaJuego(simbolo);
        
        if (gameData == null) {
            System.err.println("No se pudieron obtener los datos del juego desde la API.");
            return false; // Indicar que la ronda no se pudo generar
        }
        
        // Asignar los valores obtenidos de la API
        this.fecha = (String) gameData.get("fecha");
        this.openHoy = (Double) gameData.get("open");
        this.cierreHoy = (Double) gameData.get("cierre");
        this.cierreAyer = (Double) gameData.get("cierreAyer");
        this.altoAyer = (Double) gameData.get("altoAyer");
        this.bajoAyer = (Double) gameData.get("bajoAyer");
        this.volumenAyer = ((Number) gameData.get("volumenAyer")).longValue();
        this.cierreManana = (Double) gameData.get("cierreManana");
        
        return true;
    }

    public String calcularResultadoRonda(int errorJugador, int errorIA, double prediccionJugador) {
        boolean aciertoDireccion = (prediccionJugador > this.cierreAyer && this.cierreHoy > this.cierreAyer) ||
                                  (prediccionJugador < this.cierreAyer && this.cierreHoy < this.cierreAyer);
        int puntosBonus = aciertoDireccion ? 10 : 0;
        this.puntosJugador += puntosBonus;
        this.puntosIA -= puntosBonus;

        String ganadorRonda;
        int puntosTransferidos = 0;
        if (errorJugador < errorIA) {
            puntosTransferidos = calcularPuntosTransferidos(errorIA); 
            this.puntosJugador += puntosTransferidos;
            this.puntosIA -= puntosTransferidos;
            ganadorRonda = this.nombreJugador;
        } else if (errorIA < errorJugador) {
            puntosTransferidos = calcularPuntosTransferidos(errorJugador); 
            this.puntosIA += puntosTransferidos;
            this.puntosJugador -= puntosTransferidos;
            ganadorRonda = "IA";
        } else {
            ganadorRonda = "Empate";
        }
        
        this.puntosJugador = Math.max(0, this.puntosJugador);
        this.puntosIA = Math.max(0, this.puntosIA);

        StringBuilder resumen = new StringBuilder();
        resumen.append(String.format("Ganador de la ronda: %s\n", ganadorRonda));
        if (ganadorRonda.equals(this.nombreJugador)) {
            resumen.append(String.format("¡Ganas %d puntos!\n", puntosTransferidos));
        } else if (ganadorRonda.equals("IA")) {
            resumen.append(String.format("Pierdes %d puntos.\n", puntosTransferidos));
        }
        if (aciertoDireccion) {
            resumen.append(String.format("¡Bonus por dirección! +%d puntos", puntosBonus));
        }
        return resumen.toString();
    }

    private int calcularPuntosTransferidos(int errorPerdedor) {
        int bonus = 0;
        if (errorPerdedor <= 50) {
            bonus = 100;
        } else if (errorPerdedor > 100 && errorPerdedor <= 200) {
            bonus = 50;
        }
        return errorPerdedor + bonus;
    }
    
    public String calcularResultadoEndless(int errorJugador, int errorIA, double prediccionJugador) {
        boolean aciertoDireccion = (prediccionJugador > this.cierreAyer && this.cierreHoy > this.cierreAyer) ||
                                  (prediccionJugador < this.cierreAyer && this.cierreHoy < this.cierreAyer);
        if (aciertoDireccion) {
            this.puntosJugador += 10; 
        }

        int puntosTransferidos = 0;
        String ganadorRonda;
        if (errorJugador < errorIA) {
            puntosTransferidos = calcularPuntosTransferidos(errorIA);
            this.puntosJugador += puntosTransferidos;
            ganadorRonda = this.nombreJugador;
        } else if (errorIA < errorJugador) {
            puntosTransferidos = calcularPuntosTransferidos(errorJugador);
            this.puntosJugador -= puntosTransferidos;
            ganadorRonda = "IA";
        } else {
            ganadorRonda = "Empate";
        }

        this.puntosJugador = Math.max(0, this.puntosJugador);

        StringBuilder resumen = new StringBuilder();
        resumen.append(String.format("Ganador de la ronda: %s\n", ganadorRonda));
        if (ganadorRonda.equals(this.nombreJugador)) {
            resumen.append(String.format("¡Sumas %d puntos!\n", puntosTransferidos));
        } else if (ganadorRonda.equals("IA")) {
            resumen.append(String.format("Pierdes %d puntos.\n", puntosTransferidos));
        }
        if (aciertoDireccion) {
            resumen.append("¡Bonus por dirección! +10 puntos");
        }
        return resumen.toString();
    }
    
    public boolean isJuegoTerminado() {
        return this.puntosJugador <= 0 || this.puntosIA <= 0;
    }
    
    public String getGanadorFinal() {
        if (this.puntosJugador <= 0) return "IA";
        if (this.puntosIA <= 0) return this.nombreJugador;
        return "Nadie";
    }

    public String getNombreJugador() { return nombreJugador; }
    public String getModoJuego() { return modoJuego; }
    public int getPuntosJugador() { return puntosJugador; }
    public int getRondaActual() { return rondaActual; }
    public int getPuntosIA() { return puntosIA; }
    public String getFecha() { return fecha; }
    public double getOpenHoy() { return openHoy; }
    public double getCierreAyer() { return cierreAyer; }
    public double getAltoAyer() { return altoAyer; }
    public double getBajoAyer() { return bajoAyer; }
    public long getVolumenAyer() { return volumenAyer; }
    public double getCierreHoy() { return cierreHoy; }
    public double getCierreManana() { return cierreManana; }

    public boolean guardarPartida(String simbolo) {
        if (this.registro == null) {
            System.err.println("Registro no inicializado en Juego");
            return false;
        }
        String simboloParaGuardar = (simbolo == null) ? "KO" : simbolo;
        return registro.guardarPartida(this.nombreJugador, this.puntosJugador, this.rondaActual, new Date(), this.modoJuego, simboloParaGuardar);
    }

    public Map<String, Vector<String[]>> obtenerRankings() {
        if (this.lectura == null) {
            System.err.println("Lectura no inicializada en Juego");
            return new java.util.HashMap<>();
        }
        return lectura.obtenerRankings();
    }
}