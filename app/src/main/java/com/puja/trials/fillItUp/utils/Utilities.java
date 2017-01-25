package com.puja.trials.fillItUp.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by puja on 22/01/17.
 */

public class Utilities {

    Context context;

    public Utilities(Context context){
        this.context = context;
    }

    public void longToast(String text){
        Toast.makeText(context, text , Toast.LENGTH_LONG).show();
    }

    public void shortToast(String text){
        Toast.makeText(context, text , Toast.LENGTH_SHORT).show();
    }
}
