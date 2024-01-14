package com.app_combustible_hdgv2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app_combustible_hdgv2.R;
import com.app_combustible_hdgv2.activity_lista_vales;

import java.util.ArrayList;
import java.util.Objects;

public class adaptador_docemitidos extends BaseAdapter {
    private Context context;
    private ArrayList<lista_data> listItems;

    public adaptador_docemitidos(Context context, ArrayList<lista_data> datos) {
        this.context = context;
        this.listItems = datos;
    }


    @Override
    public int getCount(){ return listItems.size();}
    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }
    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        lista_data item =(lista_data) getItem(position);

        convertView  = LayoutInflater.from(context).inflate(R.layout.list_vales, null);

        TextView txtcorrelativo = convertView.findViewById(R.id.txtcorrelativo);
        TextView txtTitle =convertView.findViewById(R.id.txtTitle);
        TextView txtmatricula = convertView.findViewById(R.id.txtmatricuala);
        TextView txtnodoc = convertView.findViewById(R.id.txtnodoc);
        TextView txtfechadoc = convertView.findViewById(R.id.txtfechareporte);
        TextView txtcantidad = convertView.findViewById(R.id.txtCantidad);
        TextView txtvalor = convertView.findViewById(R.id.txt_valor);
        TextView txttotal = convertView.findViewById(R.id.txttotal);
        TextView txtemitido = convertView.findViewById(R.id.txtemitidopor);


        txtTitle.setText("Documento Emitido");
        txtcorrelativo.setText("Correlativo:" + item.correlativo);
        txtmatricula.setText("Matricula: "  + item.matricula);
        txtnodoc.setText("No Doc.: " +  item.numerovale);
        txtfechadoc.setText("Fecha Doc.: " + item.fecha);
        txtcantidad.setText("Cantidad: " + String.valueOf(item.cantidad));
        txtvalor.setText("Valor: Q." + String.valueOf(item.valor_vale));
        txttotal.setText("Total Doc.: Q. " + String.valueOf(item.total));
        txtemitido.setText("Emitido:" + item.emitidopor);
        return convertView;
    }

}
