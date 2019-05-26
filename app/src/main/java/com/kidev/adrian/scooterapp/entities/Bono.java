package com.kidev.adrian.scooterapp.entities;

public class Bono {
    private Integer id;
    private String nombre;
    private String descripcion;
    private int minutos;
    private double precio;

    public Bono() {
    }


    public Bono(String nombre, String descripcion, int minutos, double precio) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.minutos = minutos;
        this.precio = precio;
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

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getDescripcion() {
        return this.descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public int getMinutos() {
        return this.minutos;
    }

    public void setMinutos(int minutos) {
        this.minutos = minutos;
    }
    public double getPrecio() {
        return this.precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }
}
