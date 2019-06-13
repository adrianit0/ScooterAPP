package com.kidev.adrian.scooterapp.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;
import com.kidev.adrian.scooterapp.R;
import com.kidev.adrian.scooterapp.activities.MenuActivity;
import com.kidev.adrian.scooterapp.entities.Scooter;
import com.kidev.adrian.scooterapp.model.ScooterViewModel;

public class IncidenciaFragment extends Fragment {

    private Button parte1;
    private Button parte2;
    private Button parte3;
    private Button parte4;

    private MenuActivity menuActivity;

    public IncidenciaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_incidencia, container, false);

        menuActivity = (MenuActivity) getActivity();

        parte1 = root.findViewById(R.id.botonParte1);
        parte2 = root.findViewById(R.id.botonParte2);
        parte3 = root.findViewById(R.id.botonParte3);
        parte4 = root.findViewById(R.id.botonParte4);

        crearListener(parte1, 1);
        crearListener(parte2, 2);
        crearListener(parte3, 3);
        crearListener(parte4, 4);

        return root;
    }

    private void crearListener (Button boton, final int pos) {
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScooterViewModel viewModel = ViewModelProviders.of(getActivity()).get(ScooterViewModel.class);
                Scooter scooter = viewModel.getScooterReservada();
                Integer codigo = null;
                if (scooter!=null)
                    codigo=scooter.getCodigo();
                menuActivity.openParteIncidencia(pos, codigo);
            }
        });
    }
}
