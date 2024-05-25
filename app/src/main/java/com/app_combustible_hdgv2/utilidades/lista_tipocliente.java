package com.app_combustible_hdgv2.utilidades;

public class lista_tipocliente {
     int id_tipocliente;
     String tipocliente;

    public lista_tipocliente(){};

    public lista_tipocliente(int id_tipocliente, String tipocliente) {
        setTipocliente(tipocliente) ;
        setId_tipocliente(id_tipocliente);
    }

    public int getId_tipocliente() {
        return id_tipocliente;
    }

    public void setId_tipocliente(int id_tipocliente) {
        this.id_tipocliente = id_tipocliente;
    }

    public String toString() {
        return tipocliente;
    }

    public void setTipocliente(String tipocliente) {
        this.tipocliente = tipocliente;
    }





}
