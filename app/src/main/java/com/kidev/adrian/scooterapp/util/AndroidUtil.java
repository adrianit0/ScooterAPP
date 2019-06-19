package com.kidev.adrian.scooterapp.util;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.kidev.adrian.scooterapp.R;
import com.kidev.adrian.scooterapp.activities.MainActivity;
import com.kidev.adrian.scooterapp.inteface.IOnInputDialog;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AndroidUtil {

    private static Geocoder geocoder;

    public static int getDistanceBetweenTwoPoints (LatLng pos1, LatLng pos2) {
        float[] resultados = new float[1];
        Location.distanceBetween(pos1.latitude, pos1.longitude, pos2.latitude, pos2.longitude, resultados);
        int distancia = Math.round(resultados[0]);
        return distancia;
    }

    public static String getStreetName (Activity activity, LatLng posicion) {
        return getStreetName(activity, posicion.latitude, posicion.longitude);
    }

    public static String getStreetName (Activity activity, double latitude, double longitude) {
        if (geocoder==null)
            geocoder = new Geocoder(activity, Locale.getDefault());

        try {
            List<Address> direcciones = geocoder.getFromLocation(latitude, longitude, 10);

            if (!direcciones.isEmpty()) {
                return direcciones.get(0).getAddressLine(0);
            }
            Log.e("MAP show", "Lista de direcciones vacía");
        } catch(IOException e) {
            Log.e("Error MAP geocoder", "Error al encontrar: " + e.getMessage());
        }

        return "Dirección desconocida";
    }

    public static void crearToast (Activity activity, String message) {
        Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public static void crearDialog (Activity activity, String title, String message, DialogInterface.OnClickListener callback) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("ok", callback)
                .create().show();
    }

    public static void crearErrorDialog (Activity activity, String message){
        new AlertDialog.Builder(activity)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("ok", null)
                .create().show();
    }

    public static void crearAcceptDialog (Activity activity, String title, String message, DialogInterface.OnClickListener yesCallback, DialogInterface.OnClickListener noCallback) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Sí", yesCallback)
                .setNegativeButton("No", noCallback)
                .create().show();
    }

    public static void crearInputDialog (Activity activity, String title, String initialMessage, final IOnInputDialog callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);

        // Creo un nuevo editText
        final EditText input = new EditText(activity);
        //input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setText(initialMessage);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onAccept(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onCancel(input.getText().toString());
            }
        });

        builder.show();
    }

    public static void crearViewDialog (Activity activity, View view, String title, DialogInterface.OnClickListener callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setView(view);
        builder.setPositiveButton("Aceptar", callback);
        builder.show();
    }

    public static void crearNotificacion (Activity activity, String title, String text, String subText, int smallIcon) {
        Intent intent = new Intent(activity, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 01, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification.Builder builder = new Notification.Builder(activity.getApplicationContext());
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setSubText(subText);
        builder.setNumber(101);
        builder.setContentIntent(pendingIntent);
        builder.setTicker("Scooter APP - " + title);
        builder.setSmallIcon(smallIcon);
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_MAX);
        Notification notification = builder.build();

        NotificationManager notificationManger = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManger.notify(01, notification);
    }
}