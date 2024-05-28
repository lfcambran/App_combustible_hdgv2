package com.app_combustible_hdgv2.adapters;

import java.text.DecimalFormat;

public class lista_data {
    DecimalFormat df = new DecimalFormat("###.##");
    int numerovale,correlativo;
    String matricula;
    String fecha;
    String emitidopor,tipocliente;
    double cantidad,valor_vale,total;

    public int getCorrelativo(){return correlativo;}

    public String getEmitidopor() {
        return emitidopor;
    }
    public void setEmitidopor(String emitidopor) {
        this.emitidopor = emitidopor;
    }

    public String getTipocliente(){return  tipocliente;}
    public void setTipocliente(String tipocliente){this.tipocliente=tipocliente;}
    public int getNumerovale() {
        return numerovale;
    }

    public void setCorrelativo(int correlativo){this.correlativo = correlativo;}
    public void setNumerovale(int numerovale) {
        this.numerovale = numerovale;
    }

    public String getMatricula() {
        return matricula;
    }
    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(float cantidad) {
        this.cantidad = Double.parseDouble(df.format(cantidad));
    }

    public double getValor_vale() {
        return valor_vale;
    }

    public void setValor_vale(float valor_vale) {
        this.valor_vale = Double.parseDouble(df.format( valor_vale));
    }

    public double getTotal(float total){
        return total;
    }

    public void setTotal(float total){
        this.total = Double.parseDouble(df.format(total)) ;
    }


}
