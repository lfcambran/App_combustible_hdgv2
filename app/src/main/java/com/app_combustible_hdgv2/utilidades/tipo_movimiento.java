package com.app_combustible_hdgv2.utilidades;

public class tipo_movimiento {
    int codigomovimiento;
    String movimiento;

    public tipo_movimiento(){}

    public tipo_movimiento(int codigo,String movimiento){
        setCodigomovimiento(codigo);
        setMovimiento(movimiento);
    }

    public int getCodigomovimiento(){return codigomovimiento;}
    public String toString(){return  movimiento;}

    public void setCodigomovimiento(int codigom){
        this.codigomovimiento=codigom;
    }
    public void setMovimiento(String movimiento){
        this.movimiento=movimiento;
    }

}
