package com.example.pic2web;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Creamos los objetos que vamos a utilizar
    EditText et_email, et_pass;
    Button btn_login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Enlazamos los objetos con los elementos de la vista
        et_email = findViewById(R.id.user_email);
        et_pass = findViewById(R.id.user_password);
        btn_login = findViewById(R.id.login);
        // Recuperamos los datos del Login (revisar funcion recuperar() mas abajo)
        recuperar();
        // Al darle al botón Ingresar hace la consulta con el archivo query de php
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"Iniciando sesión...",Toast.LENGTH_SHORT).show();
                // Intenta ingresar con el correo y contraseña digitados (revisar funcion login_user mas abajo)
                login_user("https://bellaspa.com.co/login-app.php");
            }
        });
    }
    public void login_user(String URL){
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            // Reescribir la función onResponse
            @Override
            public void onResponse(String response) {
                // Si los campos Email y Contraseña no están vacíos...
                if(!et_email.getText().toString().isEmpty() && !et_pass.getText().toString().isEmpty()){
                    // Y si la respuesta del query no es vacía...
                    if(!response.isEmpty()){
                        // Guardamos los datos del usuario para luego (revisar funcion guardarSesion() mas abajo)
                        guardarSesion();
                        // Creamos un intent para enviarlos a la vista Home
                        Intent intent = new Intent(getApplicationContext(),home.class);
                        intent.putExtra("user_email",et_email.getText().toString());
                        intent.putExtra("user_pass",et_pass.getText().toString());
                        // Direccionamos al usuario a Home
                        startActivity(intent);
                        finish();
                    }else{
                        // En caso contrario, los datos suministrados son incorrectos
                        Toast.makeText(MainActivity.this,"Usuario o contraseña incorrecta",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // En caso contrario, ingrese ambos campos antes de ingresar
                    Toast.makeText(MainActivity.this,"Por favor, complete los campos antes de continuar",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            // Si hay un error imprímelo en la terminal
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Hubo un problema al iniciar",Toast.LENGTH_SHORT).show();
            }
        }){
            // Enviar los datos solicitados a través de HashMap
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("email",et_email.getText().toString());
                params.put("password",et_pass.getText().toString());
                return params;
            }
        };
        // Hacemos el request de Volley
        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
    }
    // Guardamos el correo y contraseña en caso de ser correctos para evitar digitarlos cada que se inicie la app
    public void guardarSesion(){
        SharedPreferences pref = getSharedPreferences("keeplogin", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("usser_email",et_email.getText().toString());
        editor.putString("usser_password",et_pass.getText().toString());
        editor.putBoolean("login",true);
        editor.commit();
    }
    // Recuperar los datos en caso de haber ingresado anteriormente con una cuenta
    private void recuperar(){
        SharedPreferences pref = getSharedPreferences("keeplogin",Context.MODE_PRIVATE);
        et_email.setText(pref.getString("usser_email",""));
        et_pass.setText(pref.getString("usser_password",""));
    }
}
