package com.kidev.adrian.scooterapp.activities;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.kidev.adrian.scooterapp.R;
import com.kidev.adrian.scooterapp.entities.Cliente;
import com.kidev.adrian.scooterapp.fragments.CameraFragment;
import com.kidev.adrian.scooterapp.fragments.HelpFragment;
import com.kidev.adrian.scooterapp.fragments.IncidenciaFragment;
import com.kidev.adrian.scooterapp.fragments.MapFragment;
import com.kidev.adrian.scooterapp.fragments.PackFragment;
import com.kidev.adrian.scooterapp.fragments.ParteIncidenciaFragment;
import com.kidev.adrian.scooterapp.fragments.UserFragment;
import com.kidev.adrian.scooterapp.inteface.CallbackRespuesta;
import com.kidev.adrian.scooterapp.inteface.IOnQrDetected;
import com.kidev.adrian.scooterapp.inteface.IOnRequestPermission;
import com.kidev.adrian.scooterapp.model.ScooterViewModel;
import com.kidev.adrian.scooterapp.util.AndroidUtil;
import com.kidev.adrian.scooterapp.util.ConectorTCP;
import com.kidev.adrian.scooterapp.util.Util;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Cliente usuario;
    private IOnRequestPermission mCallback;
    private ScooterViewModel scooterViewModel;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    // Fragments:
    private MapFragment mapFragment;
    private UserFragment userFragment;
    private IncidenciaFragment incidenciaFragment;
    private ParteIncidenciaFragment parteIncidenciaFragment;
    private HelpFragment helpFragment;
    private PackFragment packFragment;
    private CameraFragment cameraFragment;

    private String lastTag;
    private String preCameraTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        scooterViewModel = ViewModelProviders.of(this).get(ScooterViewModel.class);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        String token = i.getStringExtra("token");

        mapFragment = new MapFragment();
        userFragment = new UserFragment();
        incidenciaFragment = new IncidenciaFragment();
        parteIncidenciaFragment = new ParteIncidenciaFragment();
        helpFragment = new HelpFragment();
        packFragment = new PackFragment();
        cameraFragment = new CameraFragment();

        usuario = new Cliente();
        usuario.setNick(i.getStringExtra("nick"));
        usuario.setNombre(i.getStringExtra("nombre"));
        usuario.setApellido1(i.getStringExtra("apellido1"));
        usuario.setApellido2(i.getStringExtra("apellido2"));
        usuario.setEmail(i.getStringExtra("email"));
        usuario.setId(Integer.parseInt(i.getStringExtra("id")));
        usuario.setMinutos(Integer.parseInt(i.getStringExtra("minutos")));
        usuario.setFechaCreacion(i.getStringExtra("fechaCreacion"));

        // Para el bundle del Fragment Map
        // Le envía el estado en el que se encuentra el alquiler.
        Bundle mapBundle = new Bundle();
        int estadoAlquiler = Integer.parseInt(i.getStringExtra("state"));
        mapBundle.putInt("state", estadoAlquiler);
        if (estadoAlquiler>0) {
            long time = Long.parseLong(i.getStringExtra("time"));
            Integer idScooter = Integer.parseInt(i.getStringExtra("scooterID" ));

            mapBundle.putLong("time", time);
            mapBundle.putInt("scooterID", idScooter.intValue());
        }
        mapFragment.setArguments(mapBundle);
        // Fin bundle map


        ConectorTCP conector = ConectorTCP.getInstance();

        conector.setNick(usuario.getNick());
        conector.setToken(token);

        // Deslizador
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation View
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        actualizarDatosNavigationView();

        // FragmentManager
        String tag = scooterViewModel.getActualFragment();
        if (tag==null) {
            mostrarFragment(R.id.contenedor, mapFragment, getApplicationContext().getString(R.string.fragment_map), false);
        } else {
            mostrarFragmentByTag(tag, false);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (lastTag.equals(getApplicationContext().getString(R.string.fragment_camera))) {
            closeCameraQr();
        } else if(lastTag.equals(getApplicationContext().getString(R.string.fragment_parte))) {
            mostrarFragmentByTag(getApplicationContext().getString(R.string.fragment_incidencia), false);
        }else {
            preguntarDesconectar();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_alquiler) {
            mostrarFragment(R.id.contenedor, mapFragment, getApplicationContext().getString(R.string.fragment_map), false);
        } else if (id == R.id.nav_perfil) {
            mostrarFragment(R.id.contenedor, userFragment, getApplicationContext().getString(R.string.fragment_perfil), false);
        } else if (id == R.id.nav_incidencia) {
            mostrarFragment(R.id.contenedor, incidenciaFragment, getApplicationContext().getString(R.string.fragment_incidencia), false);
        //} else if (id == R.id.nav_help) {
        //    mostrarFragment(R.id.contenedor, helpFragment, getApplicationContext().getString(R.string.fragment_help), false);
        } else if (id == R.id.nav_bonos) {
            mostrarFragment(R.id.contenedor, packFragment, getApplicationContext().getString(R.string.fragment_pack), false);
        //} else if (id == R.id.nav_share) {
        //
        } else if (id == R.id.nav_disconnect) {
            preguntarDesconectar();
        }

        drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void preguntarDesconectar() {
        AndroidUtil.crearAcceptDialog(this, "Desconectar", "¿Quieres desconectarte del servidor?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                desconectar();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    private void desconectar () {
        final Activity activity = this;
        Map<String,String> parametros = new HashMap<>();
        ConectorTCP.getInstance().realizarConexion(this,"desconectar", parametros, new CallbackRespuesta() {
            @Override
            public void success(Map<String, String> contenido) {
                Intent intent = new Intent(getApplication(), MainActivity.class);
                // Eliminamos la inforación de entrada
                intent.putExtra("restart", "true");
                startActivity(intent);
            }

            @Override
            public void error(Map<String, String> contenido, Util.CODIGO codigoError) {
                AndroidUtil.crearToast(activity,"No se ha podido desconectar del servidor. " + contenido.get("error"));
            }
        });
    }

    public void openParteIncidencia (int codigoParte, Integer codigoScooter) {
        mostrarFragment(R.id.contenedor, parteIncidenciaFragment, getApplicationContext().getString(R.string.fragment_parte), true);
        parteIncidenciaFragment.configurarParte(codigoParte, codigoScooter);
    }

    // Abre la cámara Qr
    public void showCameraQr (IOnQrDetected callback) {
        preCameraTag = lastTag;
        mostrarFragment(R.id.contenedor, cameraFragment, getApplicationContext().getString(R.string.fragment_camera), true);
        cameraFragment.openCamera(callback);
    }

    // Cierra la cámara Qr
    public void closeCameraQr () {
        mostrarFragmentByTag(preCameraTag, false);
    }

    public IncidenciaFragment getIncidenciaFragment() {
        return incidenciaFragment;
    }

    public PackFragment getPackFragment() {
        return packFragment;
    }

    public void mostrarFragment(Fragment fragment, String tag, boolean addToBackStack) {
        mostrarFragment(R.id.contenedor, fragment, tag, addToBackStack);
    }

    private void mostrarFragment (int resId, Fragment fragment, String tag, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if ( lastTag != null ) {
            Fragment lastFragment = fragmentManager.findFragmentByTag( lastTag );
            if ( lastFragment != null ) {
                transaction.hide( lastFragment );
            }
        }

        if ( fragment.isAdded() ) {
            transaction.show( fragment );
        }
        else {
            transaction.add( resId, fragment, tag ).setBreadCrumbShortTitle( tag );
        }
        toolbar.setTitle(tag.substring(0,1).toUpperCase()+tag.substring(1));

        if ( addToBackStack ) {
            transaction.addToBackStack( tag );
        }

        transaction.commit();
        lastTag = tag;

        ViewModelProviders.of(this).get(ScooterViewModel.class).setActualFragment(tag);
    }

    public void mostrarFragmentByTag (String tag, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (lastTag != null) {
            Fragment lastFragment = fragmentManager.findFragmentByTag( lastTag );
            if ( lastFragment != null ) {
                transaction.hide( lastFragment );
            }
        }

        Fragment actualFragment = fragmentManager.findFragmentByTag(tag);

        if (actualFragment!=null) {
            transaction.show( actualFragment );

            toolbar.setTitle(tag.substring(0,1).toUpperCase()+tag.substring(1));

            if (addToBackStack){
                transaction.addToBackStack(tag);
            }

            transaction.commit();
            lastTag = tag;
        }

        ViewModelProviders.of(this).get(ScooterViewModel.class).setActualFragment(tag);
    }

    /**
     * Pide permiso al activity
     * */
    public void pedirPermiso (final String permiso, final int REQUEST_CODE, final IOnRequestPermission callback) {
        mCallback = callback;
        final Activity activity = this;
        if (ContextCompat.checkSelfPermission(activity, permiso) == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(activity, "You have already granted this permission!", Toast.LENGTH_SHORT).show();
            callback.onPermissionAccepted(permiso);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(activity.getApplicationContext())
                        .setTitle("Se necesita permiso")
                        .setMessage("Es requerido para el correcto funcionamiento de la aplicación")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity, new String[]{permiso}, REQUEST_CODE);
                            }
                        })
                        .setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();

            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permiso}, REQUEST_CODE);
            }
        }
    }

    public Cliente getUsuario() {
        return usuario;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mCallback==null)
            return;
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            mCallback.onPermissionAccepted(permissions[0]);
        } else {
            Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            mCallback.onPermissionDenied(permissions[0]);
        }
        // Despues de usar el callback lo eliminamos
        mCallback=null;
    }

    public void actualizarDatosNavigationView() {
        TextView textoNombre = navigationView.getHeaderView(0).findViewById(R.id.textoNombre);
        TextView textoEmail = navigationView.getHeaderView(0).findViewById(R.id.textoEmail);

        textoNombre.setText(usuario.getNombreCompleto());
        textoEmail.setText(usuario.getEmail());

        actualizarMinutos(usuario.getMinutos());
    }

    public void actualizarMinutos (int nMinutos) {
        usuario.setMinutos(nMinutos);

        TextView textoMinutos = navigationView.getHeaderView(0).findViewById(R.id.textoMinutos);
        if (nMinutos==0) {
            textoMinutos.setText("No tienes minutos de bonos");
        } else if (nMinutos==1) {
            textoMinutos.setText("Tienes " +nMinutos+ " minuto de bono");
        } else {
            textoMinutos.setText("Tienes " +nMinutos+ " minutos de bono");
        }

    }
}