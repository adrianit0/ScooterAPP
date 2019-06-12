package com.kidev.adrian.scooterapp.fragments;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.kidev.adrian.scooterapp.enums.EstadoAlquiler;
import com.kidev.adrian.scooterapp.inteface.IOnInputDialog;
import com.kidev.adrian.scooterapp.inteface.IOnQrDetected;
import com.kidev.adrian.scooterapp.inteface.IOnRequestPermission;
import com.kidev.adrian.scooterapp.inteface.IOnTimeFinished;
import com.kidev.adrian.scooterapp.model.ScooterViewModel;
import com.kidev.adrian.scooterapp.util.AndroidUtil;
import com.kidev.adrian.scooterapp.inteface.CallbackRespuesta;
import com.kidev.adrian.scooterapp.util.ConectorTCP;
import com.kidev.adrian.scooterapp.util.Cronometro;
import com.kidev.adrian.scooterapp.util.Util;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private final String TAG = this.getTag();

    private MapView mMapView;
    private GoogleMap mMap;
    private ScooterViewModel scooterViewModel;

    private MenuActivity menuActivity;

    private LinearLayout surface;           // Surface donde estará la info de la scooter actual
    private LinearLayout surfaceReserva;    // Surface donde estará la info de la reserva
    private LinearLayout surfaceContador;   // Contador donde estará puesto el tiempo
    private LinearLayout surfaceFinAlquiler;// Mensaje de fin de alquiler

    private TextView textViewTituloContador;
    private TextView textViewContador;

    private Scooter scooterSeleccionada = null;
    private int position_permission_code = 1;
    private boolean permisoLocalizacion=false;
    private LatLng myLastPosition = null;

    private Button botonReservar;
    private Button botonCancelarReserva;
    private Button botonAlquiler;
    private Button botonFinAlquiler;
    private Button botonResumenAlquiler;

    private Button botonActualizar;
    private Button botonIncidencia;
    private Button botonShop;

    private Cronometro cronometro;

    private List<Marker> markers;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstance) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        markers = new ArrayList<>();

        menuActivity = (MenuActivity) getActivity();
        scooterViewModel = ViewModelProviders.of(this).get(ScooterViewModel.class);

        // Surfaces
        surface = view.findViewById(R.id.include);
        surfaceReserva = view.findViewById(R.id.includeReserva);
        surfaceContador = view.findViewById(R.id.includeContador);
        surfaceFinAlquiler = view.findViewById(R.id.includeFinAlquiler);

        textViewTituloContador = surfaceContador.findViewById(R.id.tituloContador);
        textViewContador = surfaceContador.findViewById(R.id.contadorMap);
        surfaceFinAlquiler.setVisibility(View.GONE);

        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstance);

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

        botonReservar = surface.findViewById(R.id.botonReservar);
        botonCancelarReserva = surfaceReserva.findViewById(R.id.botonCancelarReserva);
        botonAlquiler = surfaceReserva.findViewById(R.id.botonAlquilar);
        botonFinAlquiler = surfaceReserva.findViewById(R.id.botonFinAlquilar);
        botonResumenAlquiler = surfaceFinAlquiler.findViewById(R.id.botonCerrarSurface);

        botonActualizar = view.findViewById(R.id.botonActualizar);
        botonIncidencia = view.findViewById(R.id.botonIncidencia);
        botonShop = view.findViewById(R.id.botonComprar);

        botonReservar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scooterSeleccionada==null) {
                    Toast.makeText(getActivity(), "No hay Scooter seleccionada", Toast.LENGTH_LONG);
                } else if (scooterViewModel.getScooterReservada()!=null) {
                    Toast.makeText(getActivity(), "Ya tienes reservada una scooter", Toast.LENGTH_LONG);
                } else {
                    scooterViewModel.setScooterReservada(scooterSeleccionada);

                    Map<String,String> parametros = new HashMap<>();
                    parametros.put("noSerie", scooterSeleccionada.getNoSerie()+"");
                    botonReservar.setEnabled(false);

                    ConectorTCP.getInstance().realizarConexion("reservar", parametros, new CallbackRespuesta() {
                        @Override
                        public void success(Map<String, String> contenido) {
                            realizarReserva();

                            botonReservar.setEnabled(true);
                        }

                        @Override
                        public void error(Map<String, String> contenido, Util.CODIGO codigoError) {
                            AndroidUtil.crearToast(getActivity(), "No se ha podido reservar: " + contenido.get("error"));
                            botonReservar.setEnabled(true);
                            scooterViewModel.setScooterReservada(null);
                        }
                    });
                }
            }
        });

        botonAlquiler.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                menuActivity.showCameraQr(new IOnQrDetected() {
                    @Override
                    public void onQrDetected(String message) {
                        String code="";
                        String [] splitted = message.split("[:]");
                        if (splitted.length==2&&splitted[0].equals("SC")) {
                            code = splitted[1];
                        } else {
                            AndroidUtil.crearDialog(menuActivity, "Error","Código no válido", null);
                            return;
                        }

                        Map<String,String> parametros = new HashMap<>();
                        parametros.put("noSerie", scooterViewModel.getScooterReservada().getNoSerie()+"");
                        parametros.put("codigo", code);

                        ConectorTCP.getInstance().realizarConexion("alquilar", parametros, new CallbackRespuesta() {
                            @Override
                            public void success(Map<String, String> contenido) {
                                empezarAlquiler();
                            }

                            @Override
                            public void error(Map<String, String> contenido, Util.CODIGO codigoError) {
                                AndroidUtil.crearDialog(menuActivity, "Error","No se ha podido alquilar scooter: " + contenido.get("error"), null);
                            }
                        });
                    }
                });
            }
        });


        botonCancelarReserva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                peticionCancelarReserva();
            }
        });

        botonFinAlquiler.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AndroidUtil.crearAcceptDialog(getActivity(), "Confirmación", "¿Quieres finalizar el viaje?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        View view = getLayoutInflater().inflate(R.layout.surface_aviso_alquiler, null);
                        AndroidUtil.crearViewDialog(getActivity(), view, "Confirmación", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                botonFinAlquiler.setEnabled(false);
                                Map<String,String> parametros = new HashMap<>();
                                ConectorTCP.getInstance().realizarConexion("finalizar", parametros, new CallbackRespuesta() {
                                    @Override
                                    public void success(Map<String, String> contenido) {
                                        mostrarInfoAlquiler (contenido);
                                        finalizarAlquiler();
                                    }

                                    @Override
                                    public void error(Map<String, String> contenido, Util.CODIGO codigoError) {
                                        //AndroidUtil.crearToast(getActivity(), "No se ha podido finalizar el alquiler: " + contenido.get("error"));
                                        // TODO: Mejorar esta parte
                                        botonFinAlquiler.setEnabled(true);
                                        AndroidUtil.crearDialog(getActivity(),"Error","No se ha posido finalizar el viaje, si necesita asistencia técnica llame al número que está localizado en la Scooter para finalizar el viaje.",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    }
                                });
                            }
                        });
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
        });

        botonResumenAlquiler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceFinAlquiler.setVisibility(View.GONE);
            }
        });

        botonActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actualizarScooters(true);
            }
        });

        botonIncidencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuActivity.mostrarFragmentByTag("incidencia", true);
            }
        });

        botonShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuActivity.mostrarFragmentByTag("pack", true);
            }
        });

        scooterViewModel.getTuPosicion().observe(this, new Observer<LatLng>() {
            @Override
            public void onChanged(@Nullable LatLng latLng) {
                Log.e("POS ENCONTRADA MAP", "Lat: " + latLng.latitude + " Lon: "+ latLng.longitude);
            }
        });

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        // Mira el estado del alquiler para asignar el que está realmente
        EstadoAlquiler estado = scooterViewModel.getEstadoAlquiler();
        Bundle bundle = getArguments();
        // Volcamos la información que tenemos del servidor en el cliente
        if (estado==null) {
            int estadoAlquiler = bundle.getInt("state");
            switch (estadoAlquiler) {
                case 0:
                    estado = EstadoAlquiler.NADA;
                    break;
                case 1:
                    estado = EstadoAlquiler.RESERVA;
                    break;
                case 2:
                    estado = EstadoAlquiler.ALQUILER;
                    break;
            }
            scooterViewModel.setEstadoAlquiler(estado);
            if (estado!=EstadoAlquiler.NADA) {
                long time = bundle.getLong("time");
                Integer idScooter = bundle.getInt("scooterID");
                scooterViewModel.setTimeRemain(time);
                scooterViewModel.setScooterID(idScooter);
            }
        }
        // obtenemos el tiempo actual
        //TODO: Poner más bonito
        int timeRemain = (int) ((new Date(System.currentTimeMillis())).getTime() - (new Date(scooterViewModel.getTimeRemain())).getTime()) / 1000;

        cambiarEstado(estado, timeRemain);

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


        menuActivity.pedirPermiso(Manifest.permission.ACCESS_FINE_LOCATION, position_permission_code, new IOnRequestPermission() {
            @Override
            public void onPermissionAccepted(String permiso) {
                realizarConexion();
            }

            @Override
            public void onPermissionDenied(String permiso) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Permiso denegado")
                        .setMessage("No tienes permisos para acceder al GPS. No se mostrará tu posición actual.")
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
        } catch (SecurityException e) {
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
                                Log.e("MAP Tu posicion","TU POSICION: " + myLastPosition.latitude + " - " + myLastPosition.longitude );
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

        actualizarScooters(false);
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

        if (scooterViewModel.getScooterReservada()!=null)
            return true;

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

            calleText.setText(scooter.getDireccion());
        } else {
            Log.e("ERROR map", "El surface no es visible");
        }

        return true;
    }

    private void realizarReserva () {
        scooterViewModel.setTimeRemain(new Date(System.currentTimeMillis()).getTime());

        cambiarEstado(EstadoAlquiler.RESERVA);
    }

    private void peticionCancelarReserva() {
        Scooter reservada = scooterViewModel.getScooterReservada();
        if (reservada==null) {
            AndroidUtil.crearDialog(getActivity(), "Error", "No existe moto en reserva", null);
            return;
        }
        Map<String,String> parametros = new HashMap<>();
        parametros.put("noSerie", reservada.getNoSerie()+"");

        botonCancelarReserva.setEnabled(false);
        botonAlquiler.setEnabled(false);

        if (cronometro!=null) {
            cronometro.finalizar();
            cronometro=null;
        }

        Log.e("map", "Se ha cancelado la reserva");

        ConectorTCP.getInstance().realizarConexion("cancelarReserva", parametros, new CallbackRespuesta() {
            @Override
            public void success(Map<String, String> contenido) {
                cancelarReserva();
            }

            @Override
            public void error(Map<String, String> contenido, Util.CODIGO codigoError) {
                AndroidUtil.crearToast(getActivity(), "No se ha podido cancelar la reserva: " + contenido.get("error"));
                this.success(contenido);
            }
        });
    }

    private void cancelarReserva () {
        cambiarEstado(EstadoAlquiler.NADA);
    }

    private void empezarAlquiler () {
        scooterViewModel.setTimeRemain(new Date(System.currentTimeMillis()).getTime());
        cambiarEstado(EstadoAlquiler.ALQUILER);
    }

    private void finalizarAlquiler () {
        botonFinAlquiler.setEnabled(true);
        if (cronometro!=null){
            cronometro.finalizar();
            cronometro=null;
        }
        cambiarEstado(EstadoAlquiler.NADA);
    }

    private void mostrarInfoAlquiler(Map<String,String> parametros) {
        int minutosConducidos = Integer.parseInt(parametros.get("minutosConducidos"));
        int minutosConsumidos = Integer.parseInt(parametros.get("minutosConsumidos"));
        int minutosRestantes = Integer.parseInt(parametros.get("minutosRestante"));
        double costeTotal = Double.parseDouble(parametros.get("costeTotal"));
        int minutosFueraBono = minutosConducidos-minutosConsumidos;
        double costeMinuto = costeTotal/minutosFueraBono;

        TextView textoModelo = surfaceFinAlquiler.findViewById(R.id.textoModelo);
        TextView textoMinutosConducidos = surfaceFinAlquiler.findViewById(R.id.textoDuracion);
        TextView textoMinutosConsumidos = surfaceFinAlquiler.findViewById(R.id.textoBonoConsumido);
        TextView textoMinutosRestantes = surfaceFinAlquiler.findViewById(R.id.textoBonoRestante);

        // TODO: Cambiar el modelo por el que sea de verdad
        textoModelo.setText(scooterViewModel.getScooterReservada().getMatricula() + " - Modelo 1");
        // TODO: Meter en constantes
        textoMinutosConducidos.setText(minutosConducidos + " minutos");
        textoMinutosConsumidos.setText(minutosConsumidos + " minutos");
        textoMinutosRestantes.setText(minutosRestantes + " minutos");

        LinearLayout linearMinutosSinBonos = surfaceFinAlquiler.findViewById(R.id.linearMinutosSinBonos);
        LinearLayout linearPrecioMinuto = surfaceFinAlquiler.findViewById(R.id.linearCoste);
        LinearLayout linearPrecioFinal = surfaceFinAlquiler.findViewById(R.id.linearPrecioFinal);

        linearMinutosSinBonos.setVisibility(minutosFueraBono>0 ? View.VISIBLE : View.GONE);
        linearPrecioMinuto.setVisibility(minutosFueraBono>0 ? View.VISIBLE : View.GONE);
        linearPrecioFinal.setVisibility(minutosFueraBono>0 ? View.VISIBLE : View.GONE);

        if (minutosFueraBono>0) {
            TextView textoMinutosSinBonos = surfaceFinAlquiler.findViewById(R.id.textoMinutosSinBonos);
            TextView textoPrecioMinuto = surfaceFinAlquiler.findViewById(R.id.textoCoste);
            TextView textoPrecioFinal = surfaceFinAlquiler.findViewById(R.id.textoPrecioFinal);

            textoMinutosSinBonos.setText(minutosFueraBono+" minutos");
            textoPrecioMinuto.setText(costeMinuto + "€/minuto");
            textoPrecioFinal.setText(costeTotal+"€");
        }

        menuActivity.actualizarMinutos(minutosRestantes);
        surfaceFinAlquiler.setVisibility(View.VISIBLE);
    }

    private void actualizarScooters (boolean forzar) {
        updateLocationUI();
        getDeviceLocation();

        double latitude = 1;
        double longitude = 1;

        // Eliminamos los anteriores markers si los hubiera
        for (Marker m : markers)
            m.remove();
        markers.clear();

        scooterViewModel.getScooters(getActivity(), latitude, longitude, forzar).observe(getActivity(), new Observer<List<Scooter>>() {
            @Override
            public void onChanged(@Nullable List<Scooter> scooters) {
                Scooter scooterReservada = scooterViewModel.getScooterReservada();

                LatLng pos = new LatLng(0,0);


                int i = 0;
                for (Scooter scooter : scooters) {
                    MarkerOptions markerOptions = new MarkerOptions().position(scooter.getPosicion());
                    Marker marker = mMap.addMarker(markerOptions);
                    marker.setTag(scooter);

                    markers.add(marker);

                    if (0==i++)
                        pos = scooter.getPosicion();
                }
                // Si hubiera una moto alquilada ya se mostrará esta
                if (scooterReservada!=null) {
                    // Ocultamos los actuales
                    for(Marker m : markers)
                        m.setVisible(false);

                    pos = scooterReservada.getPosicion();
                    MarkerOptions markerOptions = new MarkerOptions().position(scooterReservada.getPosicion());
                    Marker marker = mMap.addMarker(markerOptions);
                    marker.setTag(scooterReservada);

                    markers.add(marker);

                    cambiarTextoSurfaceReserva(scooterReservada);
                }


                mMap.moveCamera((CameraUpdateFactory.newLatLng(pos)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 14), 1500, null);
            }
        });
    }

    private void cambiarTextoSurfaceReserva (Scooter scooterReservada) {
        if (scooterReservada==null)
            return;
        TextView matriculaText = surfaceReserva.findViewById(R.id.textoModelo);
        TextView bateriaText = surfaceReserva.findViewById(R.id.textoBateria);
        TextView distanciaText = surfaceReserva.findViewById(R.id.textoDistancia);
        TextView calleText = surfaceReserva.findViewById(R.id.textoCalle);

        int bateria = Math.round(scooterReservada.getBateria()*100);
        int km = Math.round(bateria/1.8f); // Un valor random
        bateriaText.setText(bateria+ "% (~"+ km +" km)");

        //TODO: Añadir tambien la versión del modelo
        //TODO: Cambiar la foto según que modelo sea
        matriculaText.setText(scooterReservada.getCodigo() + " - Scooter Modelo 1");

        // TODO: Poner la diferencia entre la scooter y tu
        distanciaText.setText("?m");

        calleText.setText(scooterReservada.getDireccion());

        for(Marker m : markers) {
            Scooter s = (Scooter) m.getTag();
            if (s.getNoSerie().equals(scooterReservada.getNoSerie()))
                continue;

            m.setVisible(false);
        }
    }

    //==============
    // Para evitar el código spaguettis, al cambiar de estado se pondrá todas las ventanas
    // solas para evitar de tener que hacerlo manualmente en cada estado.
    //==============

    private void cambiarEstado (EstadoAlquiler estado) {
        cambiarEstado(estado, 0);
    }

    private void cambiarEstado (EstadoAlquiler estado, int initTime) {
        scooterViewModel.setEstadoAlquiler(estado);

        cerrarTodoEstado();
        switch (estado) {
            case NADA:
                abrirEstadoNormal();
                break;
            case RESERVA:
                //TODO: Meter contenido en una constante
                abrirEstadoReserva(900-initTime);
                break;
            case ALQUILER:
                abrirEstadoAlquiler(initTime);
                break;
        }
    }

    private void abrirEstadoNormal () {
        botonActualizar.setVisibility(View.VISIBLE);

        for (Marker m : markers) {
            m.setVisible(true);
        }
        scooterViewModel.setScooterReservada(null);
    }

    private void abrirEstadoReserva (int tiempoRestante) {
        surface.setVisibility(View.INVISIBLE);
        surfaceReserva.setVisibility(View.VISIBLE);
        surfaceContador.setVisibility(View.VISIBLE);
        botonCancelarReserva.setEnabled(true);
        botonAlquiler.setEnabled(true);
        botonCancelarReserva.setVisibility(View.VISIBLE);
        botonAlquiler.setVisibility(View.VISIBLE);

        cambiarTextoSurfaceReserva(scooterViewModel.getScooterReservada());

        if (cronometro!=null)
            cronometro.finalizar();

        //TODO: Almacenar en Strings
        textViewTituloContador.setText("Tiempo restante:");
        cronometro = new Cronometro(textViewContador, tiempoRestante, new IOnTimeFinished() {
            @Override
            public void timeFinished() {
                // Si se acaba el tiempo se cancela la reserva
                peticionCancelarReserva();
            }
        });
        cronometro.ejecutar();
    }

    private void abrirEstadoAlquiler(int initTime) {
        surface.setVisibility(View.INVISIBLE);
        surfaceReserva.setVisibility(View.VISIBLE);
        surfaceContador.setVisibility(View.VISIBLE);
        botonFinAlquiler.setVisibility(View.VISIBLE);

        if (cronometro!=null)
            cronometro.finalizar();

        //TODO: Almacenar en constantes String
        textViewTituloContador.setText("Tiempo alquiler:");
        cronometro=new Cronometro(textViewContador, initTime);
        cronometro.ejecutar();
    }

    private void cerrarTodoEstado () {
        botonActualizar.setVisibility(View.GONE);
        botonFinAlquiler.setVisibility(View.GONE);
        botonCancelarReserva.setVisibility(View.GONE);
        botonAlquiler.setVisibility(View.GONE);
        surface.setVisibility(View.GONE);
        surfaceReserva.setVisibility(View.GONE);
        surfaceContador.setVisibility(View.GONE);
    }
}