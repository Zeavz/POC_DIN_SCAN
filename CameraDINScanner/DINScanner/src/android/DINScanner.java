package cordova.plugin.DINScanner;

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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

 * This class echoes a string called from JavaScript.
 */
public class DINScanner extends CordovaPlugin {

    SurfaceView cameraView;
    TextView textView;
    CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;
    boolean hide = true;
    String url = "https://health-products.canada.ca/api/drug/drugproduct/?lang=en&type=json&din=";


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            if (cordova.hasPermission(permissions[0])) {
                return startCamera(args.getInt(0), args.getInt(1), args.getInt(2), args.getInt(3), args.getString(4), args.getBoolean(5), args.getBoolean(6), args.getBoolean(7), args.getString(8), args.getBoolean(9), args.getBoolean(10), args.getBoolean(11), callbackContext);
            }
            else {
                this.execCallback = callbackContext;
                this.execArgs = args;
                cordova.requestPermissions(this, CAM_REQ_CODE, permissions);
                return true;
            }
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        
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
}
