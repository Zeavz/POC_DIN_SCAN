package com.codingsans.ionic.sensormanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Fragment;
import android.content.Context;

//Vision Stuff
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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//Vision Stuff

//Camera Stuff
import android.graphics.Point;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Button;
import android.view.SurfaceHolder;
import android.util.SparseArray;
//Camera Stuff

import org.json.JSONArray;
import org.json.JSONObject;

public class CameraScreenFrag extends Fragment{
   
    View viewer;
    String appResourcesPackage;
    SurfaceView cameraView;
    TextView textView;
    public String dinNumber = "";
    CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;
    boolean hide = true;
    boolean firstTime = true;
    String url = "https://health-products.canada.ca/api/drug/drugproduct/?lang=en&type=json&din=";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        appResourcesPackage = getActivity().getPackageName();  
        viewer = inflater.inflate(getResources().getIdentifier("camera_view_screen", "layout", appResourcesPackage), container, false);
        setUpCameraScreen();
        return viewer;
    }

    protected void apiCall(String id, Point[] points){

        String tempUrl = url + id.substring(4);
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getWindow().getContext());
        final Point[] pointsToSend = points;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, tempUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            try{
                                JSONArray objarr = new JSONArray(response.toString());
                                JSONObject obj = objarr.getJSONObject(0);
                                final String toOutputNum = obj.getString("drug_identification_number");
                                dinNumber = toOutputNum;
                                //showText(toOutputNum, pointsToSend[3]);
                                // returnIntent.putExtra("result", toOutput);
                                // returnIntent.putExtra("result2", obj.getString("drug_identification_number"));
                                // returnIntent.putExtra("result3", obj.getString("company_name"));
                                // returnIntent.putExtra("result4", obj.getString("descriptor"));
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

    private void showText(String dinNum, Point p1){
        final String toDisplay = dinNum;
        final Point pointToUse = p1;
        getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    textView.setText(toDisplay);
                    textView.setVisibility(View.VISIBLE);
                    textView.setX(pointToUse.x);
                    textView.setY(pointToUse.y);
                }
            });
    }

    private void setUpCameraScreen(){
        cameraView = (SurfaceView) viewer.findViewById(getResources().getIdentifier("surface_view", "id", appResourcesPackage));
        textView = (TextView) viewer.findViewById(getResources().getIdentifier("txtView", "id", appResourcesPackage)); 
        textView.setVisibility(View.INVISIBLE);
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getActivity().getWindow().getContext()).build();
        if (!textRecognizer.isOperational()) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                final android.widget.Toast toast = android.widget.Toast.makeText(
                  getActivity().getWindow().getContext(),
                  "not operational",
                  android.widget.Toast.LENGTH_LONG 
                    );
                    toast.show();
                }
            });
        } else {
            //if (firstTime){
            cameraSource = new CameraSource.Builder(getActivity().getWindow().getContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    try{
                        cameraSource.start(cameraView.getHolder());
                    }
                    catch(IOException e){
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                            final android.widget.Toast toast = android.widget.Toast.makeText(
                              getActivity().getWindow().getContext(),
                              "Could not start Camera",
                              android.widget.Toast.LENGTH_LONG 
                                );
                                toast.show();
                            }
                        });
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
        // }
        // else{
            

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if(items.size() != 0){
                        for(int i=0;i<items.size();++i){
                            TextBlock item = items.valueAt(i);
                            Pattern p = Pattern.compile("DIN.?\\d{8}");
                            Matcher m = p.matcher(item.getValue().toString());
                            if (m.matches()){
                                apiCall(item.getValue().toString(), item.getCornerPoints());
                            }
                        }
                    }
                }
            });
        }
    }
}