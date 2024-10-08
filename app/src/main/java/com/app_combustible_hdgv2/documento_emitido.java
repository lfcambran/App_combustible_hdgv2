package com.app_combustible_hdgv2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app_combustible_hdgv2.utilidades.MarshalDouble;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class documento_emitido extends AppCompatActivity  {
    int correlativodoc;
    TextView correlativo,matricula,sucursal,movimiento,novale,
    fechavale,creado,piloto,cantidad,valor,total,facturado,estado;

    Button cerrar,reenviar,imprimir;
    final String URL = "http://200.30.144.133:3427/wsite_c/wsb_combustible_hdg/ws_datos_combustible.asmx";
    final String NAMESPACES = "http://tempuri.org/";
    ProgressDialog progressDoalog;
    private SoapObject resultRequestSOAP = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Documento Emitido");
        setContentView(R.layout.activity_documento_emitido);
        
        correlativo=(TextView) findViewById(R.id.txtcorrelativo_doc_emitido);
        matricula=(TextView) findViewById(R.id.txtmatricula);
        sucursal=(TextView) findViewById(R.id.txtsucursal);
        movimiento=(TextView) findViewById(R.id.txttmovimiento);
        novale=(TextView) findViewById(R.id.txtnovale);
        fechavale=(TextView) findViewById(R.id.txtfecha);
        creado=(TextView) findViewById(R.id.txtcreado);
        piloto=(TextView) findViewById(R.id.txtpiloto);
        cantidad=(TextView) findViewById(R.id.txtcantida);
        valor=(TextView) findViewById(R.id.txtvalor);
        total=(TextView) findViewById(R.id.txttotal);
        facturado=(TextView) findViewById(R.id.txtfacturado);
        estado=(TextView) findViewById(R.id.txtestado);
        cerrar=(Button) findViewById(R.id.cerrar);
        reenviar=(Button) findViewById(R.id.reenviar);
        imprimir=(Button) findViewById(R.id.btnimprimir);
        correlativodoc= getIntent().getIntExtra("correlativo_doc",0);
        correlativo.setText(String.valueOf(correlativodoc));
        consultar_doc_emitido(correlativodoc);

        cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        reenviar.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                reenvio_documento(correlativodoc);
            }
        });
        imprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url;
                String rconsulta=verificacion_documento(correlativodoc);
                if (rconsulta.equals("OK")){
                url= "http://200.30.144.133:3427/reportes_app/documento_no_" + correlativodoc + ".pdf"  ;
                Toast.makeText(getApplicationContext(),"Visualizando Reportes",Toast.LENGTH_LONG).show();

               /* Intent vistapdf = new Intent( documento_emitido.this, visor_pdfview.class);
                vistapdf.putExtra("ruta_pdf", url);
                startActivity(vistapdf);*/
                Intent intent = new Intent();
                intent.setDataAndType(Uri.parse(url), "application/pdf");
                startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"Error: " + rconsulta ,Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public String verificacion_documento(int cdoc){
        SoapPrimitive resultRequestSOAP = null;
        final String SOAP_ACTION="http://tempuri.org/consultar_documento_emitido";
        final String METHOD_NAME="consultar_documento_emitido";
        String resultado="";
        SoapObject resquest = new SoapObject(NAMESPACES,METHOD_NAME);

        MarshalDouble md = new MarshalDouble();
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        md.register(envelope);
        envelope.dotNet=true;
        envelope.implicitTypes=true;
        envelope.encodingStyle=SoapSerializationEnvelope.XSD;

        resquest.addProperty("nombrearchivo", "documento_no_" + String.valueOf(cdoc) );
        resquest.addProperty("norepeorte",cdoc);
        resquest.addProperty("tipo", "Combustible");
        envelope.setOutputSoapObject(resquest);
        HttpTransportSE transportSE = new HttpTransportSE(URL);

        try {
            transportSE.call(SOAP_ACTION,envelope);
            resultRequestSOAP = (SoapPrimitive) envelope.getResponse();
            String res_envio = resultRequestSOAP.toString();
            String respuet = res_envio.toString();

            if (respuet.equals("OK")){
                resultado=respuet.toString();
            }else {
               resultado= respuet.toString();
            }
        } catch (IOException e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }catch (XmlPullParserException d){
            Toast.makeText(getApplicationContext(),d.getMessage(),Toast.LENGTH_LONG).show();
        }
        return resultado;
    }
    public void consultar_doc_emitido(int c){
        final String SOAP_ACTION ="http://tempuri.org/vale_emitido";
        final String METHOD_NAME = "vale_emitido";
        String estado_doc,estado_facturado;
        SoapObject resquest = new SoapObject(NAMESPACES,METHOD_NAME);
        MarshalDouble md = new MarshalDouble();
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        md.register(envelope);
        envelope.dotNet=true;
        envelope.implicitTypes=true;
        envelope.encodingStyle=SoapSerializationEnvelope.XSD;

        resquest.addProperty("codigo",c);
        envelope.setOutputSoapObject(resquest);
        HttpTransportSE transportSE =new HttpTransportSE(URL);

        try {
            transportSE.call(SOAP_ACTION,envelope);
            resultRequestSOAP=(SoapObject) envelope.getResponse();

            int n = resultRequestSOAP.getPropertyCount();
            for (int i=0; i<n; i++)
            {
                SoapObject doc=(SoapObject) resultRequestSOAP.getProperty(i);
                matricula.setText("Matricula: " + doc.getProperty("matricula").toString());
                sucursal.setText("Base: " + doc.getProperty("sucursal").toString());
                movimiento.setText("Movimiento: " + doc.getProperty("tipomovimiento").toString());
                novale.setText("Vale: " + doc.getProperty("novale"));
                fechavale.setText("Fecha: " + doc.getProperty("fecha").toString());
                creado.setText("Creado por: " + doc.getProperty("creadopor").toString());
                piloto.setText("Piloto: " + doc.getProperty("piloto").toString());
                cantidad.setText("Cantidad: " + doc.getProperty("cantidad"));
                valor.setText("Valor: " + doc.getProperty("valor"));
                total.setText("Total Doc: Q. " + doc.getProperty("total"));
                estado_doc=doc.getProperty("estado").toString();
                facturado.setText(doc.getProperty("facturado").toString());

                if (estado_doc.equals("Anulado")){
                    estado.setTextColor(Color.RED);
                    estado.setText(estado_doc);
                }else {
                    estado.setTextColor(Color.WHITE);
                    estado.setText(estado_doc);
                }

            }

        }catch (IOException e){
            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }catch (XmlPullParserException d){
            Toast.makeText(getApplicationContext(),"error:" + d.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
    private void reenvio_documento(int doc){

        barra_progreso();
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SoapPrimitive resultRequestSOAP = null;
                            final String SOAP_ACTION ="http://tempuri.org/reenvio_vale";
                            final String METHOD_NAME = "reenvio_vale";
                            SoapObject resquest = new SoapObject(NAMESPACES,METHOD_NAME);
                            MarshalDouble md = new MarshalDouble();
                            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                            md.register(envelope);
                            envelope.dotNet=true;
                            envelope.implicitTypes=true;
                            envelope.encodingStyle=SoapSerializationEnvelope.XSD;

                            resquest.addProperty("codigo",doc);
                            envelope.setOutputSoapObject(resquest);
                            HttpTransportSE transportSE = new HttpTransportSE(URL);

                                transportSE.call(SOAP_ACTION,envelope);
                                resultRequestSOAP = (SoapPrimitive) envelope.getResponse();
                                String res_envio = resultRequestSOAP.toString();
                                String respuet = res_envio.toString();
                                AlertDialog.Builder builder = new AlertDialog.Builder(documento_emitido.this);
                                builder.setCancelable(false);
                                builder.setIcon(R.drawable.iconapp);

                                if (respuet.equals("ok")){
                                    progressDoalog.dismiss();
                                    runOnUiThread(new Thread(){
                                        public void run(){
                                            builder.setTitle("Reenvio de Vale Emitido");
                                            builder.setMessage("Se ha Reenviado Correctamente El Vale");
                                            builder.setPositiveButton("ok", new DialogInterface.OnClickListener(){

                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Toast.makeText(getApplicationContext(),"Envio de correo satisfactorio", Toast.LENGTH_LONG ).show();
                                                    finish();
                                                }
                                            });
                                            AlertDialog alertDialog =builder.create();
                                            alertDialog.show();
                                            alertDialog.getWindow().setGravity(Gravity.CENTER);
                                        }
                                    });

                                }else {
                                    progressDoalog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Error: " + respuet.toString(), Toast.LENGTH_LONG).show();
                                }

                        }catch (IOException e){
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                        }catch (XmlPullParserException d){
                            Toast.makeText(getApplicationContext(),d.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                }
        ).start();


    }
    Handler handler=new Handler(){
      public void handleMessage(Message msg){
          super.handleMessage(msg);
          progressDoalog.incrementProgressBy(1);
      }
    };

    private void barra_progreso(){
        progressDoalog= new ProgressDialog(documento_emitido.this);
        progressDoalog.setMax(100);
        progressDoalog.setIcon(R.drawable.iconapp);
        progressDoalog.setIndeterminate(true);
        progressDoalog.setMessage("Enviando Documento.. por favor espere");
        progressDoalog.setTitle("Reenvio de Documento");
        progressDoalog.setCancelable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (progressDoalog.getProgress()<= progressDoalog.getMax()){
                        Thread.sleep(200);
                        handler.handleMessage(handler.obtainMessage());
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
}