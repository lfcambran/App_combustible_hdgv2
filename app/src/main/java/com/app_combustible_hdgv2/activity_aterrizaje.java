package com.app_combustible_hdgv2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.app_combustible_hdgv2.utilidades.varibles_globales;
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

import java.io.ByteArrayOutputStream;
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
    String uname,codigotransaccion,nombre_usuario = null,matricula,mensaje_error;
    EditText fecha_doc,codigo_producto,p_aterrizaje,cantidad_venta,total_venta,correo_cliente,observacion,recibidopor;
    TextView nombreproducto;
    double precio_aterrizaje,longitud,latitud;;;
    DatePickerDialog picker;
    Spinner listasucursales,lmatriculas,lista_tipo_cliente;
    int codigo_sucursal,codigo_empleado,codigo_matricula,codigoproducto,noerror,codigo_tipocliente;
    Button grabar,limpiarfirma;
    SignatureView firma;
    ScrollView scrollView;
    Boolean correo_valido;
    ProgressDialog progressDoalog;
    varibles_globales gb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aterrizaje);
        setTitle("Registro de Aterrizajes");
        gb = new varibles_globales();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date date = new Date();
        String fecha = dateFormat.format(date);
        scrollView= (ScrollView) findViewById(R.id.sview);
        scrollView.requestDisallowInterceptTouchEvent(true);
        uname=getIntent().getStringExtra("nombre_usuario");
        nombreproducto=findViewById(R.id.nombreproducto);
        codigo_producto=findViewById(R.id.cproducto);
        fecha_doc=findViewById(R.id.fecha_doc);
        fecha_doc.setInputType(InputType.TYPE_NULL);
        fecha_doc.setText(fecha);
        listasucursales=findViewById(R.id.sucursal);
        lmatriculas=findViewById(R.id.matriculas);
        lista_tipo_cliente=findViewById(R.id.tipocliente);
        p_aterrizaje=findViewById(R.id.precio);
        grabar=findViewById(R.id.grabar);
        limpiarfirma=findViewById(R.id.limpiarf);
        cantidad_venta=findViewById(R.id.cantidad);
        total_venta=findViewById(R.id.total_aterrizaje);
        correo_cliente=findViewById(R.id.correo_cliente);
        observacion=findViewById(R.id.observaciones);
        recibidopor=findViewById(R.id.recibidopor);
        cantidad_venta.setEnabled(false);
        longitud=gb.getlongitu();
        latitud=gb.getlatitud();
        consultar_datos(uname);
        llenar_matriculas();
        Llenar_tipocliente();
        consultar_codigo_producto();
        buscar_nombre_producto(codigoproducto);
        generar_codigo_transaccion();
        firma=findViewById(R.id.signature_view);
        fecha_doc.setOnClickListener(new View.OnClickListener() {
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
                        fecha_doc.setText(dia_cero + "-" + mes_cero + "-" + year);
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

            AlertDialog alertDialog = new AlertDialog.Builder(activity_aterrizaje.this)
                    .setIcon(R.drawable.iconapp)
                    .setTitle("Generar Documento de Aterrizaje")
                    .setMessage("Se Creara el documento de Aterrizaje a la matricual: " + matricula + " Desea Continuar")
                    .setCancelable(false)
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            creacion_documento();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(),"Accion Cancelada", Toast.LENGTH_LONG).show();
                        }
                    }).show();

        }
    });

    correo_cliente.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {


        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            correo_valido=validar_correo(correo_cliente.getText().toString());
        }
    });
    correo_cliente.setOnKeyListener(new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction()==KeyEvent.ACTION_DOWN) && (keyCode==KeyEvent.KEYCODE_ENTER)){
                correo_valido=validar_correo(correo_cliente.getText().toString());
                HideKeyboard(correo_cliente);
            }
                return false;
        }
    });
    }
    Handler handle=new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            progressDoalog.incrementProgressBy(1);
        }
    };
    private void barra_progreso(){
        progressDoalog=new ProgressDialog(activity_aterrizaje.this);
        progressDoalog.setMax(100);
        progressDoalog.setIcon(R.drawable.iconapp);
        progressDoalog.setIndeterminate(true);
        progressDoalog.setMessage("Procesando.. por favor espere");
        progressDoalog.setTitle("Generando Documento");
        progressDoalog.setCancelable(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while (progressDoalog.getProgress() <= progressDoalog.getMax()) {
                        Thread.sleep(200);
                        handle.sendMessage(handle.obtainMessage());
                        if (progressDoalog.getProgress() == progressDoalog.getMax()) {
                            progressDoalog.dismiss();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
        progressDoalog.show();
    }

    private void creacion_documento(){
        if (validar_datos()==true){
            barra_progreso();
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                SoapPrimitive resultRequestSOAP=null;
                                String SOAP_ACTION = "http://tempuri.org/insertar_aterrizaje";
                                String METHOD_NAME = "insertar_aterrizaje";

                                Bitmap SignBitmap=firma.getSignatureBitmap();
                                String fechadoc = fecha_doc.getText().toString();
                                String precio_a= p_aterrizaje.getText() .toString();
                                String cantidad_a = cantidad_venta.getText().toString();
                                String copia_correo_cliente=correo_cliente.getText().toString();
                                String observaciones = observacion.getText().toString();
                                String byte_str=Convertir_imagen(SignBitmap);

                                SoapObject respuesta=new SoapObject(NAMESPACES,METHOD_NAME);
                                MarshalDouble md = new MarshalDouble();
                                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                md.register(envelope);
                                envelope.dotNet = true;
                                envelope.implicitTypes=true;
                                envelope.encodingStyle = SoapSerializationEnvelope.XSD;


                                if (copia_correo_cliente.length()>0) {
                                    respuesta.addProperty("correocliente", copia_correo_cliente);
                                }else if (copia_correo_cliente.length()==0){
                                    respuesta.addProperty("correocliente","Sin Correo");
                                }

                                respuesta.addProperty("tipocliente",codigo_tipocliente);
                                respuesta.addProperty("cmatricula",codigo_matricula);
                                respuesta.addProperty("codigoempleado",codigo_empleado);
                                respuesta.addProperty("codigosucursal",codigo_sucursal);
                                respuesta.addProperty("fecha",fechadoc);
                                respuesta.addProperty("creadopor",uname);
                                respuesta.addProperty("observacion",observaciones);
                                respuesta.addProperty("recibidopor",recibidopor.getText().toString());
                                respuesta.addProperty("firma",byte_str);
                                respuesta.addProperty("longitud",longitud);
                                respuesta.addProperty("latitud",latitud);
                                respuesta.addProperty("ctransaccion",codigotransaccion);
                                respuesta.addProperty("codigoarticulo",codigoproducto);
                                respuesta.addProperty("cantidad",Integer.valueOf( cantidad_a));
                                respuesta.addProperty("valor",Double.valueOf(precio_a));
                                respuesta.addProperty("usuario",uname);


                                envelope.setOutputSoapObject(respuesta);

                                HttpTransportSE transportSE=new HttpTransportSE(URL);
                                transportSE.call(SOAP_ACTION,envelope);
                                resultRequestSOAP=(SoapPrimitive) envelope.getResponse();

                                String respuesta_insert = resultRequestSOAP.toString();
                                String respuet = respuesta_insert.toString();
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity_aterrizaje.this);
                                builder.setCancelable(false);
                                builder.setIcon(R.drawable.iconapp);

                                if (respuet.equals("ok")){
                                    progressDoalog.dismiss();
                                    runOnUiThread(new Thread(){
                                        public void run(){
                                            builder.setTitle("Documento De Aterrizaje");
                                            builder.setMessage("Se ha Generado el docuemento de Aterrizaje correctamente");
                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(activity_aterrizaje.this,MainActivity.class);
                                                    intent.putExtra("nombre_usuario",uname);
                                                    startActivity(intent);
                                                    setResult(Activity.RESULT_OK);
                                                    finish();
                                                }
                                            });
                                            AlertDialog alertDialog =builder.create();
                                            alertDialog.show();
                                            alertDialog.getWindow().setGravity(Gravity.CENTER);
                                        }
                                    });
                                }else{
                                    progressDoalog.dismiss();
                                    runOnUiThread(new Thread(){
                                        public void run(){
                                            builder.setTitle("Error al Emitir documento");
                                            builder.setMessage("Error: " + respuet);
                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(activity_aterrizaje.this,MainActivity.class);
                                                    intent.putExtra("nombre_usuario",uname);
                                                    startActivity(intent);
                                                    setResult(Activity.RESULT_OK);
                                                    Toast.makeText(getApplicationContext(), "Error: " + respuet.toString(), Toast.LENGTH_LONG).show();
                                                    finish();
                                                }
                                            });
                                            AlertDialog alertDialog =builder.create();
                                            alertDialog.show();
                                            alertDialog.getWindow().setGravity(Gravity.CENTER);
                                        }
                                    });
                                }

                            }catch (IOException e){
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                            }catch (XmlPullParserException c){
                                Toast.makeText(getApplicationContext(),c.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    }

            ).start();
        }else{
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setIcon(R.drawable.iconapp);
            b.setTitle("Error al generar documento");
            b.setMessage(mensaje_error);
            b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(),"Error al grabar",Toast.LENGTH_LONG).show();

                }
            });
            AlertDialog alertDialog=b.create();
            alertDialog.show();
            alertDialog.getWindow().setGravity(Gravity.CENTER);
        }
    }

    private boolean validar_datos(){
        float cantidad,precio;
        mensaje_error="";
        noerror=0;
        String carticulo = codigo_producto.getText().toString();
        String precio_a = p_aterrizaje.getText().toString();
        String cantidad_a=cantidad_venta.getText().toString();
        String correocliente= correo_cliente.getText().toString();
        String recibido=recibidopor.getText().toString();

        if (TextUtils.isEmpty(precio_a)){
            precio =0;
        }else{
            precio = Float.parseFloat(precio_a);
        }

        if (TextUtils.isEmpty(cantidad_a)){
            cantidad=0;
        }else {
            cantidad=Float.parseFloat(cantidad_a);
        }
        if (recibido.length()==0){
            mensaje_error=mensaje_error + " Debe de Especificar quien recibe el servicio " + "\n";
            noerror+=1;
        }

        if (correocliente.length()>0){
            if (correo_valido==false){
                mensaje_error=mensaje_error + " El Correo del cliente es incorrecto" + "\n";
                noerror +=1;
            }
        }

        if (firma.isBitmapEmpty()){
            mensaje_error=mensaje_error + " El Documento no se encuentra firmado";
            noerror +=1;
        }

        if(carticulo.length()==0){
            mensaje_error=mensaje_error + " Debe de Ingresar el codigo del producto" + "\n";
            noerror+=1;
        }
        if (precio==0){
            mensaje_error=mensaje_error + " El precio de venta no puede Cero" + "\n";
            noerror+=1;
        }
        if (cantidad==0){
            mensaje_error = mensaje_error + " La cantidad de venta no puede ser cero" + "\n";
            noerror+=1;
        }
        if (codigo_matricula==-1){
            mensaje_error = mensaje_error + " Debe de seleccionar una matricula" + "\n";
            noerror+=1;
        }

        if (noerror==0) {
            return true;
        }else {
            return false;
        }
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
    public String Convertir_imagen(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
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
                int ctc;
                codigo_tipocliente=Integer.parseInt(rs.toString());
                ctc=codigo_tipocliente-1;
                if (codigo_tipocliente!=-1){
                    lista_tipo_cliente.setSelection(ctc);
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
    private void HideKeyboard(View v){
        InputMethodManager imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
    }
    private Boolean validar_correo(String correo){
        String emailRegex="^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        if (correo.matches(emailRegex)){
            return true;
        }else{
            return false;
        }
    }
}