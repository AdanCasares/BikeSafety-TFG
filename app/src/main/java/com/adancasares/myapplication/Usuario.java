package com.adancasares.myapplication;

public class Usuario {

    private String uid;
    private int vehiculo;
    private double longitud;
    private double latitud;
    private int emergencia;

    public Usuario() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(int vehiculo) {
        this.vehiculo = vehiculo;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public int getEmergencia() { return emergencia; }

    public void setEmergencia(int emergencia) {
        this.emergencia = emergencia;
    }
}
