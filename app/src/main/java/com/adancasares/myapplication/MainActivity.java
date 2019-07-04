package com.adancasares.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.http.HttpResponseCache;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private final int REQUEST_ACCESS_FINE = 0;
    private int permissionCheck = 0;
    private final int vehiculo_bicicleta = 0;
    private final int vehiculo_coche = 1;

    public double Latitud;
    public double Longitud;

    public int vehiculo = 0; //si es 0 es una bicicleta y si es 1 es un coche


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //-----------------------------------------------------------------------------------------

        pedirPermisoUbicacion();

        try {
            obtenerUbicacion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }








        //-----------------------------------------------------------------------------------------


    }

    //---------------para navegar desde MainActivity hasta segundaPantalla-------------------------
    public void onClick(View view){
        switch (view.getId()){
            case R.id.bicicleta:
                vehiculo = vehiculo_bicicleta;
                break;
            case R.id.coche:
                vehiculo = vehiculo_coche;
                break;
        }
        Intent miIntent = new Intent(MainActivity.this,segundaPantalla.class);
        startActivity(miIntent);
    }

    //--------------------PEDIR PERMISOS PARA OBTENER LA UBICACION---------------------------------
    public void pedirPermisoUbicacion(){
        permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            System.exit(0);
        }
    }

    //-----------------------------OBTENER UBICACION ACTUAL----------------------------------------
    public void obtenerUbicacion() throws InterruptedException {

        //obtiene la referecia del sistema de localizacion
        LocationManager locationManager = (LocationManager)
                MainActivity.this.getSystemService(Context.LOCATION_SERVICE);

        //define las actualizaciones de localizacion
        LocationListener locationListener = new LocationListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onLocationChanged(Location location) {  //cuando cambia la localizacion
                Latitud = location.getLatitude();
                Longitud = location.getLongitude();
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
        permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
                0, locationListener);
    }














    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------







    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------





    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------




    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------




    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------



}
