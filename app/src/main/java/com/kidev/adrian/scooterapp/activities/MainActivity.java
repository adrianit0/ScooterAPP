package com.kidev.adrian.scooterapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kidev.adrian.scooterapp.R;
import com.kidev.adrian.scooterapp.inteface.IOnInputDialog;
import com.kidev.adrian.scooterapp.util.AndroidUtil;
import com.kidev.adrian.scooterapp.inteface.CallbackRespuesta;
import com.kidev.adrian.scooterapp.util.ConectorTCP;
import com.kidev.adrian.scooterapp.util.Util;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button loginButton;
    private Button registerButton;

    private LinearLayout layoutLogin;
    private LinearLayout layoutPortada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        if (getIntent().getStringExtra("restart")!=null) {
            borrarInfo();
        }

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String ip = sharedPref.getString("IP", null);
        String nick = sharedPref.getString("nick", null);
        String pass = sharedPref.getString("pass", null);

        layoutLogin = findViewById(R.id.layoutLogin);
        layoutPortada = findViewById(R.id.layoutPortada);

        if (ip==null) {
            mostrarConfiguracionIP();
        }

        if (nick!=null && pass!=null) {
            layoutLogin.setVisibility(View.GONE);
            layoutPortada.setVisibility(View.VISIBLE);
            login(nick, pass);
        }

        final Activity activity = this;

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textoNick = findViewById(R.id.nickLogin);
                TextView textoPass = findViewById(R.id.passLogin);

                if (textoNick.getText().toString().isEmpty()) {
                    AndroidUtil.crearErrorDialog(activity, getApplicationContext().getString(R.string.error_nick));
                    return;
                } else if (textoPass.getText().toString().isEmpty()) {
                    AndroidUtil.crearErrorDialog(activity, getApplicationContext().getString(R.string.error_pass));
                    return;
                }

                login(textoNick.getText().toString(), Util.getMd5(textoPass.getText().toString()));
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings2) {
            mostrarConfiguracionIP();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void login (String nick, String pass) {
        Map<String,String> parametros = new HashMap<>();
        parametros.put("nick", nick);
        parametros.put("pass", pass);

        // Guardamos la info para cuando volvamos a conectarnos no tener que pedir de nuevo el login
        guardarInfo(ConectorTCP.getHostServerName(), nick, pass);

        loginButton.setEnabled(false);

        final Activity activity = this;

        ConectorTCP.getInstance().realizarConexion(this,"login", parametros, new CallbackRespuesta() {
            @Override
            public void success(Map<String, String> contenido) {
                Intent intent = new Intent(getApplication(), MenuActivity.class);
                for (Map.Entry<String, String> entry : contenido.entrySet()) {
                    intent.putExtra(entry.getKey(), entry.getValue());
                }

                startActivity(intent);
            }

            @Override
            public void error(Map<String, String> contenido, Util.CODIGO codigoError) {
                AndroidUtil.crearErrorDialog(activity, contenido.get("error"));
                loginButton.setEnabled(true);

                borrarInfo();
                layoutLogin.setVisibility(View.VISIBLE);
                layoutPortada.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Nos salimos del programa
        finishAffinity();
    }

    private void mostrarConfiguracionIP() {
        // Configuración previa de la IP
        String serverHost = ConectorTCP.getHostServerName();
        AndroidUtil.crearInputDialog(this, "Introducir IP", serverHost, new IOnInputDialog() {
            @Override
            public void onAccept(String message) {
                ConectorTCP.setHostServerName(message);
                ConectorTCP.getInstance();
            }

            @Override
            public void onCancel(String message) {
                ConectorTCP.getInstance();
            }
        });
        // Fin configuración de la IP
    }

    private void guardarInfo (String ip, String nick, String pass) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("IP", ip);
        editor.putString("nick", nick);
        editor.putString("pass", pass);
        editor.commit();
    }

    private void borrarInfo () {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("IP");
        editor.remove("nick");
        editor.remove("pass");
        editor.commit();
    }
}
