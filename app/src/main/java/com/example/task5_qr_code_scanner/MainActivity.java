package com.example.task5_qr_code_scanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.qrcode.encoder.QRCode;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    SurfaceView camera_view;
    private static final int CAMERA_CODE=100;

    private CameraSource cameraSource;
    TextView qr_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        camera_view=findViewById(R.id.camera_view);
        qr_content=findViewById(R.id.qr_content);

        //ask permission from user
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},CAMERA_CODE);
        }
        else{
            initalizeQRCodeScanner();
        }
    }



    private void initalizeQRCodeScanner() {
        //to initialize barcode
        BarcodeDetector barcodeDetector=new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource=new CameraSource.Builder(this,barcodeDetector)
                .setAutoFocusEnabled(true)
                .build();

        camera_view.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                if(ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                    return;
                }
                try {
                    cameraSource.start(camera_view.getHolder()); //bulb cannot start without holder it is like that we need surfaceview jena thi camera ena pr chalu thse
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                        cameraSource.stop();
            }
        });
        //to process the barcode
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
                    final SparseArray<Barcode> qrcode=detections.getDetectedItems();
                    if(qrcode.size()!=0){
                        String qrContent=qrcode.valueAt(0).displayValue;
                        runOnUiThread(() -> {
                            qr_content.setText(qrContent);
                            if(qrContent.startsWith("http://")|| (qrContent.startsWith("https://"))){
                                Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(qrContent));
                                startActivity(intent);
                            }else if(qrContent.startsWith("tel:")){
                                Intent intent=new Intent(Intent.ACTION_DIAL,Uri.parse(qrContent));
//                                intent.setData(Uri.parse(qrContent));
                                startActivity(intent);
                            }
                            else if(qrContent.startsWith("upi://pay")){
                                Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(qrContent));
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(MainActivity.this, "Scanned QR code : "+qrContent, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==CAMERA_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                initalizeQRCodeScanner();
            }
            else{
                Toast.makeText(this, "Camera is needed to use this app", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraSource!=null){
            cameraSource.release();
        }
    }


}


