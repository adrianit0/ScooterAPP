package com.kidev.adrian.scooterapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kidev.adrian.scooterapp.R;
import com.kidev.adrian.scooterapp.activities.MenuActivity;
import com.kidev.adrian.scooterapp.entities.Cliente;

public class UserFragment extends Fragment {

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_user, container, false);

        MenuActivity menu = (MenuActivity) getActivity();

        final Cliente cliente = menu.getUsuario();

        TextView textoNombre = root.findViewById(R.id.textoNombre);
        TextView textoApellido1 = root.findViewById(R.id.textoApellido1);
        TextView textoApellido2 = root.findViewById(R.id.textoApellido2);
        TextView textoNick = root.findViewById(R.id.textoNick);
        TextView textoEmail = root.findViewById(R.id.textoEmail);
        TextView textoCreacion = root.findViewById(R.id.textoFechaCreacion);

        textoNombre.setText(cliente.getNombre());
        textoApellido1.setText(cliente.getApellido1());
        textoApellido2.setText(cliente.getApellido2().equals("null") ? "" : cliente.getApellido2());
        textoNick.setText(cliente.getNick());
        textoEmail.setText(cliente.getEmail());
        textoCreacion.setText(cliente.getFechaCreacion());

        return root;
    }
}
