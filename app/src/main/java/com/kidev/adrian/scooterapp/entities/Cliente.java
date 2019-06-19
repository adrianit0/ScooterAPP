package com.kidev.adrian.scooterapp.entities;

import java.sql.Date;

public class Cliente  implements java.io.Serializable {


    private Integer id;
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String nick;
    private String email;
    private int minutos;
    private String fechaCreacion;

    public Cliente() {
    }

    public Cliente(String nombre, String apellido1, String nick, String email, String pass, int minutos, String fechaCreacion) {
        this.nombre = nombre;
        this.apellido1 = apellido1;
        this.nick = nick;
        this.email = email;
        this.minutos = minutos;
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public String getNombre() {
        return this.nombre;
    }

    public String getNombreCompleto () {
        return nombre + " " + apellido1 + (apellido2!=null&&!apellido2.equals("null")?" "+apellido2:"");
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getApellido1() {
        return this.apellido1;
    }

    public void setApellido1(String apellido1) {
        this.apellido1 = apellido1;
    }
    public String getApellido2() {
        return this.apellido2;
    }

    public void setApellido2(String apellido2) {
        this.apellido2 = apellido2;
    }
    public String getNick() {
        return this.nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public int getMinutos() {
        return this.minutos;
    }

    public void setMinutos(int minutos) {
        this.minutos = minutos;
    }
    public String getFechaCreacion() {
        return this.fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}

