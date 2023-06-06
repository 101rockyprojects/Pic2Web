package com.example.pic2web;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class home extends AppCompatActivity {

    private LocationManager ubicacion;
    TextView tdireccion;
    Button bguardar, btnCamara;
    // DataBase miBD;
    EditText descripcion;
    ImageView imgView;
    boolean taken = false;
    RequestQueue requestQueue;
    int id;
    Bitmap imgBitmap;

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
        descripcion = findViewById(R.id.desc);

        localizacion();
        estadoGPS();

        bguardar = findViewById(R.id.button);
        bguardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user_email = save();
                id = buscar_por_email("https://bellaspa.com.co/search-app.php?email="+user_email);
                insertarDatos("https://bellaspa.com.co/save-app.php");
            }


            public void insertarDatos(String URL) {
                final ProgressDialog progressDialog = new ProgressDialog(home.this);
                progressDialog.setMessage("Cargando... \n Espera unos segundos :)");
                // Guardamos lo que se ingreso en los EditText
                String desc = descripcion.getText().toString();
                String direc= tdireccion.getText().toString();
                // Siempre que haya un campo sin haber completado
                if(direc.isEmpty()){
                    Toast.makeText(home.this, "Espera mientras triangulamos tu ubicación",Toast.LENGTH_LONG).show();
                } else {
                    if(desc.isEmpty() || !taken){
                        // Imprimir que se llenen todos los campos
                        Toast.makeText(home.this, "Ingresa todos los datos antes de continuar",Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        progressDialog.show();
                        StringRequest request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (response.contains("Agregado exitosamente")) {
                                    Toast.makeText(home.this, "Agregado correctamente", Toast.LENGTH_SHORT).show();
                                    descripcion.setText("");
                                    tdireccion.setText("");
                                    progressDialog.dismiss();
                                } else {
                                    // descripcion.setText("RESPONSE"+response);
                                    Toast.makeText(home.this, response, Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
                                }
                            }
                        },new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error){
                                // descripcion.setText("VOLLEY"+error.getMessage());
                                progressDialog.dismiss();
                            }
                        }){
                            // Obtener y mandar todos los datos por params y subirlo al archivo PHP que hace el INSERT en la BDD
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String,String> params = new HashMap<String,String>();
                                //fecha_hora se digita al insertar en PHP
                                String bitmap = getStringImage();
                                params.put("usuario_id",String.valueOf(1));
                                params.put("img_src",bitmap);
                                params.put("descripcion",desc);
                                params.put("ubicacion",direc);
                                return params;
                            }
                        };

                        requestQueue = Volley.newRequestQueue(home.this);
                        requestQueue.add(request);
                    }
                }

            }
        });
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }
    public String getStringImage(){
        ByteArrayOutputStream img = new ByteArrayOutputStream();
        Bitmap bitmap = ((BitmapDrawable) imgView.getDrawable()).getBitmap();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,img);
        byte[] imageBytes = img.toByteArray();
        String encodedImg = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImg;
    }
    private String save(){
        SharedPreferences pref = getSharedPreferences("keeplogin",Context.MODE_PRIVATE);
        String email_a_buscar;
        email_a_buscar = pref.getString("usser_email","");
        return email_a_buscar;
    }
    private int buscar_por_email(String URL){
        JsonArrayRequest jsonar = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jso = null;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        jso = response.getJSONObject(i);
                        id = Integer.parseInt(jso.getString("id"));
                    } catch (JSONException e) {
                        // descripcion.setText("EMAIL1"+e.getMessage());
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                descripcion.setText("EMAIL2"+error.getMessage());
                Toast.makeText(getApplicationContext(), "Error al conectar", Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue rq = Volley.newRequestQueue(home.this);
        rq.add(jsonar);
        return id;
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
            taken = true;
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
            // longitud.setText("Longitud: " + String.valueOf(loc.getLongitude()));
            // latitud.setText("Latitud: " + String.valueOf(loc.getLatitude()));

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
