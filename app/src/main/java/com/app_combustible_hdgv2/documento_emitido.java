package com.app_combustible_hdgv2;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

    Button cerrar,reenviar;
    final String URL = "http://200.30.144.133:3427/wsite_c/wsb_combustible_hdg/ws_datos_combustible.asmx";
    final String NAMESPACES = "http://tempuri.org/";

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


        try {
            transportSE.call(SOAP_ACTION,envelope);
            resultRequestSOAP = (SoapPrimitive) envelope.getResponse();
            String res_envio = resultRequestSOAP.toString();
            String respuet = res_envio.toString();
            if (respuet.equals("ok")){
                Toast.makeText(this,"Envio de correo satisfactorio", Toast.LENGTH_LONG ).show();
                finish();
            }else {
                Toast.makeText(getApplicationContext(), "Error: " + respuet.toString(), Toast.LENGTH_LONG).show();
            }
        } catch (IOException e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }catch (XmlPullParserException d){
            Toast.makeText(getApplicationContext(),d.getMessage(),Toast.LENGTH_LONG).show();
        }

    }
}