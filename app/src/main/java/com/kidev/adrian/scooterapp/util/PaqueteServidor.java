/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kidev.adrian.scooterapp.util;

import java.util.Map;
import java.util.TreeMap;
import com.kidev.adrian.scooterapp.util.Util.CODIGO;

/**
 *
 * Paquete que le llegar del cliente al servidor con la información del mismo
 * 
 * @author agarcia.gonzalez
 */
public class PaqueteServidor implements IPaquete {
    private String idPaquete;
    private String nick;
    private String token;
    private String uri;

    private CallbackRespuesta callback;
    
    private Map<String,String> argumentos;
    private Map<String,String> objetos;

    public PaqueteServidor() {
        this.objetos = new TreeMap<>();
    }

    public PaqueteServidor(String idPaquete, String nick, String token, String uri, Map<String, String> argumentos) {
        this.idPaquete = idPaquete;
        this.nick = nick;
        this.token = token;
        this.uri = uri;
        this.argumentos = argumentos;
        this.objetos = new TreeMap<>();
    }

    public CallbackRespuesta getCallback() {
        return callback;
    }

    public void setCallback(CallbackRespuesta callback) {
        this.callback = callback;
    }
    
    public String addObjeto (String objeto) {
        String nombre = "Objeto"+idPaquete+"#"+objetos.size();
        objetos.put(nombre, objeto);
        return nombre;
    }

    public Map<String, String> getObjetos() {
        return objetos;
    }

    public void setObjetos(Map<String, String> objetos) {
        this.objetos = objetos;
    }
    
    
    
    public String getObjeto (String key) {
        if (objetos.containsKey(key))
            return objetos.get(key);
        
        return "null";
    }

    public String getIdPaquete() {
        return idPaquete;
    }

    public void setIdPaquete(String idPaquete) {
        this.idPaquete = idPaquete;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Map<String, String> getArgumentos() {
        return argumentos;
    }

    public void setArgumentos(Map<String, String> argumentos) {
        this.argumentos = argumentos;
    }

    
    
    
}
