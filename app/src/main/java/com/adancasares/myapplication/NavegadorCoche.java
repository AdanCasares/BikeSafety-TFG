//Esta segunda pantalla es solo para los usuarios que esta utilizando un coche.
package com.adancasares.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class NavegadorCoche extends Activity {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    public String uid;

    public List<Usuario> listUsuarios = new ArrayList<Usuario>();
    public List<Usuario> listUsuariosEmergencia = new ArrayList<Usuario>();

    ImageView semaforo;
    ImageView trianguloEmergencia;
    public int vehiculo = 0;

    public boolean gpsActivo = true;
    public boolean alertaVozActiva = true;
    public boolean alertaVozEmergenciaActiva = true;
    public double latitud = 0.00;
    public double longitud = 0.00;
    public int emergencia = 0; // si es 0 no hay emergencia y si es un 1 si hay una emergencia.

    MediaPlayer mpEmergencia = new MediaPlayer();
    MediaPlayer mpPeligro200m = new MediaPlayer();

    Usuario usuario = new Usuario();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navegador_coche);

        mpPeligro200m = MediaPlayer.create(this, R.raw.peligro200m);
        mpEmergencia = MediaPlayer.create(this, R.raw.emergencia);
        semaforo = findViewById(R.id.imsemaforo);
        trianguloEmergencia = findViewById(R.id.imemergencia);


        usuario.setUid(UUID.randomUUID().toString());
        uid = usuario.getUid();

        try {
            obtenerUbicacion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        leerVehiculo();
        inicializarFirebase();

        obtenerListaUsuarios();
        comprobarPeligro();


        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                enviarFirebase();
                obtenerListaUsuarios();
                comprobarPeligro();
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

    //-------------OBTIENE LA LISTA DE USUARIOS QUE SE ENCUENTRAN ACTIVOS EN FIREBASE--------------
    private void obtenerListaUsuarios() {
        databaseReference.child("Usuario").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listUsuarios.clear();
                for (DataSnapshot objSnapshot: dataSnapshot.getChildren()){
                    Usuario usuario = objSnapshot.getValue(Usuario.class);
                    //obtenemos la lista de ciclistas y corredores que se encuentran alrededor nuestra
                    if (!usuario.getUid().equals(uid) && (usuario.getVehiculo()==0 || usuario.getVehiculo()==2)) {
                        listUsuarios.add(usuario);
                    }
                }
                listUsuariosEmergencia.clear();
                for (DataSnapshot objSnapshot: dataSnapshot.getChildren()){
                    Usuario usuario = objSnapshot.getValue(Usuario.class);
                    //obtenemos la lista de usuarios en emergencia
                    if (!usuario.getUid().equals(uid) && usuario.getEmergencia()==1) {
                        listUsuariosEmergencia.add(usuario);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //---------------FORMULA DEL HAVERSINE: CALCULA LA DISTANCIA ENTRE DOS USUARIOS----------------
    public double calcularDistacia(double lat1, double lon1, double lat2, double lon2){
        /*diferenciaLatitud = suLatitud - miLatitud;
        diferenciaLongitud = suLongitud - miLongitud;
        a = Math.pow(Math.sin(diferenciaLatitud/2),2) +
                Math.cos(miLatitud)*Math.cos(suLatitud)*Math.pow(Math.sin(diferenciaLongitud/2),2);
        c = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        distancia = R_Tierra*c;
        return distancia;  //devuelta en metros.*/
        double a = 6378137, b = 6356752.314245, f = 1 / 298.257223563;
        double L = Math.toRadians(lon2 - lon1);
        double U1 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat1)));
        double U2 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat2)));
        double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);
        double cosSqAlpha;
        double sinSigma;
        double cos2SigmaM;
        double cosSigma;
        double sigma;

        double lambda = L, lambdaP, iterLimit = 100;
        do
        {
            double sinLambda = Math.sin(lambda), cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt(	(cosU2 * sinLambda)
                    * (cosU2 * sinLambda)
                    + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda)
                    * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda)
            );
            if (sinSigma == 0)
            {
                return 0;
            }

            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1 - sinAlpha * sinAlpha;
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;

            double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
            lambdaP = lambda;
            lambda = 	L + (1 - C) * f * sinAlpha
                    * 	(sigma + C * sinSigma
                    * 	(cos2SigmaM + C * cosSigma
                    * 	(-1 + 2 * cos2SigmaM * cos2SigmaM)
            )
            );

        } while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);

        if (iterLimit == 0)
        {
            return 0;
        }

        double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
        double A = 1 + uSq / 16384
                * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double deltaSigma =
                B * sinSigma
                        * (cos2SigmaM + B / 4
                        * (cosSigma
                        * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM
                        * (-3 + 4 * sinSigma * sinSigma)
                        * (-3 + 4 * cos2SigmaM * cos2SigmaM)));

        return (b * A * (sigma - deltaSigma));
    }

    public double obtenerMenorDistancia(List distancias){
        double maximo = 0.00;
        for (int i = 0; i < distancias.size(); i++) {
            if ((double)distancias.get(i) > maximo) {
                maximo = (double) distancias.get(i);
            }
        }
        double minimo = maximo;
        for (int i = 0; i < distancias.size(); i++) {
            if ((double)distancias.get(i) < minimo) {
                minimo = (double) distancias.get(i);
            }
        }
        return minimo;
    }

    //----------------COMPRUEBA QUE LA DISTANCIA AL USUARIO MAS CERCANO SEA SEGURA-----------------
    @SuppressLint("SetTextI18n")
    private void comprobarPeligro(){
        //tvEstado = findViewById(R.id.tvEstado);

        if (listUsuarios.isEmpty() && listUsuariosEmergencia.isEmpty()){
            //tvEstado.setText("SEGURO");
            alertaVozActiva = true;
            alertaVozEmergenciaActiva = true;
            semaforo.setImageResource(R.drawable.semaforoverde);
            trianguloEmergencia.setImageResource(R.drawable.triangulotranquilo);
        }
        else if (!listUsuariosEmergencia.isEmpty()){ //hay alguien en emergencia
            double suLatitud = 0.00;
            double suLongitud = 0.00;
            //tvEstado = findViewById(R.id.tvEstado);
            List<Double> distancias = new ArrayList<Double>();

            for (int i = 0; i < listUsuariosEmergencia.size(); i++) {
                suLatitud = listUsuariosEmergencia.get(i).getLatitud();
                suLongitud = listUsuariosEmergencia.get(i).getLongitud();
                double d = calcularDistacia(latitud, longitud, suLatitud, suLongitud);
                distancias.add(d);
            }
            double distanciaMinima = obtenerMenorDistancia(distancias);
            if (distanciaMinima <= 400) {
                if (alertaVozEmergenciaActiva) {
                    semaforo.setImageResource(R.drawable.semafororojo);
                    trianguloEmergencia.setImageResource(R.drawable.triangulopeligro);
                    mpEmergencia.start();
                    alertaVozEmergenciaActiva = false;
                }
            }
            else{
                alertaVozEmergenciaActiva = true;
                semaforo.setImageResource(R.drawable.semaforoverde);
                trianguloEmergencia.setImageResource(R.drawable.triangulotranquilo);
            }
        }
        else {
            trianguloEmergencia.setImageResource(R.drawable.triangulotranquilo);
            alertaVozEmergenciaActiva = true;
            double suLatitud = 0.00;
            double suLongitud = 0.00;
            //tvEstado = findViewById(R.id.tvEstado);
            List<Double> distancias = new ArrayList<Double>();

            for (int i = 0; i < listUsuarios.size(); i++) {
                suLatitud = listUsuarios.get(i).getLatitud();
                suLongitud = listUsuarios.get(i).getLongitud();
                double d = calcularDistacia(latitud, longitud, suLatitud, suLongitud);
                distancias.add(d);
            }
            double distanciaMinima = obtenerMenorDistancia(distancias);
            if (distanciaMinima <= 200) {
                //tvEstado.setText("PELIGRO");
                if (alertaVozActiva) {
                    semaforo.setImageResource(R.drawable.semafororojo);
                    mpPeligro200m.start();
                    alertaVozActiva = false;
                }
            }
            else if (distanciaMinima <= 400){
                //if (alertaVozActiva) {
                    semaforo.setImageResource(R.drawable.semaforoambar);
                    //mpPeligro200m.start();
                    alertaVozActiva = true;
                //}
            } else {
                //tvEstado.setText("SEGURO");
                alertaVozActiva = true;
                semaforo.setImageResource(R.drawable.semaforoverde);
            }
        }
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
                NavegadorCoche.this.getSystemService(Context.LOCATION_SERVICE);

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
                    obtenerListaUsuarios();
                    comprobarPeligro();
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
            int permissionCheck = ContextCompat.checkSelfPermission(NavegadorCoche.this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
                    0, locationListener);
        }

    }

    public void onClickEmergencia(View view){
        Intent miIntent = new Intent(NavegadorCoche.this,NavegadorEmergencia.class);
        miIntent.putExtra("vehiculo", vehiculo);
        miIntent.putExtra("longitud", longitud);
        miIntent.putExtra("latitud", latitud);
        gpsActivo = false;
        databaseReference.child("Usuario").child(uid).removeValue();
        startActivity(miIntent);
        System.exit(0);
    }

    public void cerrarAPP(View view) {

        Intent miIntent = new Intent(NavegadorCoche.this,MainActivity.class);
        gpsActivo = false;
        databaseReference.child("Usuario").child(uid).removeValue();
        startActivity(miIntent);
        System.exit(0);
    }
}
