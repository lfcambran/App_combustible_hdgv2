package com.app_combustible_hdgv2.utilidades;

public class varibles_globales {

    private static double latitud=0;
    private static double longitu=0;
    private static String autorizado_aterrizaje;
    public String getAutorizado_aterrizaje() {
        return autorizado_aterrizaje;
    }
    public void setAutorizado_aterrizaje(String autorizado_aterrizaje) {
        varibles_globales.autorizado_aterrizaje = autorizado_aterrizaje;
    }
    public double getlatitud() {
        return latitud;
    }

    public void setlatitud(double latitud) {
       this.latitud = latitud;
    }

    public double getlongitu() {
        return longitu;
    }

    public void setlongitu(double longitu) {
       this.longitu = longitu;
    }
}
