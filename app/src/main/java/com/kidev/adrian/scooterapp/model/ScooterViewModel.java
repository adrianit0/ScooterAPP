package com.kidev.adrian.scooterapp.model;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kidev.adrian.scooterapp.entities.Scooter;
import com.kidev.adrian.scooterapp.enums.EstadoAlquiler;
import com.kidev.adrian.scooterapp.inteface.CallbackRespuesta;
import com.kidev.adrian.scooterapp.util.AndroidUtil;
import com.kidev.adrian.scooterapp.util.ConectorTCP;
import com.kidev.adrian.scooterapp.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScooterViewModel extends AndroidViewModel {

    private MutableLiveData<List<Scooter>> scooters;
    private MutableLiveData<LatLng> tuPosicion;

    private EstadoAlquiler estadoAlquiler;
    private Scooter scooterReservada;
    private String actualFragment;

    private long timeRemain=0;
    private Integer scooterID=null;

    public ScooterViewModel(@NonNull Application application) {
        super(application);
    }


    public LiveData<List<Scooter>> getScooters (Activity activity, double latitude, double longitude, boolean forzar) {
        if (forzar || scooters==null) {
            scooters = new MutableLiveData<>();
            getScooter(latitude, longitude, activity);
        }

        return scooters;
    }

    public LiveData<LatLng> getTuPosicion () {
        if (tuPosicion==null) {
            tuPosicion = new MutableLiveData<>();
            //TODO: Coger tu posicion
        }

        return tuPosicion;
    }

    public LatLng getLastPosition() {
        if (tuPosicion==null)
            return null;

        return tuPosicion.getValue();
    }

    public void changePosition (LatLng posicion) {
        tuPosicion.setValue(posicion);
    }

    public Integer getScooterID() {
        return scooterID;
    }

    public void setScooterID(Integer scooterID) {
        this.scooterID = scooterID;
    }

    public long getTimeRemain() {
        return timeRemain;
    }

    public void setTimeRemain(long timeRemain) {
        this.timeRemain = timeRemain;
    }

    public Scooter getScooterReservada() {
        return scooterReservada;
    }
    public void setScooterReservada(Scooter scooter) {
        scooterReservada=scooter;
    }

    public String getActualFragment() {
        return actualFragment;
    }

    public void setActualFragment(String actualFragment) {
        this.actualFragment = actualFragment;
    }

    public EstadoAlquiler getEstadoAlquiler() {
        return estadoAlquiler;
    }

    public void setEstadoAlquiler(EstadoAlquiler estadoAlquiler) {
        this.estadoAlquiler = estadoAlquiler;
    }

    private void getScooter (double latitude, double longitude, final Activity activity) {
        final Map<String, String> parametros = new HashMap<>();
        parametros.put("lat", latitude + "");
        parametros.put("lon", longitude + "");

        if (scooterID==null) {
            traerScooters(parametros, activity);
        } else {
            parametros.put("id", scooterID.toString());
            List<Scooter> scooters = new ArrayList<>();
            ConectorTCP.getInstance().realizarConexion(activity,"getScooterById", parametros, new CallbackRespuesta() {
                @Override
                public void success(Map<String, String> contenido) {
                    int id = Integer.parseInt(contenido.get("id"));
                    int codigo = Integer.parseInt(contenido.get("codigo"));
                    String noSerie = contenido.get("noSerie");
                    float bateria = Float.parseFloat(contenido.get("bateria"));
                    float lat = Float.parseFloat(contenido.get("posicionLat"));
                    float lon = Float.parseFloat(contenido.get("posicionLon"));

                    Scooter scooter = new Scooter();
                    scooter.setId(id);
                    scooter.setCodigo(codigo);
                    scooter.setNoSerie(noSerie);
                    scooter.setPosicion(new LatLng(lat, lon));
                    scooter.setBateria(bateria);

                    String direccion = AndroidUtil.getStreetName(activity, lat, lon);
                    scooter.setDireccion(direccion);

                    scooterID = null;
                    scooterReservada = scooter;

                    traerScooters(parametros, activity);
                }

                @Override
                public void error(Map<String, String> contenido, Util.CODIGO codigoError) {
                    Log.e("Error de conexión", "No se han cargado las scooters " + codigoError.toString());
                    AndroidUtil.crearToast(activity, "No se han podido cargar las scooters");
                }
            });
        }
    }

    private void traerScooters (Map<String,String> parametros, final Activity activity) {
        ConectorTCP.getInstance().realizarConexion(activity,"getScooters", parametros, new CallbackRespuesta() {
            @Override
            public void success(Map<String, String> contenido) {
                int length = Integer.parseInt(contenido.get("length"));
                Log.i("Conexión exitosa", "Se han recuperado " + length + " scooters");

                List<Scooter> scooterList = new ArrayList<>();

                for (int i = 0; i < length; i++) {
                    int id = Integer.parseInt(contenido.get("id[" + i + "]"));
                    int codigo = Integer.parseInt(contenido.get("codigo[" + i + "]"));
                    String noSerie = contenido.get("noSerie[" + i + "]");
                    float bateria = Float.parseFloat(contenido.get("bateria[" + i + "]"));
                    float lat = Float.parseFloat(contenido.get("posicionLat[" + i + "]"));
                    float lon = Float.parseFloat(contenido.get("posicionLon[" + i + "]"));

                    Scooter scooter = new Scooter();
                    scooter.setId(id);
                    scooter.setCodigo(codigo);
                    scooter.setNoSerie(noSerie);
                    scooter.setPosicion(new LatLng(lat, lon));
                    scooter.setBateria(bateria);

                    String direccion = AndroidUtil.getStreetName(activity, lat, lon);
                    scooter.setDireccion(direccion);

                    scooterList.add(scooter);
                }

                scooters.setValue(scooterList);
            }

            @Override
            public void error(Map<String, String> contenido, Util.CODIGO codigoError) {
                Log.e("Error de conexión", "No se han cargado las scooters " + codigoError.toString());
                AndroidUtil.crearToast(activity, "No se han podido cargar las scooters");
            }
        });
    }

    private void cogerTuPosicion () {

    }
}
