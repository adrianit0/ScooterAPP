package com.kidev.adrian.scooterapp.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.kidev.adrian.scooterapp.R;
import com.kidev.adrian.scooterapp.inteface.CallbackRespuesta;
import com.kidev.adrian.scooterapp.util.ConectorTCP;
import com.kidev.adrian.scooterapp.util.Util;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private Button registerButton;

    private TextView textoNick;
    private TextView textoNombre;
    private TextView textoApellidos;
    private TextView textoEmail;
    private TextView textoPass1;
    private TextView textoPass2;
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        textoNick = findViewById(R.id.nickRegister);
        textoNombre = findViewById(R.id.nameRegister);
        textoApellidos = findViewById(R.id.surnameRegister);
        textoEmail = findViewById(R.id.emailRegister);
        textoPass1 = findViewById(R.id.passRegister);
        textoPass2 = findViewById(R.id.passAgainRegister);
        checkBox = findViewById(R.id.termRegister);

        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarse();
            }
        });
    }

    private void registrarse () {
        if (!checkBox.isChecked()) {
            mostrarError("", "Tienes que aceptar los términos y condiciones");
            return;
        }

        if (isEmpty(textoNick) || isEmpty(textoNombre) || isEmpty(textoApellidos) || isEmpty(textoEmail) || isEmpty(textoPass1) || isEmpty(textoPass2)) {
            mostrarError("", "No puedes dejar ningún campo en blanco");
            return;
        }

        if (!textoPass1.getText().toString().equals(textoPass2.getText().toString())) {
            mostrarError("", "Las contraseñas debes coincidir");
            return;
        }

        registerButton.setEnabled(false);

        Map<String,String> parametros = new HashMap<>();
        parametros.put("nick", textoNick.getText().toString());
        parametros.put("nombre", textoNombre.getText().toString());
        parametros.put("apellido1", textoApellidos.getText().toString());
        parametros.put("email", textoEmail.getText().toString());
        parametros.put("pass", textoPass1.getText().toString());

        ConectorTCP.getInstance().realizarConexion("register", parametros, new CallbackRespuesta() {
            @Override
            public void success(Map<String, String> contenido) {
                Log.e("Registrado", contenido.get("nick") + " se ha registrado correctamente");
                // Si se puede registrar, se conecta
                Intent intent = new Intent(getApplication(), MenuActivity.class);
                for (Map.Entry<String, String> entry : contenido.entrySet()) {
                    intent.putExtra(entry.getKey(), entry.getValue());
                }

                startActivity(intent);
            }

            @Override
            public void error(Map<String, String> contenido, Util.CODIGO codigoError) {
                Log.e("Error de conexión", codigoError.toString());

                mostrarError("Error", contenido.get("error"));
                registerButton.setEnabled(true);
            }
        });
    }

    private void mostrarError (String title, String texto) {
        (Toast.makeText(getApplicationContext(), title + ": "+ texto, Toast.LENGTH_LONG)).show();
    }

    private boolean isEmpty (TextView t) {
        return t.getText().equals("");
    }
}
