package com.example.jenis.camerascancontroller;

import android.Manifest;
import android.app.Activity;
import android.app.VoiceInteractor;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main2Activity extends AppCompatActivity {

    SurfaceView cameraView;
    TextView textView;
    CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;
    boolean hide = true;
    String url = "https://health-products.canada.ca/api/drug/drugproduct/?lang=en&type=json&din=";

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RequestCameraPermissionID:{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    try{
                        cameraSource.start(cameraView.getHolder());
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    protected void apiCall(String id){

        String tempUrl = url + id.substring(4);
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, tempUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            Intent returnIntent = new Intent();

                            try{
                                JSONArray objarr = new JSONArray(response.toString());
                                JSONObject obj = objarr.getJSONObject(0);
                                String toOutput = obj.getString("brand_name");
                                returnIntent.putExtra("result", toOutput);
                                returnIntent.putExtra("result2", obj.getString("drug_identification_number"));
                                returnIntent.putExtra("result3", obj.getString("company_name"));
                                returnIntent.putExtra("result4", obj.getString("descriptor"));
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                            }
                            catch (Throwable t){
                            }

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        requestQueue.add(stringRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        cameraView = (SurfaceView)findViewById(R.id.surface_view);
        textView = (TextView)findViewById(R.id.txtView);
        textView.setVisibility(View.INVISIBLE);
        Button button = (Button)findViewById(R.id.btnHide);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hide) {
                    hide = false;
                    textView.setVisibility(View.VISIBLE);
                }
                 else {
                    hide = true;
                    textView.setVisibility(View.INVISIBLE);
                }
            }
        });



        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
            Log.w("Main2Activity", "Detector dependencies are not yet available");
        } else {
            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(Main2Activity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    RequestCameraPermissionID);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if(items.size() != 0){
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for(int i=0;i<items.size();++i){
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                    Pattern p = Pattern.compile("DIN\\s\\d{8}");
                                    Matcher m = p.matcher(item.getValue().toString());
                                    if (m.matches()){
                                        Context context = getApplicationContext();
                                        Toast toast = Toast.makeText(context, "Found DIN!" + item.getValue().toString(), Toast.LENGTH_LONG);
                                        toast.show();
                                        apiCall(item.getValue().toString());









//                                        Context context = getApplicationContext();
//                                        Toast toast = Toast.makeText(context, "Found DIN!" + item.getValue().toString(), Toast.LENGTH_LONG);
//                                        toast.show();
//                                        release();
                                    }

//                                    Pattern x = Pattern.compile("\\s\\d{8}\\s\\d");
//                                    Matcher z = x.matcher(item.getValue().toString());
//                                    if (z.matches()){
//
//                                        Intent returnIntent = new Intent();
//                                        returnIntent.putExtra("result",item.getValue().toString());
//                                        setResult(Activity.RESULT_OK,returnIntent);
//                                        finish();
//
////                                        Context context = getApplicationContext();
////                                        Toast toast = Toast.makeText(context, "Found DIN!" + item.getValue().toString(), Toast.LENGTH_LONG);
////                                        toast.show();
////                                        release();
//                                    }
                                }
                                textView.setText(stringBuilder.toString());
                            }
                        });
                    }
                }
            });
        }
    }
}
