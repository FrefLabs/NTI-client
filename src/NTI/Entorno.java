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
    
    public boolean enviarNConfig(String mon, String idm, boolean sfx, boolean modo, boolean rdr){
        boolean v = reg.actualizarCofig(mon, idm, sfx, modo, rdr); 
        return(v);
    }
    
    public String[] conseguirConfig(){
        String[] valores = lec.obtenerConfig();
        return(valores);
    }
}
