package com.kidev.adrian.scooterapp.util;

import android.os.CountDownTimer;

import com.kidev.adrian.scooterapp.inteface.IOnSecondTick;

public abstract class CountUpTimer extends CountDownTimer {
    private static final long INTERVAL_MS = 1000;
    private final long duration;
    private final int initialTime;
    private final IOnSecondTick onSecondTick;

    protected CountUpTimer(long durationMs, int initialTime, IOnSecondTick onSecondTick) {
        super(durationMs, INTERVAL_MS);
        this.duration = durationMs;
        this.initialTime=initialTime;
        this.onSecondTick = onSecondTick;
    }

    public abstract void onTick(int second);

    @Override
    public void onTick(long msUntilFinished) {
        int second = initialTime + (int) ((duration - msUntilFinished) / 1000);
        if (second<0)
            second=0;
        onTick(second);

        if (onSecondTick!=null)
            onSecondTick.onSecondTick();
    }

    @Override
    public void onFinish() {
        onTick(duration / 1000);
    }
}