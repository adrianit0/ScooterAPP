package com.kidev.adrian.scooterapp.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kidev.adrian.scooterapp.R;
import com.kidev.adrian.scooterapp.activities.MenuActivity;
import com.kidev.adrian.scooterapp.adapter.BonoAdapter;
import com.kidev.adrian.scooterapp.entities.Bono;
import com.kidev.adrian.scooterapp.entities.Cliente;
import com.kidev.adrian.scooterapp.util.AndroidUtil;
import com.kidev.adrian.scooterapp.inteface.CallbackRespuesta;
import com.kidev.adrian.scooterapp.util.ConectorTCP;
import com.kidev.adrian.scooterapp.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class PackFragment extends Fragment {

    private BonoAdapter mAdapter;
    private ArrayList<Bono> bonos;
    private RecyclerView recyclerView;
    private TextView textoMinutos;

    private MenuActivity menuActivity;

    public PackFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pack, container, false);

        bonos = new ArrayList<>();

        recyclerView= rootView.findViewById(R.id.recyclerView);
        textoMinutos = rootView.findViewById(R.id.minutosActuales);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        menuActivity = (MenuActivity) getActivity();

        final Cliente cliente = menuActivity.getUsuario();


        ConectorTCP.getInstance().realizarConexion(getActivity(), "getBonos", null, new CallbackRespuesta() {
            @Override
            public void success(Map<String, String> contenido) {
                int length  = Integer.parseInt(contenido.get("length"));

                for (int i = 0; i < length; i++) {
                    Bono bono = new Bono();
                    bono.setId(Integer.parseInt(contenido.get("id["+i+"]")));
                    bono.setNombre(contenido.get("nombre["+i+"]"));
                    bono.setDescripcion(contenido.get("descripcion["+i+"]"));
                    bono.setPrecio(Double.parseDouble(contenido.get("precio["+i+"]")));
                    bono.setMinutos(Integer.parseInt(contenido.get("minutos["+i+"]")));

                    bonos.add(bono);
                }

                mAdapter = new BonoAdapter(bonos, R.layout.bono_row, getActivity(), new BonoAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View vista, final Bono bono) {
                        AndroidUtil.crearAcceptDialog(getActivity(), "Confirmar", "¿Quieres comprar " + bono.getMinutos() + " minutos de bonos por " + bono.getPrecio() + "€?",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    comprarMinutos(bono);
                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }
                        );
                    }
                });

                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void error(Map<String, String> contenido, Util.CODIGO codigoError) {
                AndroidUtil.crearErrorDialog(getActivity(), "No se ha podido descargar los bonos: " + contenido.get("error"));
            }
        });


        textoMinutos.setText(cliente.getMinutos()+"");

        return rootView;
    }

    private void comprarMinutos (final Bono bono) {
        final Cliente cliente = menuActivity.getUsuario();
        Map<String,String> parametros = new HashMap<>();
        parametros.put("idCliente", cliente.getId()+"");
        parametros.put("idBono", bono.getId()+"");

        final MenuActivity activity = (MenuActivity) getActivity();

        ConectorTCP.getInstance().realizarConexion(getActivity(),"aumentarBonos", parametros, new CallbackRespuesta() {
            @Override
            public void success(Map<String, String> contenido) {
                int totales = Integer.parseInt(contenido.get("minutosTotales"));
                cliente.setMinutos(totales);
                AndroidUtil.crearDialog(getActivity(), "Compra correcta", "La compra ha sido realizado satisfactoriamente", null);

                textoMinutos.setText(cliente.getMinutos()+"");

                activity.actualizarMinutos(cliente.getMinutos());
            }

            @Override
            public void error(Map<String, String> contenido, Util.CODIGO codigoError) {
                AndroidUtil.crearErrorDialog(getActivity(), "No se ha podido comprar el bono: " + contenido.get("error"));
            }
        });
    }
}
