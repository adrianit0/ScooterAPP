/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kidev.adrian.scooterapp.util;

import java.util.Map;
import java.util.TreeMap;

import com.kidev.adrian.scooterapp.inteface.IPaquete;
import com.kidev.adrian.scooterapp.util.Util.CODIGO;

/**
 *
 * Paquete que le llegar del servidor al cliente con la información del mismo
 * 
 * @author agarcia.gonzalez
 */
public class PaqueteCliente implements IPaquete {
    private CODIGO codigo;
    private String idPaquete;

    private Map<String,String> argumentos;
    private Map<String,String> objetos;

    public PaqueteCliente() {
        this.objetos = new TreeMap<>();
    }

    public PaqueteCliente(CODIGO codigo, String idPaquete, Map<String, String> argumentos) {
        this.codigo = codigo;
        this.idPaquete = idPaquete;
        this.argumentos = argumentos;
        this.objetos = new TreeMap<>();
    }
    
    public String addObjeto (String objeto) {
        String nombre = "Objeto"+idPaquete+"#"+objetos.size();
        objetos.put(nombre, objeto);
        return nombre;
    }
    
    public String getObjeto (String key) {
        if (objetos.containsKey(key))
            return objetos.get(key);
        
        return "null";
    }

    public Map<String, String> getObjetos() {
        return objetos;
    }

    public void setObjetos(Map<String, String> objetos) {
        this.objetos = objetos;
    }
    
    
    
    
    
    public CODIGO getCodigo() {
        return codigo;
    }

    public void setCodigo(CODIGO codigo) {
        this.codigo = codigo;
    }

    public String getIdPaquete() {
        return idPaquete;
    }

    public void setIdPaquete(String idPaquete) {
        this.idPaquete = idPaquete;
    }

    public Map<String, String> getArgumentos() {
        return argumentos;
    }

    public void setArgumentos(Map<String, String> argumentos) {
        this.argumentos = argumentos;
    }

    
    
    
}
