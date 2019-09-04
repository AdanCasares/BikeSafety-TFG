package com.adancasares.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class NavegadorEmergencia extends Activity {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    public String uid;

    public int vehiculo = 0;
    public boolean alertaVozActiva = true;
    final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 0;

    public boolean gpsActivo = true;

    public double latitud = 0.00;
    public double longitud = 0.00;
    public int emergencia = 1; // si es 0 no hay emergencia y si es un 1 si hay una emergencia.

    MediaPlayer unoUnoDos = new MediaPlayer();

    Usuario usuario = new Usuario();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navegador_emergencia);

        unoUnoDos = MediaPlayer.create(this, R.raw.unounodos);

        usuario.setUid(UUID.randomUUID().toString());
        uid = usuario.getUid();

        try {
            obtenerUbicacion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (alertaVozActiva) {
            unoUnoDos.start();
            alertaVozActiva = false;
        }

        leerVehiculo();
        inicializarFirebase();

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                enviarFirebase();
            }
        };
        timer.schedule(task, 1000, 1000);
    }

    @Override
    protected void onDestroy() {
        databaseReference.child("Usuario").child(uid).removeValue();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        super.onDestroy();
    }

    @Override
    public void onBackPressed(){

    }

    //------------------INICIALIZA LA CONEXION CON LA BASE DE DATOS FIREBASE-----------------------
    private void inicializarFirebase(){
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    //------LEE LA INFORMACION DEL MAINACTIVITY Y NOS INDICA EL TIPO DE VEHICULO SELECIONADO-------
    @SuppressLint("SetTextI18n")
    public void leerVehiculo(){
        Bundle bundle = this.getIntent().getExtras();
        assert bundle != null;
        vehiculo = bundle.getInt("vehiculo");
        longitud = bundle.getDouble("longitud");
        latitud = bundle.getDouble("latitud");
    }

    public void enviarFirebase(){
        usuario.setLatitud(latitud);
        usuario.setLongitud(longitud);
        usuario.setVehiculo(vehiculo);
        usuario.setEmergencia(emergencia);
        databaseReference.child("Usuario").child(usuario.getUid()).setValue(usuario);
    }

    //-----------------------------OBTENER UBICACION ACTUAL----------------------------------------
    public void obtenerUbicacion() throws InterruptedException {

        //obtiene la referecia del sistema de localizacion
        LocationManager locationManager = (LocationManager)
                NavegadorEmergencia.this.getSystemService(Context.LOCATION_SERVICE);

        if (gpsActivo){
            //define las actualizaciones de localizacion
            LocationListener locationListener = new LocationListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onLocationChanged(Location location) {  //cuando cambia la localizacion
                    latitud = location.getLatitude();
                    longitud = location.getLongitude();

                    //AQUI HAY QUE ENVIAR LA INFORMACION A FIREBASE
                    enviarFirebase();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) { //cuando cambia el estatus
                }

                @Override
                public void onProviderEnabled(String provider) { //cuando el porveedor esta habilitado
                }

                @Override
                public void onProviderDisabled(String provider) { // cuando el proveedor esta deshabilitado
                }
            };

            //registra las actualizaciones de localizacion recividas
            int permissionCheck = ContextCompat.checkSelfPermission(NavegadorEmergencia.this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
                    0, locationListener);
        }

    }

    public void pararEmergencia(View view) {

        Intent miIntent = new Intent(NavegadorEmergencia.this,MainActivity.class);
        gpsActivo = false;
        databaseReference.child("Usuario").child(uid).removeValue();
        startActivity(miIntent);
        System.exit(0);
    }

    public void onClickLlamar(View view){

        if(ActivityCompat.checkSelfPermission(NavegadorEmergencia.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
            // Aquí ya está concedido, procede a realizar lo que tienes que hacer

            Intent miIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:112"));
            startActivity(miIntent);
        }else{
            // Aquí lanzamos un dialog para que el usuario confirme si permite o no el realizar llamadas
            ActivityCompat.requestPermissions(NavegadorEmergencia.this, new String[]{ Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
        }


        /*Intent miIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:112"));
        if (ActivityCompat.checkSelfPermission(NavegadorEmergencia.this,
                Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
            return;
        }
        startActivity(miIntent);*/
    }
}
