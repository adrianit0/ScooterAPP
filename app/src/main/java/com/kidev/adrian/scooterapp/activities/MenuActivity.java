package com.kidev.adrian.scooterapp.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.kidev.adrian.scooterapp.R;
import com.kidev.adrian.scooterapp.entities.Cliente;
import com.kidev.adrian.scooterapp.fragments.CameraFragment;
import com.kidev.adrian.scooterapp.fragments.HelpFragment;
import com.kidev.adrian.scooterapp.fragments.IncidenciaFragment;
import com.kidev.adrian.scooterapp.fragments.MapFragment;
import com.kidev.adrian.scooterapp.fragments.PackFragment;
import com.kidev.adrian.scooterapp.fragments.UserFragment;
import com.kidev.adrian.scooterapp.inteface.IOnRequestPermission;
import com.kidev.adrian.scooterapp.util.ConectorTCP;

import java.sql.Date;

public class MenuActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener {

    private Cliente usuario;
    private IOnRequestPermission mCallback;

    private DrawerLayout drawer;
    private NavigationView navigationView;

    // Fragments:
    private MapFragment mapFragment;
    private UserFragment userFragment;
    private IncidenciaFragment incidenciaFragment;
    private HelpFragment helpFragment;
    private PackFragment packFragment;
    private CameraFragment cameraFragment;

    private String lastTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        String token = i.getStringExtra("token");

        mapFragment = new MapFragment();
        userFragment = new UserFragment();
        incidenciaFragment = new IncidenciaFragment();
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

        ConectorTCP conector = ConectorTCP.getInstance();

        conector.setNick(usuario.getNick());
        conector.setToken(token);

        // TODO: Borrar botón flotante
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

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
        mostrarFragment(R.id.contenedor, mapFragment, "mapa", false);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        // No dejar volver al anterior activity
        /*else {
            super.onBackPressed();
        } */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_alquiler) {
            mostrarFragment(R.id.contenedor, mapFragment, "mapa", false);
        } else if (id == R.id.nav_perfil) {
            mostrarFragment(R.id.contenedor, userFragment, "user", false);
        } else if (id == R.id.nav_incidencia) {
            mostrarFragment(R.id.contenedor, incidenciaFragment, "incidencia", false);
        } else if (id == R.id.nav_help) {
            mostrarFragment(R.id.contenedor, helpFragment, "help", false);
        } else if (id == R.id.nav_bonos) {
            mostrarFragment(R.id.contenedor, packFragment, "pack", false);
        } else if (id == R.id.nav_share) {
            mostrarFragment(R.id.contenedor, cameraFragment, "camera", false);
        } else if (id == R.id.nav_disconnect) {

        }

        drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

        if ( addToBackStack ) {
            transaction.addToBackStack( tag );
        }

        transaction.commit();
        lastTag = tag;
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