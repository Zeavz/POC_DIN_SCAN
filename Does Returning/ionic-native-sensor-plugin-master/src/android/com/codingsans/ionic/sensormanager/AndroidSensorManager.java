package com.codingsans.ionic.sensormanager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import android.Manifest;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.FrameLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.hardware.Sensor;
import android.view.ViewGroup;
import android.os.Handler;

import android.content.pm.PackageManager;

public class AndroidSensorManager extends CordovaPlugin {
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private CallbackContext callbackContext;
    private JSONObject data = new JSONObject();
    private CameraScreenFrag frag;
    private int containerViewId = 20;
    CallbackContext callbackContext2;
    FragmentTransaction fragmentTransaction;

    private static final String [] permissions = {
        Manifest.permission.CAMERA
      };


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        mSensorManager = (SensorManager) cordova.getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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
            startScanner(callbackContext);
          }
    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(listener);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("start".equals(action)) {
            mSensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        } else if ("stop".equals(action)) {
            if (cordova.hasPermission(permissions[0])) {
                return startScanner(callbackContext);
              } else {
                this.callbackContext2 = callbackContext;
                cordova.requestPermissions(this, 0, permissions);
                return true;
              }
              //startScanner();
        }
        else if ("getDin".equals(action)){
            data = new JSONObject();
              try {
                  data.put("din", frag.dinNumber);
              } catch(JSONException e) {}
              callbackContext.success(this.data);
              if (!frag.dinNumber.equals("12345678")){
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    ((ViewGroup)webView.getView()).bringToFront();
                    }
                });
                fragmentTransaction.remove(frag).commit();
                frag = null;
              }
              return true;
        } 
        else if ("close".equals(action)){
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                ((ViewGroup)webView.getView()).bringToFront();
                }
            });
            fragmentTransaction.remove(frag).commit();
            frag = null;
            return true;
        }
        else if ("getCurrent".equals(action)) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, this.data);
            callbackContext.sendPluginResult(result);
            return true;
        }
        return false;  // Returning false results in a "MethodNotFound" error.
    }

    private SensorEventListener listener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
          if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
              data = new JSONObject();
              try {
                  data.put("x", event.values[0]);
                  data.put("y", event.values[1]);
                  data.put("z", event.values[2]);
              } catch(JSONException e) {}
          }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // unused
        }
    };

    private boolean startScanner(CallbackContext callbackContext){
        frag = new CameraScreenFrag();

        FrameLayout containerView = (FrameLayout)cordova.getActivity().findViewById(containerViewId);
        if(containerView == null){
            containerView = new FrameLayout(cordova.getActivity().getApplicationContext());
            containerView.setId(containerViewId);
            FrameLayout.LayoutParams containerLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            cordova.getActivity().addContentView(containerView, containerLayoutParams);
        }

        FragmentManager fragmentManager = cordova.getActivity().getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(containerView.getId(), frag);
        fragmentTransaction.commit();
        data = new JSONObject();
              try {
                  data.put("din", frag.dinNumber);
              } catch(JSONException e) {}
        callbackContext.success(this.data);
        return true;
    }
}
