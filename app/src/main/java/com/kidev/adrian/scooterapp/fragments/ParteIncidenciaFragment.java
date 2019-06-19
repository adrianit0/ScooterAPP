package com.kidev.adrian.scooterapp.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.kidev.adrian.scooterapp.R;
import com.kidev.adrian.scooterapp.activities.MenuActivity;
import com.kidev.adrian.scooterapp.inteface.CallbackRespuesta;
import com.kidev.adrian.scooterapp.inteface.IOnQrDetected;
import com.kidev.adrian.scooterapp.model.ScooterViewModel;
import com.kidev.adrian.scooterapp.util.AndroidUtil;
import com.kidev.adrian.scooterapp.util.ConectorTCP;
import com.kidev.adrian.scooterapp.util.Util;

import java.util.HashMap;
import java.util.Map;

public class ParteIncidenciaFragment extends Fragment {

    private TextView textoTitulo;
    private LinearLayout linearCodigo;
    private EditText editCodigo;
    private Button botonCodigo;
    private TextView textoCalle;
    private EditText editDescripcion;
    private Button botonEnviar;

    private int tipoIncidencia;
    private Integer codigoScooter;
    private LatLng clientePosicion;

    private MenuActivity menuActivity;
    private ScooterViewModel scooterViewModel;

    private boolean creado = false;

    public ParteIncidenciaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_incidencia_contenido, container, false);

        menuActivity = (MenuActivity) getActivity();
        scooterViewModel = ViewModelProviders.of(getActivity()).get(ScooterViewModel.class);

        textoTitulo = root.findViewById(R.id.tituloIncidencia);
        linearCodigo = root.findViewById(R.id.linearCodigo);
        editCodigo = root.findViewById(R.id.editTextCodigo);
        botonCodigo = root.findViewById(R.id.botonCodigo);
        textoCalle = root.findViewById(R.id.textoCalle);
        editDescripcion = root.findViewById(R.id.textoDescripcion);
        botonEnviar = root.findViewById(R.id.botonEnviar);

        botonEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarParte();
            }
        });

        botonCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuActivity.showCameraQr(new IOnQrDetected() {
                    @Override
                    public void onQrDetected(String message) {
                        String[] splitted = message.split("[:]");
                        if (splitted.length==2&&splitted[0].equals("SC")){
                            editCodigo.setText(splitted[1]);
                        } else {
                            AndroidUtil.crearDialog(getActivity(), "Error", "Este código Qr no es de una scooter", null);
                        }
                    }
                });
            }
        });

        // Para obtener la posición
        ViewModelProviders.of(getActivity()).get(ScooterViewModel.class).getTuPosicion().observe(getActivity(), new Observer<LatLng>() {
            @Override
            public void onChanged(@Nullable LatLng latLng) {
                clientePosicion = latLng;
                String direccion = AndroidUtil.getStreetName(getActivity(), clientePosicion.latitude, clientePosicion.longitude);
                textoCalle.setText(direccion);
            }
        });

        creado=true;
        // Si ya estuviera de antes lo vuelve a consigurar
        if (scooterViewModel.getCodigoScooter()!=null)
            configurarParte(scooterViewModel.getTipoIncidencia(), scooterViewModel.getCodigoScooter());
        else
            generarParte();

        return root;
    }

    private void generarParte() {
        scooterViewModel.setParteIncidencia(tipoIncidencia, codigoScooter);

        linearCodigo.setVisibility(codigoScooter!=null?View.GONE:View.VISIBLE);
        editCodigo.setText(codigoScooter!=null?codigoScooter.toString():"");

        String titulo = getTipoIncidencia(tipoIncidencia);
        textoTitulo.setText(titulo);

        if (clientePosicion!=null) {
            String direccion = AndroidUtil.getStreetName(getActivity(), clientePosicion.latitude, clientePosicion.longitude);
            textoCalle.setText(direccion);
        } else {
            textoCalle.setText("Calle desconocida");
        }

        editDescripcion.setText("");
    }

    public void configurarParte (int codigoParte, Integer codigoScooter) {
        this.tipoIncidencia = codigoParte;
        this.codigoScooter = codigoScooter;

        if (creado) {
            generarParte();
        }
    }

    public void configurarParteWithPosition (int codigoParte, LatLng clientePosicion) {
        configurarParte(codigoParte, null);
        this.clientePosicion = clientePosicion;
    }

    public void cerrarParte () {
        // Cerramos este fragment y volvemos al fragment de incidencias
        menuActivity.mostrarFragmentByTag("incidencia", false);
        scooterViewModel.removeParteIncidencia();
    }

    private void enviarParte () {
        String codigo = editCodigo.getText().toString();
        if (codigo==null||codigo.isEmpty()) {
            AndroidUtil.crearDialog(getActivity(), "Error", "El código no puede estar vacío", null);
            return;
        }

        Map<String,String> parametros = new HashMap<>();
        parametros.put("codigo", codigo);
        if (clientePosicion!=null) {
            parametros.put("lat", clientePosicion.latitude+"");
            parametros.put("lon", clientePosicion.longitude+"");
        }
        parametros.put("tipoIncidencia", tipoIncidencia+"");
        parametros.put("descripcion", editDescripcion.getText().toString());

        ConectorTCP.getInstance().realizarConexion(getActivity(),"enviarIncidencia", parametros, new CallbackRespuesta() {
            @Override
            public void success(Map<String, String> contenido) {
                AndroidUtil.crearDialog(getActivity(), "Confirmacion", "Se ha enviado el parte de incidencia correctamente", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cerrarParte();
                            }
                        });
                    }
                });
            }

            @Override
            public void error(Map<String, String> contenido, Util.CODIGO codigoError) {
                AndroidUtil.crearDialog(getActivity(), "Error", "Ha habido un error al enviar los datos. " + contenido.get("error"), null);
            }
        });

    }

    private String getTipoIncidencia (int codigo) {
        switch (codigo) {
            case 1:
                return "Scooter mal aparcada";
            case 2:
                return "Scooter dañada";
            case 3:
                return "Scooter no arranca";
            case 4:
            default:
                return "Otros problemas";
        }
    }
}
