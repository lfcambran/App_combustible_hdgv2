package com.app_combustible_hdgv2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.app_combustible_hdgv2.utilidades.MarshalDouble;
import com.app_combustible_hdgv2.utilidades.lista_matriculas;
import com.app_combustible_hdgv2.utilidades.lista_sucursales;
import com.app_combustible_hdgv2.utilidades.lista_tipocliente;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.kyanogen.signatureview.SignatureView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.sql.Struct;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class activity_aterrizaje extends AppCompatActivity {
    final String URL = "http://200.30.144.133:3427/wsite_c/wsb_combustible_hdg/ws_datos_combustible.asmx";
    final String NAMESPACES = "http://tempuri.org/";
    private SoapObject resultRequestSOAP = null;
    String uname,codigotransaccion,nombre_usuario = null,matricula;
    EditText fecha_vale,codigo_producto,p_aterrizaje,cantidad_venta,total_venta;
    TextView nombreproducto;
    double precio_aterrizaje;
    DatePickerDialog picker;
    Spinner listasucursales,lmatriculas,lista_tipo_cliente;
    int codigo_sucursal,codigo_empleado,codigo_matricula,codigoproducto;
    Button grabar,limpiarfirma;
    SignatureView firma;
    ScrollView scrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aterrizaje);
        setTitle("Registro de Aterrizajes");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date date = new Date();
        String fecha = dateFormat.format(date);
        scrollView= (ScrollView) findViewById(R.id.sview);
        scrollView.requestDisallowInterceptTouchEvent(true);
        uname=getIntent().getStringExtra("nombre_usuario");
        nombreproducto=findViewById(R.id.nombreproducto);
        codigo_producto=findViewById(R.id.cproducto);
        fecha_vale=findViewById(R.id.fecha_vale);
        fecha_vale.setInputType(InputType.TYPE_NULL);
        fecha_vale.setText(fecha);
        listasucursales=findViewById(R.id.sucursal);
        lmatriculas=findViewById(R.id.matriculas);
        lista_tipo_cliente=findViewById(R.id.tipocliente);
        p_aterrizaje=findViewById(R.id.precio);
        grabar=findViewById(R.id.grabar);
        limpiarfirma=findViewById(R.id.limpiarf);
        cantidad_venta=findViewById(R.id.cantidad);
        total_venta=findViewById(R.id.total_aterrizaje);
        cantidad_venta.setEnabled(false);
        consultar_datos(uname);
        llenar_matriculas();
        Llenar_tipocliente();
        consultar_codigo_producto();
        buscar_nombre_producto(codigoproducto);
        generar_codigo_transaccion();
        firma=findViewById(R.id.signature_view);
        fecha_vale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr=Calendar.getInstance();
                int dia = cldr.get(Calendar.DAY_OF_MONTH);
                int mes = cldr.get(Calendar.MONTH);
                int anio = cldr.get(Calendar.YEAR);


                picker =new DatePickerDialog(activity_aterrizaje.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String dia_cero=null;
                        String mes_cero=null;
                        if (mes<10){
                            mes_cero="0" + (month+1);
                        }else{
                            mes_cero= String.valueOf((month+1));
                        }
                        if (dia<10){
                            dia_cero = "0" + (dayOfMonth);
                        }else{
                            dia_cero= String.valueOf(dayOfMonth);
                        }
                        fecha_vale.setText(dia_cero + "-" + mes_cero + "-" + year);
                    }
                },anio,mes,dia);
                picker.show();
            }
        });
        lmatriculas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object i=parent.getItemAtPosition(position);
                matricula=lmatriculas.getSelectedItem().toString();
                if (i!=null){
                    SeleccionOnClick_matricula(view);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        listasucursales.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if (item != null){
                    seleccion_sucursal_OnClik();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cantidad_venta.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String cantidadv=cantidad_venta.getText().toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Double et,mt;
                String cantidad=cantidad_venta.getText().toString();
                String precio = p_aterrizaje.getText().toString();
                DecimalFormat format = new DecimalFormat("#.00");

                if (!cantidad.isEmpty()){
                    double totalventa;
                    int cadena_valor = cantidad.length();

                    if (cadena_valor==2){
                        if (cantidad.charAt(0) == '0') {
                            cantidad_venta.setText(cantidad.substring(1));
                            cantidad_venta.setSelection(1, 1);
                        }
                    }

                    totalventa=Double.valueOf(cantidad) * Double.valueOf(precio);
                    total_venta.setText(String.valueOf(format.format(totalventa)));
                    }
                    else {
                    cantidad_venta.setText(String.valueOf(0));
                    int c = cantidad_venta .getText().length();
                    cantidad_venta.setSelection(c,c);

                     }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    limpiarfirma.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            firma.clearCanvas();
            Toast.makeText(activity_aterrizaje.this,"Firma Vacia",Toast.LENGTH_LONG).show();
        }
    });
    firma.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();

            switch (action) {
                case MotionEvent.ACTION_DOWN:

                    scrollView.requestDisallowInterceptTouchEvent(true);

                    return false;

                case MotionEvent.ACTION_UP:

                    scrollView.requestDisallowInterceptTouchEvent(false);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    scrollView.requestDisallowInterceptTouchEvent(true);
                    return false;

                default:
                    return true;
                }
            }
        });

    grabar.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(activity_aterrizaje.this,"Ingreso",Toast.LENGTH_LONG).show();
        }
    });

    }

    public void seleccion_sucursal_OnClik(){
        lista_sucursales s=(lista_sucursales) listasucursales.getSelectedItem();
        Toast.makeText(this,"Base Seleccionada: " + s.getCodigo_sucursal() + " " + s.toString(),Toast.LENGTH_LONG).show();
        codigo_sucursal=s.getCodigo_sucursal();

    }
    public void generar_codigo_transaccion(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.getDefault());
        String fechaHora = simpleDateFormat.format(new Date());
        codigotransaccion="";
        codigotransaccion=uname + " - " + fechaHora;
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
                nombre_usuario=ic.getProperty("nombre_usuario").toString();
                codigo_sucursal=Integer.parseInt(ic.getProperty("codigo_sucursal").toString());
                codigo_empleado= Integer.parseInt(ic.getProperty("codigo_empleado").toString());
            }

            if (codigo_sucursal==0){
                if (codigo_sucursal==0){
                   // boton_grabar.setEnabled(false);
                    Toast.makeText(getApplicationContext(),"Sin Numero de Sucursal",Toast.LENGTH_LONG).show();
                }

            }else{
                llenar_sucursales();
            }

        }catch (IOException e){
            e.printStackTrace();
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }
    }

    private void llenar_sucursales(){
        final String SOAP_ACTION ="http://tempuri.org/listado_sucursales";
        final String METHOD_NAME = "listado_sucursales";

        SoapObject resquest = new SoapObject(NAMESPACES,METHOD_NAME);
        MarshalDouble md = new MarshalDouble();
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        md.register(envelope);
        envelope.dotNet=true;
        envelope.implicitTypes=true;
        envelope.encodingStyle=SoapSerializationEnvelope.XSD;

        envelope.setOutputSoapObject(resquest);
        HttpTransportSE transportSE =new HttpTransportSE(URL);
        try {
            transportSE.call(SOAP_ACTION,envelope);
            resultRequestSOAP=(SoapObject) envelope.getResponse();
            final ArrayList<lista_sucursales> sucursales=new ArrayList<lista_sucursales>();

            int nPropiedades = resultRequestSOAP.getPropertyCount();
            for (int i = 0; i < nPropiedades; i++)
            {
                SoapObject ic = (SoapObject)resultRequestSOAP.getProperty(i);
                sucursales.add((new lista_sucursales(Integer.parseInt(ic.getProperty("codigo_sucursal").toString()),ic.getProperty("nombre_sucursal").toString())));
            }
            ArrayAdapter<lista_sucursales> l = new ArrayAdapter<lista_sucursales>(this,android.R.layout.simple_spinner_dropdown_item,sucursales);
            listasucursales.setAdapter(l);

            listasucursales.setSelection(codigo_sucursal-1);

        }catch (IOException e){
            e.printStackTrace();
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }
    }

    private void Llenar_tipocliente(){
        final String SOAP_ACTION="http://tempuri.org/consultar_tipo_cliente_aterrizaje";
        final String METHOD_NAME="consultar_tipo_cliente_aterrizaje";

        SoapObject drespuesta = new SoapObject(NAMESPACES,METHOD_NAME);
        MarshalDouble m = new MarshalDouble();
        SoapSerializationEnvelope Env = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        m.register(Env);
        Env.dotNet=true;
        Env.implicitTypes=true;
        Env.encodingStyle = SoapSerializationEnvelope.XSD;

        Env.setOutputSoapObject(drespuesta);
        HttpTransportSE tse = new HttpTransportSE(URL);
        try{
            tse.call(SOAP_ACTION,Env);
            resultRequestSOAP=(SoapObject) Env.getResponse();
            final ArrayList<lista_tipocliente> ltipocliente = new ArrayList<lista_tipocliente>();

            int noitem = resultRequestSOAP.getPropertyCount();
            for (int i=0; i<noitem; i++){
                SoapObject ltc =(SoapObject) resultRequestSOAP.getProperty(i);
                lista_tipocliente l = new lista_tipocliente();
                l.setId_tipocliente(Integer.parseInt(ltc.getProperty("idtipocliente").toString()));
                l.setTipocliente(ltc.getProperty("tipocliente").toString());
                ltipocliente.add(l);
            }
            ArrayAdapter<lista_tipocliente> tipocliente = new ArrayAdapter<lista_tipocliente>(this, android.R.layout.simple_spinner_dropdown_item,ltipocliente);

            lista_tipo_cliente.setAdapter(tipocliente);
            lista_tipo_cliente.setPrompt("Seleccion Un tipo");
            lista_tipo_cliente.setSelection(-1);

        } catch (IOException e){
            e.printStackTrace();
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }
    }
    private void llenar_matriculas(){
        final String SOAP_ACTION="http://tempuri.org/listado_matriculas";
        final String METHOD_NAME="listado_matriculas";

        SoapObject respueta = new SoapObject(NAMESPACES,METHOD_NAME);
        MarshalDouble md = new MarshalDouble();
        SoapSerializationEnvelope envelope =new SoapSerializationEnvelope(SoapEnvelope.VER11);
        md.register(envelope);
        envelope.dotNet=true;
        envelope.implicitTypes=true;
        envelope.encodingStyle=SoapSerializationEnvelope.XSD;

        envelope.setOutputSoapObject(respueta);
        HttpTransportSE transportSE = new HttpTransportSE(URL);
        try {
            transportSE.call(SOAP_ACTION,envelope);
            resultRequestSOAP=(SoapObject)  envelope.getResponse();
            final  ArrayList<lista_matriculas> listam = new ArrayList<lista_matriculas>();

            int noitem = resultRequestSOAP.getPropertyCount();
            for (int m=0; m<noitem; m++){
                SoapObject lm =(SoapObject) resultRequestSOAP.getProperty(m);
                listam.add((new lista_matriculas(Integer.parseInt(lm.getProperty("codigo_matricula").toString()),lm.getProperty("matricula").toString(),lm.getProperty("escliente").toString())));
            }
            ArrayAdapter<lista_matriculas> lm = new ArrayAdapter<lista_matriculas>(this, android.R.layout.simple_spinner_dropdown_item,listam);
            lmatriculas.setAdapter(lm);
            lmatriculas.setPrompt("Seleccionar Matricula");
            lmatriculas.setSelection(-1);
        }catch (IOException e){
            e.printStackTrace();
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }
    }
    public  void SeleccionOnClick_matricula(View v){
        SoapPrimitive rs= null;
        final String SOAP_ACTION="http://tempuri.org/tipo_cliente_matricula";
        final String METHOD_NAME="tipo_cliente_matricula";
        int codigo_tipocliente;
        lista_matriculas m=(lista_matriculas) lmatriculas .getSelectedItem();
        String matricula_select=m.toString();
        codigo_matricula = m.getCodigomatricula();
        if (codigo_matricula!=-1){
            SoapObject tipocliente = new SoapObject(NAMESPACES,METHOD_NAME);
            MarshalDouble md = new MarshalDouble();
            SoapSerializationEnvelope env=new SoapSerializationEnvelope(SoapEnvelope.VER11);
            md.register(env);
            env.dotNet=true;
            env.implicitTypes=true;
            env.encodingStyle=SoapSerializationEnvelope.XSD;
            tipocliente.addProperty("codigomatricula",codigo_matricula);
            env.setOutputSoapObject(tipocliente);
            HttpTransportSE tse = new HttpTransportSE(URL);
           try{
                tse.call(SOAP_ACTION,env);
                rs=(SoapPrimitive) env.getResponse();
                codigo_tipocliente=Integer.parseInt(rs.toString());
                codigo_tipocliente=codigo_tipocliente-1;
                if (codigo_tipocliente!=-1){
                    lista_tipo_cliente.setSelection(codigo_tipocliente);
                    buscar_precio_aterrizaje(codigo_matricula,codigo_sucursal);
                }else{
                    lista_tipo_cliente.setSelection(1);
                }

           } catch (IOException e){
               Toast.makeText(this, e.getMessage(),Toast.LENGTH_LONG).show();
           } catch (XmlPullParserException e){
               Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
           }
        }

    }
    public void buscar_precio_aterrizaje(int codigomatricula, int codigosucursal){
        SoapPrimitive rsoap=null;
        final String SOAP_ACTION="http://tempuri.org/consultar_precio_aterrizaje";
        final String METHOD_NAME="consultar_precio_aterrizaje";
        String valor_resultado;
        SoapObject rs_cp = new SoapObject(NAMESPACES,METHOD_NAME);
        MarshalDouble m = new MarshalDouble();
        SoapSerializationEnvelope env =new SoapSerializationEnvelope(SoapEnvelope.VER11);
        m.register(env);
        env.dotNet=true;
        env.implicitTypes=true;
        env.encodingStyle=SoapSerializationEnvelope.XSD;
        rs_cp.addProperty("matricula",codigomatricula);
        rs_cp.addProperty("codigosucursal",codigosucursal);
        env.setOutputSoapObject(rs_cp);
        HttpTransportSE t = new HttpTransportSE(URL);
        try {
            t.call(SOAP_ACTION,env);
            rsoap = (SoapPrimitive) env.getResponse();
            valor_resultado=rsoap.toString();
            String rv_consulta = valor_resultado.substring(0,5);
            if (rv_consulta.equals("Error")){
                grabar.setEnabled(false);
                cantidad_venta.setEnabled(false);
                p_aterrizaje.setText("0");
                Toast.makeText(this,"No se encontro precio de aterrizaje",Toast.LENGTH_LONG).show();
            } else
            {
                precio_aterrizaje=Double.parseDouble(valor_resultado);
                if (precio_aterrizaje!=0) {
                    p_aterrizaje.setText(String.valueOf(precio_aterrizaje));
                    grabar.setEnabled(true);
                    cantidad_venta.setEnabled(true);
                }
            }


        }catch (IOException e){
            Toast.makeText(this,"error: " + e.getMessage().toString(),Toast.LENGTH_LONG).show();
        }catch (XmlPullParserException e){
            Toast.makeText(this,"error: " + e.getMessage().toString(),Toast.LENGTH_LONG).show();
        }
    }
    public void consultar_codigo_producto(){
        SoapPrimitive rsoap=null;
        final String SOAP_ACTION="http://tempuri.org/codigo_producto_aterrizaje";
        final String METHOD_NAME="codigo_producto_aterrizaje";

        SoapObject rs_cp = new SoapObject(NAMESPACES,METHOD_NAME);
        MarshalDouble m = new MarshalDouble();
        SoapSerializationEnvelope env =new SoapSerializationEnvelope(SoapEnvelope.VER11);
        m.register(env);
        env.dotNet=true;
        env.implicitTypes=true;
        env.encodingStyle=SoapSerializationEnvelope.XSD;

        env.setOutputSoapObject(rs_cp);
        HttpTransportSE t = new HttpTransportSE(URL);
        try {
            t.call(SOAP_ACTION,env);
            rsoap=(SoapPrimitive) env.getResponse();
            codigoproducto=Integer.parseInt(rsoap.toString());
            codigo_producto.setText(String.valueOf(codigoproducto));
        }catch (IOException e){
            Toast.makeText(this,"error: " + e.getMessage().toString(),Toast.LENGTH_LONG).show();
        }catch (XmlPullParserException e){
            Toast.makeText(this,"error: " + e.getMessage().toString(),Toast.LENGTH_LONG).show();
        }
    }
    public void buscar_nombre_producto(int codigo){
        SoapPrimitive rs=null;
        final String SOAP_ACTION="http://tempuri.org/nombre_producto";
        final String METHOD_NAME="nombre_producto";
        SoapObject respueta_producto = new SoapObject(NAMESPACES,METHOD_NAME);
        MarshalDouble m = new MarshalDouble();
        SoapSerializationEnvelope env = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        m.register(env);
        env.dotNet=true;
        env.implicitTypes=true;
        env.encodingStyle=SoapSerializationEnvelope.XSD;
        respueta_producto.addProperty("codigoproducto",codigo);
        env.setOutputSoapObject(respueta_producto);
        HttpTransportSE t = new HttpTransportSE(URL);
        try{
            t.call(SOAP_ACTION,env);
            rs=(SoapPrimitive) env.getResponse();
            nombreproducto.setText(rs.toString());
        }catch (IOException e){
            Toast.makeText(this,"error: " + e.getMessage().toString(),Toast.LENGTH_LONG).show();
        }catch (XmlPullParserException e){
            Toast.makeText(this,"error: " + e.getMessage().toString(),Toast.LENGTH_LONG).show();
        }
    }
}