package com.kidev.adrian.scooterapp.fragments;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kidev.adrian.scooterapp.R;
import com.kidev.adrian.scooterapp.util.CallbackRespuesta;
import com.kidev.adrian.scooterapp.util.ConectorTCP;
import com.kidev.adrian.scooterapp.util.Util;

import java.util.HashMap;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private MapView mMapView;
    private GoogleMap mMap;

    private LinearLayout surface;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);



        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

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

        googleMap.setMyLocationEnabled(true);

        Location myPos = googleMap.getMyLocation();

        Map<String,String> parametros = new HashMap<String,String>();
        parametros.put("lat", "1");
        parametros.put("lon", "1");

        ConectorTCP.getInstance().realizarConexion("getScooters", parametros, new CallbackRespuesta() {
            @Override
            public void success(Map<String, String> contenido) {
                int length = Integer.parseInt(contenido.get("length"));
                Log.i("Conexión exitosa", "Se han recuperado "+length+ " scooters");

                for (int i = 0; i < length; i++) {
                    float lat = Float.parseFloat(contenido.get("posicionLat["+i+"]"));
                    float lon = Float.parseFloat(contenido.get("posicionLon["+i+"]"));

                    LatLng coordenadas = new LatLng(lat, lon);

                    mMap.addMarker(new MarkerOptions()
                                    .position(coordenadas)
                                    .title(contenido.get("matricula["+i+"]"))
                    );

                    if (i==0)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordenadas));
                }
            }

            @Override
            public void error(Map<String, String> contenido, Util.CODIGO codigoError) {
                Log.e("Error de conexión", "No se han cargado las scooters " + codigoError.toString());
            }
        });



    }
}
