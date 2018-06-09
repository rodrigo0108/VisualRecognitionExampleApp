package com.ramos.ibmejemploapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ibm.watson.developer_cloud.android.library.camera.CameraHelper;
import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.Classification;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassResult;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifierResult;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyOptions;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private VisualRecognition service;
    private CameraHelper helper;
    private ProgressDialog progressDialog;
    private CoordinatorLayout coordinatorLayout;
    private ImageView foto;
    private TextView resultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        foto = findViewById(R.id.preview);
        resultado  = findViewById(R.id.detected_objects);

        service = new VisualRecognition(getString(R.string.version));
        service.setEndPoint("https://gateway.watsonplatform.net/visual-recognition/api");

        IamOptions options = new IamOptions.Builder().apiKey(getString(R.string.api_key)).build();
        service.setIamCredentials(options);

        // Initialize camera helper
        helper = new CameraHelper(this);

    }
    private void mostrarSnackbar(){
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Hay algunos problemas, verifica la conexión a Internet.", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void takePicture(View view) {
        //showDialog();
        if(isOnline()){
            helper.dispatchTakePictureIntent();
        }else{
            mostrarSnackbar();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
try {
    progressDialog = ProgressDialog.show(this, "Cargando", "El registro demomará unos segundos...", true);
    //you usually don't want the user to stop the current process, and this will make sure of that

    if (requestCode == CameraHelper.REQUEST_IMAGE_CAPTURE) {
        final Bitmap photo = helper.getBitmap(resultCode);
        final File photoFile = helper.getFile(resultCode);
        //Por que
        Log.e("MainActivity", photoFile + "");

        foto = findViewById(R.id.preview);
        foto.setImageBitmap(photo);
        if (photoFile == null)
            return;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ClassifyOptions classifyOptions = null;
                    classifyOptions = new ClassifyOptions.Builder()
                            .imagesFile(photoFile)
                            .imagesFilename(photoFile.getName())
                            .threshold((float) 0.6)
                            .acceptLanguage("es")
                            .owners(Arrays.asList("me"))
                            .classifierIds(Arrays.asList("Alimento_396195486"))
                            .build();


                    final ClassifiedImages result = service.classify(classifyOptions).execute();

                    Log.e("MainActivty", result.toString());

                    ClassifierResult classifier = result.getImages().get(0).getClassifiers().get(0);
                    final List<String> alimentos_detectados = new ArrayList<String>();

                    //final StringBuffer output = new StringBuffer();
                    for (ClassResult object : classifier.getClasses()) {
                        if (object.getScore() > 0.7f)
                            alimentos_detectados.add(object.getClassName());
                    }

                    Log.e("MainActivty", alimentos_detectados.toString());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (alimentos_detectados.isEmpty()) {
                                resultado.setText("El elemento detectado no es un alimento");
                                progressDialog.dismiss();
                            } else {
                                resultado.setText("El alimento es: " + alimentos_detectados.get(0));
                                progressDialog.dismiss();
                            }
                        }
                    });

                } catch (Throwable t){
                    t.printStackTrace();
                    Log.e("MainActivty", t.getMessage(), t);
                }finally {
                    progressDialog.dismiss();
                }

            }
        });
    }
    }catch (Throwable e){
    progressDialog.dismiss();
    mostrarSnackbar();
    Log.e("MainActivty", e.getMessage(), e);
}
/*finally {
    progressDialog.dismiss();
}*/
}
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
