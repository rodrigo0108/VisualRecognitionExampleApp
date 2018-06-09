package com.ramos.ibmejemploapp;

import android.app.ProgressDialog;
import android.content.Context;

public class AppHelper {


    public static ProgressDialog showProgress(Context context, ProgressDialog progressDialog){
       progressDialog =  ProgressDialog.show(context, "Cargando", "El registro demomará unos segundos...", true);
       progressDialog.setCancelable(false);
       return progressDialog;
    }
}
