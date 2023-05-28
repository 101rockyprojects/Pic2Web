package com.example.pic2web;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class home extends AppCompatActivity {

    private LocationManager ubicacion;
    TextView tdireccion;
    Button bguardar;
   // DataBase miBD;

    Button btnCamara;
    ImageView imgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        btnCamara = findViewById(R.id.btnCamara);
        imgView = findViewById(R.id.imageView);

        btnCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirCamara();
            }
        });
        registrarLocalizacion();
        tdireccion = (TextView) findViewById(R.id.txtDireccion);
        // miBD = new DataBase(this);

        localizacion();
        estadoGPS();
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 1);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imgBitmap = (Bitmap) extras.get("data");
            imgView.setImageBitmap(imgBitmap);
        }
    }


       /* bguardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String datosDireccion = tdireccion.getText().toString();
                if (tdireccion.length()!=0){
                    agregar(datosDireccion);
                }else {
                    Toast.makeText(MainActivity.this, "Ingresa algo", Toast.LENGTH_LONG).show();
                }
            }
        });*/

   /* public void agregar(String nuevaEntrada){
        boolean insertarData = miBD.addData(nuevaEntrada);
        if(insertarData==true){
            Toast.makeText(this, "Datos insertados correctmente", Toast.LENGTH_LONG).show();
        } else{
            Toast.makeText(this, "No se  insertaron los datos", Toast.LENGTH_LONG).show();
        }
    }*/

    private void localizacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1000);
        }


        ubicacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location loc = ubicacion.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (ubicacion != null) {
            Log.d("Latitud", String.valueOf(loc.getLatitude()));
            Log.d("Longitud", String.valueOf(loc.getLongitude()));
            //longitud.setText("Longitud: " + String.valueOf(loc.getLongitude()));
            //  latitud.setText("Latitud: " + String.valueOf(loc.getLatitude()));

        }
    }


    private boolean estadoGPS() {
        ubicacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!ubicacion.isProviderEnabled(LocationManager.GPS_PROVIDER))
            Log.d("GPS", "NO ACTIVADO");
        else {
            Log.d("GPS", "ACTIVADO");
        }
        return true;
    }

    private void registrarLocalizacion() {
        ubicacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ubicacion.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, new milocalizacionListener());
    }

    private class milocalizacionListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> direccion1 = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                //System.out.println(direccion.get(0).getAddressLine(0));
                // System.out.println(direccion.get(0).getCountryName());
                // System.out.println(direccion.get(0).getLocality());
                tdireccion.setText(direccion1.get(0).getAddressLine(0));

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}
