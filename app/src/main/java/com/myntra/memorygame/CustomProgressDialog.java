package com.myntra.memorygame;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;



public class CustomProgressDialog extends ProgressDialog {

    private Activity activity;

    public CustomProgressDialog(Activity activity) {
        super(activity);
        this.activity = activity;
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setIndeterminate(true);
        setMessage(activity.getResources().getString(R.string.loading));
    }

    public CustomProgressDialog(Activity activity, String message) {
        super(activity);
        this.activity = activity;
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setIndeterminate(true);
        setMessage(message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void show() {
        if (activity == null) return;
        if (!activity.isFinishing()) {
            super.show();
        }
    }

    @Override
    public void dismiss() {
        if (activity != null & !activity.isFinishing()) {
            try {
                super.dismiss();
            }catch (IllegalArgumentException e){
                e.printStackTrace();
                return;
            }
        }
    }
}