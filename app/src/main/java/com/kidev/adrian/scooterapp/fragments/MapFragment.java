package com.kidev.adrian.scooterapp.fragments;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kidev.adrian.scooterapp.R;
import com.kidev.adrian.scooterapp.entities.Scooter;
import com.kidev.adrian.scooterapp.util.CallbackRespuesta;
import com.kidev.adrian.scooterapp.util.ConectorTCP;
import com.kidev.adrian.scooterapp.util.Util;

import java.util.HashMap;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private MapView mMapView;
    private GoogleMap mMap;

    private LinearLayout surface;

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

        if(!mMap.isMyLocationEnabled())
            mMap.setMyLocationEnabled(true);

        /*LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (myLocation == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            String provider = lm.getBestProvider(criteria, true);
            myLocation = lm.getLastKnownLocation(provider);
        }*/

        //if(myLocation!=null) {
            //final LatLng userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());


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
                        float lat = Float.parseFloat(contenido.get("posicionLat[" + i + "]"));
                        float lon = Float.parseFloat(contenido.get("posicionLon[" + i + "]"));

                        Scooter scooter = new Scooter();
                        scooter.setId(id);
                        scooter.setCodigo(codigo);
                        scooter.setPosicion(new LatLng(lat, lon));

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
        //} else {
        //    Log.e("MAP ERROR", "No se ha podido cargar la posición de las Scooters");
        //}
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
        if (surface!=null) {
            surface.setVisibility(View.VISIBLE);
        } else {
            Log.e("ERROR map", "El surface no es visible");
        }

        return true;
    }
}