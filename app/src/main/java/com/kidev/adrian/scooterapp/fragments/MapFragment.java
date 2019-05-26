package com.kidev.adrian.scooterapp.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.kidev.adrian.scooterapp.R;
import com.kidev.adrian.scooterapp.activities.MenuActivity;
import com.kidev.adrian.scooterapp.entities.Scooter;
import com.kidev.adrian.scooterapp.inteface.IOnRequestPermission;
import com.kidev.adrian.scooterapp.util.CallbackRespuesta;
import com.kidev.adrian.scooterapp.util.ConectorTCP;
import com.kidev.adrian.scooterapp.util.Util;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private final String TAG = this.getTag();

    private MapView mMapView;
    private GoogleMap mMap;

    private LinearLayout surface;

    private Scooter scooterSeleccionada = null;
    private int position_permission_code = 1;
    private boolean permisoLocalizacion=false;
    private LatLng myLastPosition = null;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        View include = (View) view.findViewById(R.id.include);
        surface = (LinearLayout) view.findViewById(R.id.include);
        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        surface.setVisibility(View.GONE);

        Button botonCerrarSurface = surface.findViewById(R.id.botonCerrarSurface);
        botonCerrarSurface.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    surface.setVisibility(View.GONE);
                    scooterSeleccionada = null;
                }
            }
        );

        Button botonReservar = surface.findViewById(R.id.botonReservar);
        botonReservar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scooterSeleccionada==null) {
                    Toast.makeText(getActivity(), "No hay Scooter seleccionada", Toast.LENGTH_LONG);
                } else {
                    // TODO: Hacer la reserva

                }
            }
        });

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surface = getActivity().findViewById(R.id.scooter_surface);
        if (surface!=null)
            surface.setVisibility(View.GONE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setOnMarkerClickListener(this);

        ((MenuActivity)getActivity()).pedirPermiso(Manifest.permission.ACCESS_FINE_LOCATION, position_permission_code, new IOnRequestPermission() {
            @Override
            public void onPermissionAccepted(String permiso) {
                realizarConexion();
            }

            @Override
            public void onPermissionDenied(String permiso) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Permiso denegado")
                        .setMessage("No tienes permisos para acceder a la cámara. No se mostrará tu posición actual.")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (permisoLocalizacion) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                myLastPosition = null;
                // getLocationPermission();
                Toast.makeText(getContext(), "No tienes permisos para coger tu ubicación", Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            if (permisoLocalizacion) {
                Task locationResult = null;//mFusedLocationProviderClient.getLastLocation();
                if (locationResult!=null) {
                    locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                // Set the map's camera position to the current location of the device.
                                myLastPosition = (LatLng) task.getResult();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( new LatLng(myLastPosition.latitude, myLastPosition.longitude), 40));
                            } else {
                                Log.d(TAG, "Current location is null. Using defaults.");
                                Log.e(TAG, "Exception: %s", task.getException());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLastPosition, 1));
                                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }
                        }
                    });
                } else {
                    Log.e("MapFragment::getDevice", "No se ha encontrado la posición actual");
                }
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void realizarConexion () {
        permisoLocalizacion=true;
        updateLocationUI();
        getDeviceLocation();

        double latitude = 1;
        double longitude = 1;

        Map<String, String> parametros = new HashMap<String, String>();
        parametros.put("lat", latitude + "");
        parametros.put("lon", longitude + "");

        ConectorTCP.getInstance().realizarConexion("getScooters", parametros, new CallbackRespuesta() {
            @Override
            public void success(Map<String, String> contenido) {
                int length = Integer.parseInt(contenido.get("length"));
                Log.i("Conexión exitosa", "Se han recuperado " + length + " scooters");

                LatLng pos = new LatLng(0,0);

                for (int i = 0; i < length; i++) {
                    int id = Integer.parseInt(contenido.get("id[" + i + "]"));
                    int codigo = Integer.parseInt(contenido.get("codigo[" + i + "]"));
                    float bateria = Float.parseFloat(contenido.get("bateria[" + i + "]"));
                    float lat = Float.parseFloat(contenido.get("posicionLat[" + i + "]"));
                    float lon = Float.parseFloat(contenido.get("posicionLon[" + i + "]"));

                    Scooter scooter = new Scooter();
                    scooter.setId(id);
                    scooter.setCodigo(codigo);
                    scooter.setPosicion(new LatLng(lat, lon));
                    scooter.setBateria(bateria);

                    mMap.addMarker(new MarkerOptions()
                            .position(scooter.getPosicion())
                    ).setTag(scooter);

                    if (i==0)
                        pos = new LatLng(lat, lon);
                }

                mMap.moveCamera((CameraUpdateFactory.newLatLng(pos)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 14), 1500, null);

            }

            @Override
            public void error(Map<String, String> contenido, Util.CODIGO codigoError) {
                Log.e("Error de conexión", "No se han cargado las scooters " + codigoError.toString());
            }
        });
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14), 1500, null);
        Scooter scooter = (Scooter) marker.getTag();
        scooterSeleccionada = scooter;
        if (surface!=null) {
            surface.setVisibility(View.VISIBLE);

            TextView matriculaText = surface.findViewById(R.id.textoModelo);
            TextView bateriaText = surface.findViewById(R.id.textoBateria);
            TextView distanciaText = surface.findViewById(R.id.textoDistancia);
            TextView calleText = surface.findViewById(R.id.textoCalle);

            int bateria = Math.round(scooter.getBateria()*100);
            int km = Math.round(bateria/1.8f); // Un valor random
            bateriaText.setText(bateria+ "% (~"+ km +" km)");

            //TODO: Añadir tambien la versión del modelo
            //TODO: Cambiar la foto según que modelo sea
            matriculaText.setText(scooter.getCodigo() + " - Scooter Modelo 1");

            // TODO: Poner la diferencia entre la scooter y tu
            distanciaText.setText("?m");

            // TODO: poner la dirección
            calleText.setText("Dirección desconocia");
        } else {
            Log.e("ERROR map", "El surface no es visible");
        }

        return true;
    }
}