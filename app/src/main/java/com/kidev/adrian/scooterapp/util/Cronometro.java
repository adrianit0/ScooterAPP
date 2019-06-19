package com.kidev.adrian.scooterapp.util;

import android.os.CountDownTimer;
import android.widget.TextView;

import com.kidev.adrian.scooterapp.inteface.IOnSecondTick;
import com.kidev.adrian.scooterapp.inteface.IOnTimeFinished;


public class Cronometro{

    private TextView textView;
    private int segundos;
    private boolean decreciendo;
    private IOnTimeFinished callback;
    private boolean seguir;

    private CountDownTimer downTimer;
    private CountUpTimer upTimer;

    private Cronometro (TextView texto, int segundos, boolean decreciendo, IOnTimeFinished callback) {
        this.textView = texto;
        this.segundos = segundos;
        this.decreciendo = decreciendo;
        this.callback = callback;
        seguir = true;
    }

    public Cronometro (TextView texto, int segundos, IOnTimeFinished cb) {
        this (texto, segundos, true, cb);

        downTimer = new CountDownTimer(segundos*1000, 1000) {
            public void onTick(long millisUntilFinished) {
                int segHastaTerminar = Math.round(millisUntilFinished/1000);

                final int min = segHastaTerminar/60;
                int seg = segHastaTerminar%60;

                String segString = Integer.toString(seg).length()==2 ? seg+"" : "0"+seg;

                textView.setText(min+":"+segString);
            }

            public void onFinish() {
                textView.setText("0:00");

                if (callback!=null)
                    callback.timeFinished();
            }
        };
    }

    public Cronometro (TextView texto, int initSeconds, IOnSecondTick callback) {
        this(texto, initSeconds, false, null);

        upTimer = new CountUpTimer(Long.MAX_VALUE, initSeconds, callback) {
            @Override
            public void onTick(int second) {
                final int min = second/60;
                int seg = second%60;

                String segString = Integer.toString(seg).length()==2 ? seg+"" : "0"+seg;

                textView.setText(min+":"+segString);
            }
        };
    }

    public void ejecutar() {
        if (downTimer !=null)
            downTimer.start();
        else if (upTimer!=null)
            upTimer.start();
    }

    public void finalizar () {
        if (downTimer !=null)
            downTimer.cancel();
        else if (upTimer!=null)
            upTimer.cancel();
    }
}
