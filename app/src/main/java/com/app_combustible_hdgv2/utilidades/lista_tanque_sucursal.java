package com.app_combustible_hdgv2.utilidades;

public class lista_tanque_sucursal {
    int codigo_tanque;
    String descripcion_tanque;
    double capacidad;

    public lista_tanque_sucursal() {}

    public lista_tanque_sucursal(int codigo_tanque,String descripcion,double capacidad){
        setCodigo_tanque(codigo_tanque);
        setDescripcion_tanque(descripcion);
        setCapacidad(capacidad);
    }

    public int getCodigo_tanque(){return codigo_tanque;}
    public String toString(){return descripcion_tanque;}
    public double getCapacidad(){return capacidad;}

    public void setCodigo_tanque(int codigo_tanque){
        this.codigo_tanque=codigo_tanque;
    }
    public void setDescripcion_tanque(String descripcion){
        this.descripcion_tanque=descripcion;
    }
    public void setCapacidad(double capacidad){
        this.capacidad=capacidad;
    }

}
