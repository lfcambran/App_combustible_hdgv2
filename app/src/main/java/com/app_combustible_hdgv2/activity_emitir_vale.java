package com.app_combustible_hdgv2;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.viewmodel.CreationExtras;
import androidx.navigation.ui.AppBarConfiguration;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.app_combustible_hdgv2.databinding.ActivityEmitirValeBinding;
import com.app_combustible_hdgv2.ui.login.LoginActivity;
import com.app_combustible_hdgv2.utilidades.CaptureBitmapView;
import com.app_combustible_hdgv2.utilidades.MarshalDouble;
import com.app_combustible_hdgv2.utilidades.varibles_globales;
import com.google.android.material.snackbar.Snackbar;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.app_combustible_hdgv2.utilidades.lista_sucursales;
import com.app_combustible_hdgv2.utilidades.lista_matriculas;
import com.app_combustible_hdgv2.utilidades.lista_tanque_sucursal;
import com.app_combustible_hdgv2.utilidades.tipo_movimiento;

public class activity_emitir_vale extends AppCompatActivity   {
    int codigom,tmov,noerror;
    Boolean correo_valido;
    private SoapObject resultRequestSOAP = null;
    public static final int SIGNATURE_ACTIVITY = 1;
    final int REQUEST_ACTION_CAMERA = 9;
    final String URL = "http://200.30.144.133:3427/wsite_c/wsb_combustible_hdg/ws_datos_combustible.asmx";
    final String NAMESPACES = "http://tempuri.org/";
    EditText fecha_vale,codigo_producto,preciogalon,totalvale,cantidad_venta,npiloto,correo_cliente;
    DatePickerDialog picker;
    Button boton_grabar,boton_limpiar,boton_tomar_foto;
    TextView nameuser,existencia,metrot,nombre_producto;
    String uname,codigotransaccion,mensaje_error,nombre_usuario = null,mCurrentPhotoPath;;
    Spinner listasucursales,lmatriculas,ltanques,tipomovimiento;
    int codigo_sucursal,codigo_empleado,codigo_tanque_select,codigoproducto;
    double existencia_tanque,metro_tanque, longitud,latitud;;
    private ActivityEmitirValeBinding binding;
    private Uri photoURI;
    Bitmap foto_tanque;
    varibles_globales gb;
    ProgressDialog progressDoalog;

    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emitir_vale);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding=ActivityEmitirValeBinding.inflate(getLayoutInflater());
        gb = new varibles_globales();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date date = new Date();
        String fecha = dateFormat.format(date);
        setTitle("Emision de Vale");
        setContentView(binding.getRoot());
        scrollView= (ScrollView) findViewById(R.id.sview);
        scrollView.requestDisallowInterceptTouchEvent(true);
        uname=getIntent().getStringExtra("nombre_usuario");
        boton_grabar= (Button) findViewById(R.id.grabar);
        boton_limpiar = (Button) findViewById(R.id.limpiarf);
        boton_tomar_foto=(Button) findViewById(R.id.tomar_foto);
        fecha_vale=findViewById(R.id.fecha_vale);
        totalvale = findViewById(R.id.total_vale);
        cantidad_venta=findViewById(R.id.cantidad);
        fecha_vale.setInputType(InputType.TYPE_NULL);
        fecha_vale.setText(fecha);
        npiloto=findViewById(R.id.nombrepiloto);
        correo_cliente=findViewById(R.id.correo_cliente);
        nameuser=findViewById(R.id.nauser);
        listasucursales=findViewById(R.id.sucursal);
        lmatriculas=findViewById(R.id.matriculas);
        ltanques=findViewById(R.id.tanques);
        existencia=findViewById(R.id.existencia);
        metrot=findViewById(R.id.metrotanque);
        tipomovimiento=findViewById(R.id.tipomovimiento);
        llenar_tipomovimiento();
        consultar_codigo_producto();
        nombre_producto=findViewById(R.id.nombreproducto);
        codigo_producto=findViewById(R.id.cproducto);
        codigo_producto.setText(String.valueOf(codigoproducto));
        preciogalon=findViewById(R.id.precio);
        buscar_nombre_producto(codigoproducto);
        consultar_datos(uname);
        llenar_matriculas();
        generar_codigo_transaccion();
        nameuser.setText("Nombre: " + codigo_empleado + " - " + nombre_usuario);
        longitud=gb.getlongitu();
        latitud=gb.getlatitud();
        cantidad_venta.setEnabled(false);

        if (codigo_sucursal==1 || codigo_sucursal==5 ){
            boton_tomar_foto.setEnabled(false);
        }
        binding.limpiarf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.signatureView.clearCanvas();
                Toast.makeText(activity_emitir_vale.this,"Firma Vacia",Toast.LENGTH_LONG).show();
            }
        });

        binding.tomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(activity_emitir_vale.this,CAMERA)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        activity_emitir_vale.this,WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity_emitir_vale.this, new String[]{CAMERA},1);
                } else {
                    File mi_foto = null;
                    try {
                        mi_foto = createImageFile();
                    } catch (IOException ex) {
                        Toast.makeText(activity_emitir_vale.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    if (mi_foto != null) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.TITLE, "MyPicture");
                        values.put(MediaStore.Images.Media.DESCRIPTION, "Photo taken on " + System.currentTimeMillis());
                        photoURI = getContentResolver().insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                        photoURI = getContentResolver().insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(cameraIntent, REQUEST_ACTION_CAMERA);
                    }
                }
            }
        });

        binding.signatureView.setOnTouchListener(new View.OnTouchListener(){

            @SuppressLint("ClickableViewAccessibility")
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
        fecha_vale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr=Calendar.getInstance();
                int dia = cldr.get(Calendar.DAY_OF_MONTH);
                int mes = cldr.get(Calendar.MONTH);
                int anio = cldr.get(Calendar.YEAR);

                picker =new DatePickerDialog(activity_emitir_vale.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    fecha_vale.setText(dayOfMonth + "/" + (month+1) + "/" + year);
                    }
                },anio,mes,dia);
                picker.show();
            }
        });


        listasucursales.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if (item != null) {
                    //Toast.makeText(parent.getContext(), "onItemSelected"+ item.toString(), Toast.LENGTH_SHORT).show();
                    SeleccionOnclick(view);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ltanques.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item=parent.getItemAtPosition(position);
                if (item!=null){
                    SeleccionOnClick_tanque(view);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        lmatriculas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
             Object i=parent.getItemAtPosition(position);
             if (i!=null){
                 SeleccionOnClick_matricula(view);
             }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        codigo_producto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String codigoproducto = codigo_producto.getText().toString();
                if (codigoproducto.length()>0) {
                    buscar_nombre_producto(Integer.parseInt(codigoproducto));
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    consultar_precio_combustible(Integer.parseInt(codigo_producto.getText().toString()),codigom,codigo_sucursal,codigo_tanque_select,tmov);
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }


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
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    correo_valido=validar_correo(correo_cliente.getText().toString());
                    HideKeyboard(correo_cliente);
                    return true;
                }
                return false;
            }
        });
        cantidad_venta.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String cantidadv = cantidad_venta.getText().toString();


            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Double et,mt;
                String cantidad=cantidad_venta.getText().toString();
                String precio = preciogalon.getText().toString();
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
                    totalvale.setText(String.valueOf(format.format(totalventa)));
                    mt=metro_tanque+Double.valueOf(cantidad);
                    et=existencia_tanque-Double.valueOf(cantidad);
                    metrot.setText("Metro:" + String.valueOf(format.format(mt)));
                    existencia.setText("Existencia: " + String.valueOf(format.format(et)));
                } else {
                    cantidad_venta.setText(String.valueOf(0));
                    int c = cantidad_venta .getText().length();
                    cantidad_venta.setSelection(c,c);

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }
    private void HideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private boolean validar_correo(String correo)
    {
        String emailRegex ="^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        if(correo.matches(emailRegex))
        {
            return true;
        }else {
        return false;
        }
    }
    public void grabar_vale(View View){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
        .setIcon(R.drawable.iconapp)
        .setTitle("Creacion de Documento")
        .setMessage("Desea Crear el documento?")
        .setCancelable(false)
        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                creacion_vale();
            }
        })
          .setNegativeButton("No", new DialogInterface.OnClickListener(){

              @Override
              public void onClick(DialogInterface dialog, int which) {
                  Toast.makeText(getApplicationContext(),"Accion Cancelada", Toast.LENGTH_LONG).show();
              }
          })
                .show();
    }

    private void creacion_vale(){
        if(validar_datos()==true){
            barrar_progreso();
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {

                                SoapPrimitive resultRequestSOAP = null;
                                String SOAP_ACTION = "http://tempuri.org/insertar_vale";
                                String METHOD_NAME = "insertar_vale";
                                Bitmap signBitmap = binding.signatureView.getSignatureBitmap();
                                String fechavale = fecha_vale.getText().toString();
                                String nombrepioto =npiloto.getText().toString();
                                String precio_g = preciogalon.getText().toString();
                                String cantidad_v = cantidad_venta .getText().toString();
                                String copia_correo_cliente = correo_cliente.getText().toString();


                                SoapObject respuesta = new SoapObject(NAMESPACES,METHOD_NAME);
                                MarshalDouble md = new MarshalDouble();
                                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                md.register(envelope);
                                envelope.dotNet = true;
                                envelope.implicitTypes=true;
                                envelope.encodingStyle = SoapSerializationEnvelope.XSD;
                                String fotografia;

                                String byte_str=convertir_imagen_byte(signBitmap);
                                if (codigo_sucursal==1 || codigo_sucursal==5 ){
                                    fotografia = "";
                                }else {
                                    fotografia=convertir_imagen_byte(foto_tanque);

                                }

                                respuesta.addProperty( "tipom",tmov);
                                respuesta.addProperty("codsucursal",codigo_sucursal);
                                respuesta.addProperty("matricula",codigom );
                                respuesta.addProperty("despachador",codigo_empleado);
                                respuesta.addProperty("fecha", fechavale );
                                respuesta.addProperty("usuario", uname);
                                respuesta.addProperty("piloto",nombrepioto);
                                respuesta.addProperty("ctransaccion", codigotransaccion);
                                respuesta.addProperty("producto",codigoproducto);
                                respuesta.addProperty("cantidad", cantidad_v );
                                respuesta.addProperty("valor", precio_g);
                                respuesta.addProperty("notanque",codigo_tanque_select);
                                respuesta.addProperty("img_firma",byte_str);
                                respuesta.addProperty("longitud",longitud);
                                respuesta.addProperty("latitud",latitud);

                                if (codigo_sucursal==1 || codigo_sucursal==5){
                                    respuesta.addProperty("fotografia","Sin Imagen");
                                }else {
                                    respuesta.addProperty("fotografia", fotografia);
                                }
                                if (copia_correo_cliente.length()>0) {
                                    respuesta.addProperty("correo_cliente", copia_correo_cliente);
                                }else if (copia_correo_cliente.length()==0){
                                    respuesta.addProperty("correo_cliente","Sin Correo");
                                }
                                envelope.setOutputSoapObject(respuesta);

                                HttpTransportSE transportSE = new HttpTransportSE(URL);
                                transportSE.call(SOAP_ACTION,envelope);
                                resultRequestSOAP =(SoapPrimitive) envelope.getResponse();

                                String res_insert = resultRequestSOAP.toString();
                                String respuet = res_insert.toString();

                                AlertDialog.Builder builder = new AlertDialog.Builder(activity_emitir_vale.this);
                                builder.setCancelable(false);
                                builder.setIcon(R.drawable.iconapp);

                                if (respuet.equals("ok")){
                                    progressDoalog.dismiss();
                                    runOnUiThread(new Thread(){
                                        public void run(){
                                            builder.setTitle("Documento De Combustible");
                                            builder.setMessage("Se ha Generado el vale correctamente");
                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(activity_emitir_vale.this,MainActivity.class);
                                                    intent.putExtra("nombre_usuario",uname);
                                                    startActivity(intent);
                                                    setResult(Activity.RESULT_OK);
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
                                            builder.setTitle("Error al Emitir Vales");
                                            builder.setMessage("Error: " + respuet);
                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(activity_emitir_vale.this,MainActivity.class);
                                                    intent.putExtra("nombre_usuario",uname);
                                                    startActivity(intent);
                                                    setResult(Activity.RESULT_OK);
                                                    Toast.makeText(getApplicationContext(), "Error: " + respuet.toString(), Toast.LENGTH_LONG).show();

                                                }
                                            });
                                            AlertDialog alertDialog =builder.create();
                                            alertDialog.show();
                                            alertDialog.getWindow().setGravity(Gravity.CENTER);
                                        }
                                    });

                                }
                                ///
                            }catch (IOException e) {
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                            }catch (XmlPullParserException c){
                                Toast.makeText(getApplicationContext(),c.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    }
            ).start();


        }else{
            AlertDialog.Builder builder =new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.iconapp);
            builder.setTitle("Se ha generado un error");
            builder.setMessage(mensaje_error);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(getApplicationContext(), "Error a grabar", Toast.LENGTH_LONG).show();
                }
            });
            AlertDialog alertDialog =builder.create();
            alertDialog.show();
            alertDialog.getWindow().setGravity(Gravity.CENTER);

        }
    }
    private void barrar_progreso(){

        progressDoalog=new ProgressDialog(activity_emitir_vale.this);
        progressDoalog.setMax(100);
        progressDoalog.setIcon(R.drawable.iconapp);
        progressDoalog.setIndeterminate(true);
        progressDoalog.setMessage("Procesando.. por favor espere");
        progressDoalog.setTitle("Generando Documento");
        progressDoalog.setCancelable(false);


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (progressDoalog.getProgress() <= progressDoalog.getMax()) {
                        Thread.sleep(200);
                        handle.sendMessage(handle.obtainMessage());
                        if (progressDoalog.getProgress() == progressDoalog.getMax()) {
                            progressDoalog.dismiss();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        progressDoalog.show();
    }
    Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progressDoalog.incrementProgressBy(1);
        }
    };

    private boolean validar_datos(){
        float cantidad;
        float precio;
        mensaje_error="";
        noerror=0;
        String codigoproducto = codigo_producto.getText().toString();
        String nombrepiloto = npiloto.getText().toString();
        String precio_g = preciogalon.getText().toString();
        String cantidad_v = cantidad_venta .getText().toString();
        String CorreoCliente = correo_cliente.getText().toString();

        if (precio_g.length()==0 || precio_g==null ){
            precio=0;
        }else {
             precio=Float.parseFloat(precio_g);
        }
        if (TextUtils.isEmpty( cantidad_v)){
            cantidad=0;
        }else {
             cantidad=Float.parseFloat(cantidad_v);
        }

        if (CorreoCliente.length()>0){
            if (correo_valido==false){
                mensaje_error=mensaje_error + " El Correo del cliente es incorrecto" + "\n";
                noerror +=1;
            }
        }

        if (binding.signatureView.isBitmapEmpty()){
            mensaje_error=mensaje_error + " El documento no se encuentra firmado" + "\n";
            noerror+=1;
        }
        if (codigo_sucursal!=1 && codigo_sucursal!=5 ){
            if (foto_tanque==null){
                mensaje_error=mensaje_error + " No se encontro ninguna foto de tanque" + "\n";
                noerror+=1;
            }
        }

        if (codigoproducto.length()==0){
            mensaje_error=mensaje_error + " Debe de Ingresar el codigo del producto" + "\n";
            noerror+=1;
        }
        if (nombrepiloto.length()==0){
            mensaje_error=mensaje_error + " Debe de Ingresar el nombre del Piloto" + "\n";
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
        if (codigom==-1){
            mensaje_error = mensaje_error + " Debe de seleccionar una matricula" + "\n";
            noerror+=1;
        }

         if (noerror==0) {
             return true;
         }else {
             return false;
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
                nombre_usuario=ic.getProperty("nombre_usuario").toString();
                codigo_sucursal=Integer.parseInt(ic.getProperty("codigo_sucursal").toString());
                codigo_empleado= Integer.parseInt(ic.getProperty("codigo_empleado").toString());
            }

           if (codigo_sucursal==0){
               if (codigo_sucursal==0){
                   boton_grabar.setEnabled(false);
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

    private void llenar_tipomovimiento(){
        final String SOAP_ACTION = "http://tempuri.org/lista_tipo_movimientos";
        final String METHOD_NAME = "lista_tipo_movimientos";

        SoapObject r = new SoapObject(NAMESPACES,METHOD_NAME);
        MarshalDouble m = new MarshalDouble();
        SoapSerializationEnvelope env = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        m.register(env);
        env.dotNet=true;
        env.implicitTypes=true;
        env.encodingStyle=SoapSerializationEnvelope.XSD;

        env.setOutputSoapObject(r);
        HttpTransportSE t = new HttpTransportSE(URL);
        try {
            t.call(SOAP_ACTION,env);
            resultRequestSOAP = (SoapObject) env.getResponse();
            final ArrayList<tipo_movimiento>tm=new ArrayList<tipo_movimiento>();
            int np = resultRequestSOAP.getPropertyCount();
            for (int i = 0; i < np; i++)
            {
                SoapObject  ltm = (SoapObject)resultRequestSOAP.getProperty(i);
                tm.add((new tipo_movimiento(Integer.parseInt(ltm.getProperty("codigo_movimiento").toString()),ltm.getProperty("tipomovimiento").toString() )));
            }
            ArrayAdapter<tipo_movimiento> t_mv =new ArrayAdapter<tipo_movimiento>(this, android.R.layout.simple_spinner_dropdown_item,tm);
            tipomovimiento.setAdapter(t_mv);
            tipomovimiento.setSelection(0);

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
        final ArrayList<lista_sucursales>sucursales=new ArrayList<lista_sucursales>();

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
    public void SeleccionOnclick(View v){
        lista_sucursales sucursales = (lista_sucursales) listasucursales.getSelectedItem();
        Toast.makeText(this, "Seleccion " + sucursales.getCodigo_sucursal() + " " + sucursales.toString(),Toast.LENGTH_LONG).show();
        codigo_sucursal=sucursales.getCodigo_sucursal();
        llenar_tanques(codigo_sucursal);
        if (codigo_sucursal==1 || codigo_sucursal==5 ){
            boton_tomar_foto.setEnabled(false);
        }else {
            boton_tomar_foto.setEnabled(true);
        }
    }

    public void SeleccionOnClick_tanque(View v){
        lista_tanque_sucursal tanques = (lista_tanque_sucursal) ltanques.getSelectedItem();
        Toast.makeText(this, "tanque " + tanques.getCodigo_tanque() + " " + tanques.toString(),Toast.LENGTH_LONG).show();
        codigo_tanque_select=tanques.getCodigo_tanque();

        buscar_existencia(codigo_tanque_select);
        buscar_metro(codigo_tanque_select);
        buscar_codigo_producto(codigo_tanque_select);
        //buscar metro-existencia
    }
    public void SeleccionOnClick_matricula(View v){
        //evaluar tipo de movimiento
        SoapPrimitive rs=null;
        int tipo;

        final String SOAP_ACTION="http://tempuri.org/tipo_m_matricula";
        final String METHOD_NAME="tipo_m_matricula";
        lista_matriculas m=(lista_matriculas) lmatriculas.getSelectedItem();
        String matricula_sel=m.toString();
        codigom = m.getCodigomatricula();
        SoapObject rtm=new SoapObject(NAMESPACES,METHOD_NAME);
        MarshalDouble md = new MarshalDouble();
        SoapSerializationEnvelope env=new SoapSerializationEnvelope(SoapEnvelope.VER11);
        md.register(env);
        env.dotNet=true;
        env.implicitTypes=true;
        env.encodingStyle=SoapSerializationEnvelope.XSD;
        rtm.addProperty("matricula",matricula_sel);
        env.setOutputSoapObject(rtm);
        HttpTransportSE transportSE = new HttpTransportSE(URL);
        try {
            transportSE.call(SOAP_ACTION, env);
            rs = (SoapPrimitive) env.getResponse();
            tipo=Integer.parseInt(rs.toString());
            preciogalon.setText("0");
            if (tipo==0){
                tipomovimiento.setSelection(1);
                consultar_precio_combustible(codigoproducto,codigom,codigo_sucursal,codigo_tanque_select,1);
            }else if(tipo==1){
                tipomovimiento.setSelection(2);
            }

            tipomovimiento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Object test = tipomovimiento.getSelectedItem();
                    Toast.makeText(activity_emitir_vale.this,
                            tipomovimiento.getItemAtPosition(i).toString(), Toast.LENGTH_LONG).show();
                    tipo_movimiento tm=(tipo_movimiento) test;
                        tmov=tm.getCodigomovimiento();

                    if (codigo_tanque_select!=0) {
                        consultar_precio_combustible(codigoproducto, codigom, codigo_sucursal, codigo_tanque_select, tmov);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

        }catch (IOException e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }catch (XmlPullParserException e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }

    }

    public void consultar_codigo_producto(){
       SoapPrimitive rsoap=null;
       final String SOAP_ACTION="http://tempuri.org/codigoproducto";
       final String METHOD_NAME="codigoproducto";

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
            nombre_producto.setText(rs.toString());
        }catch (IOException e){
            Toast.makeText(this,"error: " + e.getMessage().toString(),Toast.LENGTH_LONG).show();
        }catch (XmlPullParserException e){
            Toast.makeText(this,"error: " + e.getMessage().toString(),Toast.LENGTH_LONG).show();
        }
    }
    public void  buscar_codigo_producto(int codtanque){
        SoapPrimitive resultado = null;
        final String SOAP_Action = "http://tempuri.org/codigo_producto_tanque_sel";
        final String Method_name = "codigo_producto_tanque_sel";
        int codigo_producto_tanque=0;
        SoapObject respuesta_codigo_p= new SoapObject(NAMESPACES,Method_name);
        MarshalDouble md =  new MarshalDouble();
        SoapSerializationEnvelope env=new SoapSerializationEnvelope(SoapEnvelope.VER11);
        md.register(env);
        env.dotNet=true;
        env.implicitTypes=true;
        env.encodingStyle=SoapSerializationEnvelope.XSD;
        respuesta_codigo_p.addProperty("codigo_tanque",codtanque);
        env.setOutputSoapObject(respuesta_codigo_p);
        HttpTransportSE transportSE=new HttpTransportSE(URL);
        try{
            transportSE.call(SOAP_Action,env);
            resultado = (SoapPrimitive) env.getResponse();
            codigo_producto_tanque=Integer.parseInt(resultado.toString());
            if (codigo_producto_tanque==0){
                codigo_producto.setText(String.valueOf(codigoproducto));
                buscar_nombre_producto(codigoproducto);
            } else {
            codigo_producto.setText(String.valueOf(codigo_producto_tanque));
            buscar_nombre_producto(codigo_producto_tanque);
            }
        }catch (IOException e){
            Toast.makeText(this,"error: " + e.getMessage().toString(),Toast.LENGTH_LONG).show();
        }catch (XmlPullParserException e){
            Toast.makeText(this,"error: " + e.getMessage().toString(),Toast.LENGTH_LONG).show();
        }
    }
    public void buscar_metro(int codtanque){
        SoapPrimitive resultado_Soap = null;
        final String SOAP_ACTION="http://tempuri.org/metro_tanque";
        final String METHOD_NAME="metro_tanque";

        SoapObject resputa_metro=new SoapObject(NAMESPACES,METHOD_NAME);
        MarshalDouble md = new MarshalDouble();
        SoapSerializationEnvelope env=new SoapSerializationEnvelope(SoapEnvelope.VER11);
        md.register(env);
        env.dotNet=true;
        env.implicitTypes=true;
        env.encodingStyle=SoapSerializationEnvelope.XSD;
        resputa_metro.addProperty("codigotanque",codtanque);
        resputa_metro.addProperty("sucursal",codigo_sucursal);
        env.setOutputSoapObject(resputa_metro);
        HttpTransportSE transportSE = new HttpTransportSE(URL);
        try {
            transportSE.call(SOAP_ACTION,env);
            resultado_Soap=(SoapPrimitive) env.getResponse();
            metro_tanque=Double.parseDouble(resultado_Soap.toString());
            metrot.setText("Metro: "+ String.valueOf(metro_tanque));

        }catch (IOException e){
            e.printStackTrace();
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }

    }
    public void buscar_existencia(int codtanque){
        SoapPrimitive resultadaSoap =null;
        final String SOAP_ACTION="http://tempuri.org/datos_existencia_tanque";
        final String METHOD_NAME="datos_existencia_tanque";

        SoapObject respuesta_existencia=new SoapObject(NAMESPACES,METHOD_NAME);
        MarshalDouble md =new MarshalDouble();
        SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
        md.register(envelope);
        envelope.dotNet=true;
        envelope.implicitTypes=true;
        envelope.encodingStyle=SoapSerializationEnvelope.XSD;
        respuesta_existencia.addProperty("codigotanque",codtanque);
        respuesta_existencia.addProperty("sucursal",codigo_sucursal);
        envelope.setOutputSoapObject(respuesta_existencia);
        HttpTransportSE transportSE = new HttpTransportSE(URL);

        try {
            transportSE.call(SOAP_ACTION,envelope);
            resultadaSoap=(SoapPrimitive) envelope.getResponse();
            existencia_tanque=Double.parseDouble(resultadaSoap.toString());
            existencia.setText("existencia: "+String.valueOf(existencia_tanque));

        }catch (IOException e){
            e.printStackTrace();
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }

    }

    public void consultar_precio_combustible(int producto,int codigo_matricula,int sucursal,int tanque,int tmov){
        SoapPrimitive resultadaSoap =null;
        double precio_galon;
        final String SOAP_ACTION="http://tempuri.org/precio_galon";
        final String METHOD_NAME="precio_galon";

        SoapObject respuesta = new SoapObject(NAMESPACES,METHOD_NAME);
        MarshalDouble md = new MarshalDouble();
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        md.register(envelope);

        envelope.dotNet=true;
        envelope.implicitTypes=true;
        envelope.encodingStyle=SoapSerializationEnvelope.XSD;

        respuesta.addProperty("cproducto",producto);
        respuesta.addProperty("matricula",codigo_matricula);
        respuesta.addProperty("codigosucursal",sucursal);
        respuesta.addProperty("codigotanque",tanque);
        respuesta.addProperty("tmovimiento",tmov);
        respuesta.addProperty("r","S");

        envelope.setOutputSoapObject(respuesta);
        HttpTransportSE transportSE = new HttpTransportSE(URL);
        try {
            transportSE.call(SOAP_ACTION,envelope);
            resultadaSoap=(SoapPrimitive) envelope.getResponse();

            precio_galon=Double.parseDouble(resultadaSoap.toString());

            preciogalon.setText(String.valueOf(precio_galon));
            if (precio_galon != 0){
                cantidad_venta.setEnabled(true);
            }
        }catch (IOException e){
            Toast.makeText(this,"error: " + e.getMessage().toString(),Toast.LENGTH_LONG).show();
        }catch (XmlPullParserException e){
            Toast.makeText(this,"error: " + e.getMessage().toString(),Toast.LENGTH_LONG).show();
        }
    }
    public void llenar_tanques(int codigosucursal ){
        final String SOAP_ACTION="http://tempuri.org/listado_tanque_sucursal";
        final String METHOD_NAME="listado_tanque_sucursal";

        SoapObject respueta = new SoapObject(NAMESPACES,METHOD_NAME);
        MarshalDouble md = new MarshalDouble();
        SoapSerializationEnvelope envelope =new SoapSerializationEnvelope(SoapEnvelope.VER11);
        md.register(envelope);
        envelope.dotNet=true;
        envelope.implicitTypes=true;
        envelope.encodingStyle=SoapSerializationEnvelope.XSD;

        respueta.addProperty("codigo_sucursal",String.valueOf(codigosucursal));
        envelope.setOutputSoapObject(respueta);
        HttpTransportSE transportSE = new HttpTransportSE(URL);
        try {
            transportSE.call(SOAP_ACTION,envelope);
            resultRequestSOAP=(SoapObject)  envelope.getResponse();
            final ArrayList<lista_tanque_sucursal>tanques=new ArrayList<lista_tanque_sucursal>();

            int lnotan = resultRequestSOAP.getPropertyCount();
            for (int st=0; st<lnotan; st++){
                SoapObject lst =(SoapObject) resultRequestSOAP.getProperty(st);
                tanques.add((new lista_tanque_sucursal(Integer.parseInt(lst.getProperty("codigo_tanque").toString()),lst.getProperty("descripcion_tanque").toString(),Float.parseFloat(lst.getProperty("capacidad").toString()))));
            }

            ArrayAdapter<lista_tanque_sucursal>ltanque=new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,tanques);
            ltanques.setAdapter(ltanque);
        }catch (IOException e){
            e.printStackTrace();
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }
    }
    public String convertir_imagen_byte(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
    public void generar_codigo_transaccion(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.getDefault());
        String fechaHora = simpleDateFormat.format(new Date());
        codigotransaccion="";
        codigotransaccion=uname + " - " + fechaHora;
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    public static Bitmap reducirBitmap(Context context, String uri, int maxancho, int maxalto) {
            try {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                BitmapFactory.decodeStream(context.getContentResolver().openInputStream(Uri.parse(uri)), null, options);
                options.inSampleSize = (int) Math.max(Math.ceil(options.outWidth / maxancho),
                        Math.ceil(options.outHeight / maxalto));
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeStream(context.getContentResolver()
                        .openInputStream(Uri.parse(uri)), null, options);
            } catch (FileNotFoundException e) {
                Toast.makeText(context, "Fichero/recurso no encontrado",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
                return null;
            }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACTION_CAMERA:
                if (resultCode == RESULT_OK) {
                    Bitmap bitmap;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI );
                        foto_tanque = Bitmap.createScaledBitmap(bitmap,780,780,false);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                break;

        }

    }

 }  