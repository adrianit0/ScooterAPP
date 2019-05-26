package com.kidev.adrian.scooterapp.entities;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Date;

public class Scooter {

    private Integer id;
    private LatLng posicion;
    private Date fechaCompra;
    private String noSerie;
    private String matricula;
    private Integer codigo;
    private Float bateria;


    public Scooter() {

    }

    public Scooter(Integer id, LatLng posicion, Date fechaCompra, String noSerie, String matricula, Integer codigo, Float bateria) {
        this.id = id;
        this.posicion = posicion;
        this.fechaCompra = fechaCompra;
        this.noSerie = noSerie;
        this.matricula = matricula;
        this.codigo = codigo;
        this.bateria = bateria;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LatLng getPosicion() {
        return posicion;
    }

    public void setPosicion(LatLng posicion) {
        this.posicion = posicion;
    }

    public Date getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(Date fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public String getNoSerie() {
        return noSerie;
    }

    public void setNoSerie(String noSerie) {
        this.noSerie = noSerie;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public Float getBateria() {
        return bateria;
    }

    public void setBateria(Float bateria) {
        this.bateria = bateria;
    }
}
