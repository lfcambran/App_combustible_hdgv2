package com.app_combustible_hdgv2.utilidades;

public class lista_sucursales {
    private int codigosucursal;
    private String nombre_sucursal;

    public lista_sucursales(){};

    public lista_sucursales(int codigo,String sucursal){
        setCodigo_sucursal(codigo);
        setNombre_sucursal(sucursal);
    }

    public int getCodigo_sucursal(){
        return codigosucursal;
    }
    public String toString(){
        return nombre_sucursal;
    }
    public void setCodigo_sucursal(int codigo_sucursal){
        this.codigosucursal=codigo_sucursal;
    }
    public void setNombre_sucursal(String nombre_sucursal){
        this.nombre_sucursal=nombre_sucursal;
    }
}
