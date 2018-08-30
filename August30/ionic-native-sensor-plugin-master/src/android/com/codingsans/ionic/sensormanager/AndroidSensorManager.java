package com.codingsans.ionic.sensormanager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaPlugin;
import android.content.pm.PackageManager;
import org.apache.cordova.PluginResult;
import android.Manifest;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.FrameLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.content.Context;
import android.view.ViewGroup;
import android.os.Handler;

import android.content.pm.PackageManager;

public class AndroidSensorManager extends CordovaPlugin implements CameraScreenFrag.OnFragBackClick {
    private CallbackContext callbackContext;
    private JSONObject data = new JSONObject();
    private CameraScreenFrag frag;
    private int containerViewId = 20;
    CallbackContext callbackContext2;
    String dinChecker = "";

    private static final String [] permissions = {
        Manifest.permission.CAMERA
      };

    public AndroidSensorManager(){
        super();
    }
    // @Override
    // public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    //     super.initialize(cordova, webView);

    //     mSensorManager = (SensorManager) cordova.getActivity().getSystemService(Context.SENSOR_SERVICE);
    //     accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    // }

    public void onBackPressed(){

        // cordova.getActivity().runOnUiThread(new Runnable() {
        //             public void run() {
        //             final android.widget.Toast toast = android.widget.Toast.makeText(
        //               cordova.getActivity().getWindow().getContext(),
        //               "Reached",
        //               android.widget.Toast.LENGTH_LONG 
        //                 );
        //                 toast.show();
        //             }
        //         });

        cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    ((ViewGroup)webView.getView()).bringToFront();
                    }
                });
                FragmentTransaction ft = cordova.getActivity().getFragmentManager().beginTransaction();
                frag.dinNumber = "";
                ft.remove(frag).commit();
                frag = null;
    }

    public void dinRecieved(String din){
        data = new JSONObject();
                try {
                  data.put("din", din);
                } catch(JSONException e) {}
                callbackContext2.success(this.data);
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    ((ViewGroup)webView.getView()).bringToFront();
                    }
                });
                FragmentTransaction ft = cordova.getActivity().getFragmentManager().beginTransaction();
                frag.dinNumber = "";
                ft.remove(frag).commit();
                frag = null;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for(int r:grantResults){
            if(r == PackageManager.PERMISSION_DENIED){
                cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                    final android.widget.Toast toast = android.widget.Toast.makeText(
                      cordova.getActivity().getWindow().getContext(),
                      "Something went wrong getting permissions",
                      android.widget.Toast.LENGTH_LONG 
                        );
                        toast.show();
                    }
                });
              return;
            }
          }
          if (requestCode == 0) {
            startScanner();
          }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("stop".equals(action)) {
            this.callbackContext2 = callbackContext;
            if (cordova.hasPermission(permissions[0])) {
                return startScanner();
              } else {
                cordova.requestPermissions(this, 0, permissions);
                return true;
              }
              //startScanner();
        }
        else if ("getDin".equals(action)){
            try{
              dinChecker = frag.dinNumber;
              }
              catch (Exception e2){
                frag = null;
                try {
                  data.put("msg", "closed");
                } catch(JSONException e) {}
                callbackContext.success(this.data);
                return true;
              } 
              if (!dinChecker.equals("")){
                data = new JSONObject();
                try {
                  data.put("din", dinChecker);
                } catch(JSONException e) {}
                callbackContext.success(this.data);
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    ((ViewGroup)webView.getView()).bringToFront();
                    }
                });
                FragmentTransaction ft = cordova.getActivity().getFragmentManager().beginTransaction();
                frag.dinNumber = "";
                ft.remove(frag).commit();
                frag = null;
              }
              else{
                data = new JSONObject();
                try {
                  data.put("din", dinChecker);
                } catch(JSONException e) {}
                callbackContext.error(this.data);
              }
              return true;
        } 
        else if ("close".equals(action)){
            // if (frag != null){
            // cordova.getActivity().runOnUiThread(new Runnable() {
            //         @Override
            //         public void run() {
            //         ((ViewGroup)webView.getView()).bringToFront();
            //         }
            //     });
            //     FragmentTransaction ft = cordova.getActivity().getFragmentManager().beginTransaction();
            //     frag.dinNumber = "";
            //     ft.remove(frag).commit();
            //     frag = null;
            // }
            return true;
        }
        return false;  // Returning false results in a "MethodNotFound" error.
    }

    public void closeFrag(){
        cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    ((ViewGroup)webView.getView()).bringToFront();
                    }
                });
                FragmentTransaction ft = cordova.getActivity().getFragmentManager().beginTransaction();
                frag.dinNumber = "";
                ft.remove(frag).commit();
                frag = null;
    }

    private boolean startScanner(){
        if (frag == null){
        try{
            frag = new CameraScreenFrag();
        frag.setListener(this);
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                
        
        frag.dinNumber = "";
        FrameLayout containerView = (FrameLayout)cordova.getActivity().findViewById(containerViewId);
        if(containerView == null){
            containerView = new FrameLayout(cordova.getActivity().getApplicationContext());
            containerView.setId(containerViewId);
            FrameLayout.LayoutParams containerLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            cordova.getActivity().addContentView(containerView, containerLayoutParams);
        }
        containerView.bringToFront();
        FragmentManager fragmentManager = cordova.getActivity().getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(containerView.getId(), frag);
        fragmentTransaction.commit();
                }
            });
    }
    catch(Exception e){
        try {
            data.put("errorMessage", e.toString());
        } catch(JSONException e1) {}
        callbackContext2.error(this.data);
    }
}
        return true;
    }
}
