package com.kidev.adrian.scooterapp.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kidev.adrian.scooterapp.R;
import com.kidev.adrian.scooterapp.entities.Bono;

import java.util.List;

public class BonoAdapter extends RecyclerView.Adapter<BonoAdapter.ViewHolder> {
    private List<Bono> bonos;
    private int layout;
    private Activity activity;
    private OnItemClickListener listener;

    public BonoAdapter(List<Bono> bonos, int layout, Activity activity, OnItemClickListener listener) {
        this.bonos = bonos;
        this.layout = layout;
        this.activity = activity;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BonoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(activity).inflate(layout,viewGroup, false);
        BonoAdapter.ViewHolder viewHolder= new BonoAdapter.ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BonoAdapter.ViewHolder viewHolder, int i) {
        final Bono bono = bonos.get(i);

        double precio = (double) Math.round(bono.getPrecio()*100)/100;

        viewHolder.textoNombre.setText(bono.getNombre());
        viewHolder.textoDescripcion.setText(bono.getDescripcion());
        if (viewHolder.textoMinutos!=null)
            viewHolder.textoMinutos.setText(bono.getMinutos());
        viewHolder.textoPrecio.setText(precio+"€");

        viewHolder.linear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                listener.onItemClick(v, bono);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bonos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout linear;
        private TextView textoNombre;
        private TextView textoDescripcion;
        private TextView textoMinutos;
        private TextView textoPrecio;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textoNombre= itemView.findViewById(R.id.tituloBono);
            textoDescripcion = itemView.findViewById(R.id.descripcionBono);
            //textoMinutos = itemView.findViewById(R.id.textViewMaxAcertadas);

            textoPrecio = itemView.findViewById(R.id.precioBono);

            linear = itemView.findViewById(R.id.linear);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(View vista, Bono bono);
    }
}