package com.app_combustible_hdgv2;


import android.app.DatePickerDialog;
import android.app.LauncherActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app_combustible_hdgv2.adapters.adaptador_docemitidos;
import com.app_combustible_hdgv2.utilidades.MarshalDouble;
import com.app_combustible_hdgv2.utilidades.varibles_globales;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.app_combustible_hdgv2.adapters.lista_data;

public class activity_lista_vales extends AppCompatActivity implements View.OnClickListener {
    public final Calendar c = Calendar.getInstance();
    private static final String CERO = "0";
    private static final String BARRA = "/";
    //Variables para obtener la fecha
    final int mes = c.get(Calendar.MONTH);
    final int dia = c.get(Calendar.DAY_OF_MONTH);
    final int anio = c.get(Calendar.YEAR);
    ArrayList<String> adaptador_docemitidos = new ArrayList<>();
    private ListView lv;
    ImageButton bfechai,bfechaf;
    EditText fecha_inicial,fecha_final;
    Button bconsultar;

    varibles_globales vg;

    final String URL = "http://200.30.144.133:3427/wsite_c/wsb_combustible_hdg/ws_datos_combustible.asmx";
    final String NAMESPACES = "http://tempuri.org/";
    String uname;
    int codigo_sucursal;
    private SoapObject resultRequestSOAP = null;
    ArrayList<lista_data> datos=new ArrayList<lista_data>();
    CheckBox aterrizajes;

    LinearLayout grupoaterrizaje;
protected void onCreate(Bundle savedInstancesState) {
    super.onCreate(savedInstancesState);
    setTitle("Documentos Emitidos por Fecha");
    setContentView(R.layout.activity_lista_vales);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    vg =new varibles_globales();
    adaptador_docemitidos=new ArrayList<String>();
    lv=(ListView)findViewById(R.id.lv_doc);
    aterrizajes=findViewById(R.id.ver_aterrizajes);
    grupoaterrizaje=findViewById(R.id.groupaterrizaje);
    fecha_inicial=(EditText) findViewById(R.id.fechainicial);
    fecha_final = (EditText) findViewById(R.id.fechafinal);
    bfechai = (ImageButton) findViewById(R.id.btfechai);
    bfechaf = (ImageButton) findViewById(R.id.btfechaf);
    bconsultar=(Button) findViewById(R.id.btconsultar);
    bfechai.setOnClickListener(this);
    bfechaf.setOnClickListener(this);
    bconsultar.setOnClickListener(this);
    uname=getIntent().getStringExtra("nombre_usuario");
    consultar_datos(uname);
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    Date date = new Date();
    String fecha = dateFormat.format(date);
    fecha_inicial.setText(fecha);
    fecha_final.setText(fecha);
    if (vg.getAutorizado_aterrizaje().equals("true")){
        grupoaterrizaje.setVisibility(View.VISIBLE);
    }else
    {grupoaterrizaje.setVisibility(View.GONE);}

    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (aterrizajes.isChecked()!=true) {
                datos.get(position);
                int correlativo_doc = datos.get(position).getCorrelativo();
                Intent doc_emitido = new Intent(activity_lista_vales.this, documento_emitido.class);
                doc_emitido.putExtra("correlativo_doc", correlativo_doc);
                startActivity(doc_emitido);
            }
        }
    });
}

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btfechai) {
            ObtenerFecha_inicial();
        } else if (id == R.id.btfechaf) {
            ObtenerFecha_final();
        } else if (id == R.id.btconsultar){
            consultar_documentos_emitidos();
        }
    }

    private void ObtenerFecha_inicial(){
        final DatePickerDialog rfecha = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                final int mesActual = month + 1;
                String diaFormateado = (dayOfMonth < 10)? CERO + String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);
                String mesFormateado = (mesActual < 10)? CERO + String.valueOf(mesActual):String.valueOf(mesActual);
                fecha_inicial.setText(diaFormateado + BARRA + mesFormateado + BARRA + year );
            }
        },anio,mes,dia);
        rfecha.show();
    }
    private void ObtenerFecha_final(){
    final DatePickerDialog rfechaf = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener(){
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            final int mesactual = month +1;
            String diaFormateado = (dayOfMonth < 10)? CERO + String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);
            String mesFormateado = (mesactual < 10)? CERO + String.valueOf(mesactual):String.valueOf(mesactual);
            fecha_final.setText(diaFormateado + BARRA + mesFormateado + BARRA + year );
        }
    },anio,mes,dia);
    rfechaf.show();
    }

    private void consultar_documentos_emitidos(){
    String finicial =fecha_inicial.getText().toString();
    String ffinal = fecha_final.getText().toString();
    if (aterrizajes.isChecked()){
        llenar_lv_documentos_aterrizaje(finicial,ffinal);
        Toast.makeText(getApplicationContext(),"Consultando Aterrizajes de: " + finicial + " - " + ffinal ,Toast.LENGTH_LONG).show();
    }else {
        llenar_lv_documentos(finicial, ffinal);
        Toast.makeText(getApplicationContext(), "Consultar Datos: " + finicial + " - " + ffinal, Toast.LENGTH_LONG).show();
    }
    }
    public void llenar_lv_documentos_aterrizaje(String fechai,String fechaf){
    final String SOAP_ACTION="http://tempuri.org/Listado_aterrizaje_emitidos_base_fecha";
    final String METHOD = "Listado_aterrizaje_emitidos_base_fecha";
    SoapObject datos_at = new SoapObject(NAMESPACES,METHOD);
    MarshalDouble md = new MarshalDouble();
    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
    md.register(envelope);
    envelope.dotNet=true;
    envelope.implicitTypes=true;
    envelope.encodingStyle=SoapSerializationEnvelope.XSD;
    datos_at.addProperty("codigo_sucursal",codigo_sucursal);
    datos_at.addProperty("fecha_inicial",fechai);
    datos_at.addProperty("fecha_final",fechaf);
    envelope.setOutputSoapObject(datos_at);
    HttpTransportSE transportSE = new HttpTransportSE(URL);
    try {
        transportSE.call(SOAP_ACTION,envelope);
        resultRequestSOAP=(SoapObject) envelope.getResponse();
        datos.clear();
        int item_count = resultRequestSOAP.getPropertyCount();

        for (int m=0; m<item_count; m++){
            SoapObject lista_d = (SoapObject) resultRequestSOAP.getProperty(m);
            lista_data ldoc_emitido = new lista_data();

            ldoc_emitido.setCorrelativo(Integer.parseInt(lista_d.getProperty("correlativo").toString()));
            ldoc_emitido.setNumerovale(Integer.parseInt(lista_d.getProperty("nodoc").toString()));
            ldoc_emitido.setMatricula(lista_d.getProperty("matricula").toString());
            ldoc_emitido.setFecha(lista_d.getProperty("fecha_doc").toString());
            ldoc_emitido.setCantidad(Float.parseFloat(lista_d.getProperty("cantidad").toString()));
            ldoc_emitido.setValor_vale(Float.parseFloat(lista_d.getProperty("valor").toString()));
            ldoc_emitido.setTotal(Float.parseFloat(lista_d.getProperty("total_doc").toString()));
            ldoc_emitido.setEmitidopor(lista_d.getProperty("emitidopor").toString());
            ldoc_emitido.setTipocliente((lista_d.getProperty("tipocliente").toString()));
            datos.add(ldoc_emitido);
        }
        adaptador_docemitidos rdoc_emitidos=new adaptador_docemitidos(this,datos);
        rdoc_emitidos.setAterrizaje(aterrizajes.isChecked());
        lv.setAdapter(rdoc_emitidos);

    }catch (IOException e){
        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
    }catch (XmlPullParserException d){
        Toast.makeText(getApplicationContext(),d.getMessage(), Toast.LENGTH_LONG).show();
    }
    }
    public void llenar_lv_documentos(String fechai, String fechaf){
        final String SOAP_ACTION="http://tempuri.org/listar_documentos_emitidos_fecha";
        final String METHOD_NAME="listar_documentos_emitidos_fecha";

        SoapObject datos_res = new SoapObject(NAMESPACES,METHOD_NAME);
        MarshalDouble md = new MarshalDouble();
        SoapSerializationEnvelope env = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        md.register(env);
        env.dotNet=true;
        env.implicitTypes=true;
        env.encodingStyle=SoapSerializationEnvelope.XSD;
        datos_res.addProperty("codigo_sucursal",codigo_sucursal);
        datos_res.addProperty("fecha_inicial",fechai );
        datos_res.addProperty("fecha_final",fechaf);
        env.setOutputSoapObject(datos_res);
        HttpTransportSE tse = new HttpTransportSE(URL);
        try {
            tse.call(SOAP_ACTION,env);
            resultRequestSOAP=(SoapObject) env.getResponse();

            datos.clear();

            int item_count = resultRequestSOAP.getPropertyCount();

            for (int m=0; m<item_count; m++){
                SoapObject lista_d = (SoapObject) resultRequestSOAP.getProperty(m);
                lista_data ldoc_emitido = new lista_data();
                ldoc_emitido.setCorrelativo(Integer.parseInt(lista_d.getProperty("correlativo").toString()));
                ldoc_emitido.setNumerovale(Integer.parseInt(lista_d.getProperty("novale").toString()));
                ldoc_emitido.setMatricula(lista_d.getProperty("matricula").toString());
                ldoc_emitido.setFecha(lista_d.getProperty("fecha_vale").toString());
                ldoc_emitido.setCantidad(Float.parseFloat(lista_d.getProperty("cantidad").toString()));
                ldoc_emitido.setValor_vale(Float.parseFloat(lista_d.getProperty("valor").toString()));
                ldoc_emitido.setTotal(Float.parseFloat(lista_d.getProperty("total_doc").toString()));
                ldoc_emitido.setEmitidopor(lista_d.getProperty("emitidopor").toString());
                datos.add(ldoc_emitido);

            }

            adaptador_docemitidos rdoc_emitidos=new adaptador_docemitidos(this,datos);
            lv.setAdapter(rdoc_emitidos);


        }catch (IOException e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }catch (XmlPullParserException d){
            Toast.makeText(getApplicationContext(),d.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void consultar_datos(String user){
        final String SOAP_ACTION ="http://tempuri.org/consultar_datos_usuario";
        final String METHOD_NAME = "consultar_datos_usuario";

        SoapObject resquest = new SoapObject(NAMESPACES,METHOD_NAME);
        MarshalDouble md = new MarshalDouble();
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        md.register(envelope);
        envelope.dotNet=true;
        envelope.implicitTypes=true;
        envelope.encodingStyle=SoapSerializationEnvelope.XSD;

        resquest.addProperty("codigo_usuario",user);
        envelope.setOutputSoapObject(resquest);
        HttpTransportSE transportSE =new HttpTransportSE(URL);

        try {
            transportSE.call(SOAP_ACTION,envelope);
            resultRequestSOAP=(SoapObject) envelope.getResponse();

            int nPropiedades = resultRequestSOAP.getPropertyCount();
            for (int i = 0; i < nPropiedades; i++)
            {
                SoapObject ic = (SoapObject)resultRequestSOAP.getProperty(i);
                codigo_sucursal=Integer.parseInt(ic.getProperty("codigo_sucursal").toString());

            }

            if (codigo_sucursal==0){
                if (codigo_sucursal==0){
                    bconsultar.setEnabled(false);
                    Toast.makeText(getApplicationContext(),"Sin Numero de Sucursal",Toast.LENGTH_LONG).show();
                }

            }else{
                bconsultar.setEnabled(true);
            }

        }catch (IOException e){
            e.printStackTrace();
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }
    }
}
