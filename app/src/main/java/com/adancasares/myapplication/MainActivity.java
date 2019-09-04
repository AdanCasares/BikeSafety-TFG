package com.adancasares.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    public int vehiculo = 0; //si es 0 es una bicicleta, si es 1 es un coche y si es 2 es correr.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pedirPermisoUbicacion();
    }

    @Override
    public void onBackPressed(){

    }

    //---------------TRAS SELECIONAR EL VEHICULO AVANZAMOS A LA SEGUNDA PANTALLA-------------------
    public void onClick(View view){

        int vehiculo_bicicleta = 0;
        int vehiculo_coche = 1;
        int vehiculo_correr = 2;
        switch (view.getId()){
            case R.id.imBicicleta:
                vehiculo = vehiculo_bicicleta;
                Intent miIntentBicicleta = new Intent(MainActivity.this,NavegadorBicicleta.class);
                miIntentBicicleta.putExtra("vehiculo", vehiculo);
                startActivity(miIntentBicicleta);
                break;
            case R.id.imCoche:
                vehiculo = vehiculo_coche;
                Intent miIntentCoche = new Intent(MainActivity.this,NavegadorCoche.class);
                miIntentCoche.putExtra("vehiculo", vehiculo);
                startActivity(miIntentCoche);
                break;
            case R.id.imCorrer:
                vehiculo = vehiculo_correr;
                Intent miIntentCorrer = new Intent(MainActivity.this,NavegadorCorrer.class);
                miIntentCorrer.putExtra("vehiculo", vehiculo);
                startActivity(miIntentCorrer);
                break;
        }
    }

    //--------------------PEDIR PERMISOS PARA OBTENER LA UBICACION---------------------------------
    public void pedirPermisoUbicacion(){
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            System.exit(0);
        }
    }
}
