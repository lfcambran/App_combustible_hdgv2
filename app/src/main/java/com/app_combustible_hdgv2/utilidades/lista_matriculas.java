package com.app_combustible_hdgv2.utilidades;

public class lista_matriculas {

private int codigomatricula;
private String nmatricula;
private String cliente;

    public lista_matriculas(){};
    public lista_matriculas(int codigomatricula,String matricula,String cliente){
        setCodigomatricula(codigomatricula);
        setNmatricula(matricula);
        setCliente(cliente);
    }

    public int getCodigomatricula(){return codigomatricula;}
    public String toString(){return nmatricula;}
    public String getCliente(){return  cliente;}

    public void setCodigomatricula(int codigo_matricula){
        this.codigomatricula=codigo_matricula;
    }
    public void setNmatricula(String matricula){
        this.nmatricula=matricula;
    }
    public void setCliente(String ccliente){
        this.cliente=ccliente;
    }
}
