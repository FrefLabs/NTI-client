/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NTI;

/**
 *
 * @author NICO
 */
public class Entorno {
    
    Registro reg = new Registro();
    Lectura lec = new Lectura();
    
    public boolean enviarNConfig(String mon, boolean sfx, boolean rdr, int idModelo, String estiloGrafica){
        boolean v = reg.actualizarCofig(mon, sfx, rdr, idModelo, estiloGrafica); 
        return(v);
    }
    
    public String[] conseguirConfig(){
        String[] valores = lec.obtenerConfig();
        return(valores);
    }
}
