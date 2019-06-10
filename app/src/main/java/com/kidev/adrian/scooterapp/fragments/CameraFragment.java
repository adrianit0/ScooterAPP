package com.kidev.adrian.scooterapp.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.kidev.adrian.scooterapp.R;
import com.kidev.adrian.scooterapp.activities.MenuActivity;
import com.kidev.adrian.scooterapp.inteface.IOnRequestPermission;
import com.kidev.adrian.scooterapp.util.AndroidUtil;

import java.io.IOException;

import static com.google.android.gms.vision.CameraSource.CAMERA_FACING_BACK;

public class CameraFragment extends Fragment {

    private SurfaceView surfaceView;
    private CameraSource camera;
    private TextView textView;
    private BarcodeDetector barcodeDetector;


    public CameraFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_camera, container, false);

        surfaceView = root.findViewById(R.id.surfaceCamera);
        textView = root.findViewById(R.id.textview);

        // Para detectar el código Qr
        barcodeDetector = new BarcodeDetector.Builder(getActivity()).setBarcodeFormats(Barcode.QR_CODE).build();

        // Para mostrar la cámara
        camera = new CameraSource.Builder(getActivity(), barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setRequestedFps(20.0f)
                .setFacing(CAMERA_FACING_BACK)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //TODO: Cambiar el Request code
                ((MenuActivity) getActivity()).pedirPermiso(Manifest.permission.CAMERA, 10, new IOnRequestPermission() {
                    @Override
                    public void onPermissionAccepted(String permiso) {
                        try {
                            // Inicializamos la cámara
                            if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                AndroidUtil.crearDialog(getActivity(), "Error", "Se ha producido un error al inicializar la cámara: No tienes suficientes permisos.", null);
                                return;
                            }
                            // TODO: Eliminar
                            AndroidUtil.crearToast(getActivity(), "Aceptado");
                            camera.start(surfaceView.getHolder());
                        } catch (IOException e) {
                            AndroidUtil.crearDialog(getActivity(), "Error", "Se ha producido un error al inicializar la cámara: " + e.getMessage(), null);
                            Log.e("Exception: %s", e.getMessage());
                        }
                    }

                    @Override
                    public void onPermissionDenied(String permiso) {
                        AndroidUtil.crearDialog(getActivity(), "Permiso denegado", "No tienes permisos para acceder a la cámara...", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                    }
                });

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                camera.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();

                //Log.e("MAP QR", "Está recibiendo información");

                if (qrCodes.size()>0) {

                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            Vibrator vibrator = (Vibrator) getActivity().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            try {
                                vibrator.vibrate(500);
                            } catch (NullPointerException e ){
                                Log.e("Error vibracion MAP", "No se ha podido vibrar: " + e.getMessage());
                            }

                            textView.setText(qrCodes.valueAt(0).displayValue);

                        }
                    });
                }
            }
        });

        return root;
    }
}
