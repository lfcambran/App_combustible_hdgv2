package com.app_combustible_hdgv2.ui.login;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.app.Activity;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.net.Network;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.os.StrictMode;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.transport.HttpsTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.app_combustible_hdgv2.R;

import com.app_combustible_hdgv2.databinding.ActivityLoginBinding;
import com.app_combustible_hdgv2.MainActivity;
import com.app_combustible_hdgv2.utilidades.MarshalDouble;
import com.app_combustible_hdgv2.utilidades.varibles_globales;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    private final AppCompatActivity activity = this;
    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;
    LocationManager locationManager;
    private varibles_globales ug;
    final String NAMESPACES = "http://tempuri.org/";
    final String URL = "http://200.30.144.133:3427/wsite_c/wsb_combustible_hdg/ws_datos_combustible.asmx";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ug = new varibles_globales();
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);
        setTitle("Inicio Sesion");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.iconapp);

        localizacion();

        final EditText usernameEditText = binding.usuario;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;
        final TextView localizacionText= binding.localizacion;

        binding.localizacion.setText("Latitud:" + ug.getlatitud() + " Longitud: " + ug.getlongitu() );
        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (Build.VERSION.SDK_INT > 0) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });


        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoapPrimitive resultRequestSOAP = null;
                final String SOAP_ACTION = "http://tempuri.org/Usuario";
                final String METHOD_NAME = "Usuario";

                SoapObject respuesta = new SoapObject(NAMESPACES, METHOD_NAME);
                MarshalDouble md = new MarshalDouble();
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                md.register(envelope);
                envelope.dotNet = true;
                envelope.implicitTypes = true;
                envelope.encodingStyle = SoapSerializationEnvelope.XSD;
                respuesta.addProperty("Codigo", usernameEditText.getText().toString());
                respuesta.addProperty("Contra", passwordEditText.getText().toString());
                envelope.setOutputSoapObject(respuesta);
                HttpTransportSE transportSE = new HttpTransportSE(URL);

                try {
                    @Nullable LoginResult loginResult = null;
                    transportSE.call(SOAP_ACTION, envelope);
                    resultRequestSOAP = (SoapPrimitive) envelope.getResponse();
                    String resultado = resultRequestSOAP.toString();
                    String r = resultado.toString();
                    if (r.equals("User")) {

                        Toast.makeText(getApplicationContext(), "Inicio de Sesion Correcto", Toast.LENGTH_LONG).show();
                        Intent accountsIntent = new Intent(activity, MainActivity.class);
                        accountsIntent.putExtra("nombre_usuario", usernameEditText.getText().toString().trim());
                        startActivity(accountsIntent);
                        setResult(Activity.RESULT_OK);

                        finish();
                    } else {
                        builder.setTitle("Inicio de Sesion");
                        builder.setMessage("Contraseña Ingresada Incorrecta");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getApplicationContext(), "Contraseña Erronea", Toast.LENGTH_LONG).show();
                            }
                        });
                        AlertDialog alertDialog =builder.create();
                        alertDialog.show();
                        alertDialog.getWindow().setGravity(Gravity.CENTER);


                    }
                } catch (IOException e) {
                    builder.setTitle("Error de Conexion");
                    builder.setMessage("Error al intentar Conectarse al servicio: " + e.getMessage());
                    builder.setNegativeButton("Cerrar",null);
                    AlertDialog alertDialog =builder.create();
                    alertDialog.show();
                    alertDialog.getWindow().setGravity(Gravity.CENTER);
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    builder.setTitle("Error de Conexion");
                    builder.setMessage("Error al intentar Conectarse al servicio: " + e.getMessage());
                    builder.setNegativeButton("Cerrar",null);
                    AlertDialog alertDialog =builder.create();
                    alertDialog.show();
                    alertDialog.getWindow().setGravity(Gravity.CENTER);
                    e.printStackTrace();
                }

            }
        });

        usernameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (!hasFocus && usernameEditText.getText().toString() != null) {
                    SoapPrimitive resultRequestSOAP = null;
                    final String SOAP_ACTION = "http://tempuri.org/consulta_usuario";
                    final String METHOD_NAME = "consulta_usuario";

                    SoapObject respuesta = new SoapObject(NAMESPACES, METHOD_NAME);
                    MarshalDouble md = new MarshalDouble();
                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    md.register(envelope);
                    envelope.dotNet = true;
                    envelope.implicitTypes = true;
                    envelope.encodingStyle = SoapSerializationEnvelope.XSD;

                    respuesta.addProperty("codigo_user", usernameEditText.getText().toString());
                    envelope.setOutputSoapObject(respuesta);
                    HttpTransportSE transportSE = new HttpTransportSE(URL);

                    try {

                        transportSE.call(SOAP_ACTION, envelope);
                        resultRequestSOAP = (SoapPrimitive) envelope.getResponse();

                        String resultado = resultRequestSOAP.toString();

                        if (resultado.contains("True")) {
                            Toast.makeText(getApplicationContext(), "Bienvenido: " + usernameEditText.getText().toString(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Usuario No identificado", Toast.LENGTH_LONG).show();
                        }


                    } catch (IOException e) {
                        builder.setTitle("Error de Conexion");
                        builder.setMessage("Error al intentar Conectarse al servicio: " + e.getMessage());
                        builder.setNegativeButton("Cerrar",null);
                        AlertDialog alertDialog =builder.create();
                        alertDialog.show();
                        alertDialog.getWindow().setGravity(Gravity.CENTER);
                        e.printStackTrace();
                       // Toast.makeText(getApplicationContext(), "Error: " + e.getMessage().toString(), Toast.LENGTH_LONG).show();
                    } catch (XmlPullParserException e) {
                        builder.setTitle("Error de Conexion");
                        builder.setMessage("Error al intentar Conectarse al servicio: " + e.getMessage());
                        builder.setNegativeButton("Cerrar",null);
                        AlertDialog alertDialog =builder.create();
                        alertDialog.show();
                        alertDialog.getWindow().setGravity(Gravity.CENTER);
                        e.printStackTrace();
                        //Toast.makeText(getApplicationContext(), "Error: " + e.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }


                }
            }
        });
    }
    protected void AlertMensageNoGPS(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS se encuentra Inactivo por favor de activarlo!")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void localizacion(){
    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
        AlertMensageNoGPS();
        }else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            getLocalizacion();
         }
    }

    private void getLocalizacion(){
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION},1);
            getLocalizacion();
        } else {
            Location lnp = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location lgps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location lpp = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (lnp!=null){
                double lat = lnp.getLatitude();
                double longi = lnp.getLongitude();
                ug.setlatitud(lat);
                ug.setlongitu(longi);
            } else if (lgps!=null){
                double lat = lgps.getLatitude();
                double longi = lgps.getLongitude();
                ug.setlatitud(lat);
                ug.setlongitu(longi);
            } else if (lpp!=null){
                double lat = lpp.getLatitude();
                double longi = lpp.getLongitude();
                ug.setlatitud(lat);
                ug.setlongitu(longi);
            }else{
                Toast.makeText(this,"No se ha Encontrado su Ubicacion",Toast.LENGTH_LONG).show();
            }

        }
    }

}