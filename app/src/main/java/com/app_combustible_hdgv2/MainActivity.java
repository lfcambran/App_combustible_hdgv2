package com.app_combustible_hdgv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.ui.AppBarConfiguration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app_combustible_hdgv2.databinding.ActivityMainBinding;
import com.app_combustible_hdgv2.ui.login.LoginActivity;
import com.app_combustible_hdgv2.utilidades.MarshalDouble;
import com.app_combustible_hdgv2.utilidades.varibles_globales;
import com.google.android.material.snackbar.Snackbar;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    TextView tusuario;
    String uname,nombrecompleto;
    private SoapObject resultRequestSOAP = null;
    private final static String CHANNEL_ID = "NOTIFICACION";
    private final static int NOTIFICACION_ID = 0;
    private CardView nuevo_vale, lista_vales,nuevo_aterrizaje;
    private LinearLayout opciones;
    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private static final int REQUEST_LOCATION = 1;
    final String URL = "http://200.30.144.133:3427/wsite_c/wsb_combustible_hdg/ws_datos_combustible.asmx";
    final String NAMESPACES = "http://tempuri.org/";
    private varibles_globales ug;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        uname = getIntent().getStringExtra("nombre_usuario");
        tusuario = findViewById(R.id.tnousuario);
        tusuario.setText("Usuario: " + uname);

        opciones=findViewById(R.id.nueva_opcion);
        ug = new varibles_globales();
            createNotificationChannel();
            createNotification();
            consultar_datos(uname);
            if (ug.getAutorizado_aterrizaje().equals("true")){
                opciones.setVisibility(View.VISIBLE);
            }else{
                opciones.setVisibility(View.GONE);
            };
        setTitle("Principal-" + nombrecompleto);
    }

    public void cerrar_session(View view) {
        Button boton = (Button) view;
        NotificationManager notificationManager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
        notificationManager.cancelAll();
        Snackbar.make(view, "Cerrando Sesion", Snackbar.LENGTH_LONG).setAnchorView(R.id.cerrarsesion).setAction("Action", null).show();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addCategory(intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        System.exit(0);
        finish();
    }

    public void nuevo_vale(View v) {
        CardView boton = (CardView) v;
        Snackbar.make(v, "Creacion de Vale", Snackbar.LENGTH_LONG).setAnchorView(R.id.cerrarsesion).setAction("Action", null).show();
        Intent accountsIntent = new Intent(this, activity_emitir_vale.class);
        accountsIntent.putExtra("nombre_usuario", uname);
        startActivity(accountsIntent);
        setResult(Activity.RESULT_OK);

    }

    public void listado_vales(View v) {
        CardView c = (CardView) v;
        Intent listavales = new Intent(MainActivity.this, activity_lista_vales.class);
        listavales.putExtra("nombre_usuario", uname);
        startActivity(listavales);

    }

    public void nuevo_aterrizaje(View V){

        Intent aterrizaje = new Intent(MainActivity.this, activity_aterrizaje.class);
        aterrizaje.putExtra("nombre_usuario",uname);
        startActivity(aterrizaje);
        setResult(Activity.RESULT_OK);
    }

    public void acercade(View v) {
        showAboutDialog();
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Acerca de la APP");
        builder.setView(R.layout.aboutdialog);
        builder.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificacion";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Sistema de Venta y Consumo de Combustibles");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        builder.setSmallIcon(R.drawable.iconapp);
        builder.setContentTitle("Aplicacion Combustible");
        builder.setContentText("Venta y Consumo de Combustible HDG");
        builder.setColor(Color.BLUE);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setLights(Color.MAGENTA, 1000, 1000);
        builder.setVibrate(new long[]{1000, 1000, 1000, 100, 1000});
        builder.setDefaults(Notification.DEFAULT_SOUND);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManagerCompat.notify(NOTIFICACION_ID, builder.build());
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
                ug.setAutorizado_aterrizaje(ic.getProperty("opcionadmin").toString());
                nombrecompleto=ic.getProperty("nombreCompleto_usuario").toString();
            }


        }catch (IOException e){
            e.printStackTrace();
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }
    }

}