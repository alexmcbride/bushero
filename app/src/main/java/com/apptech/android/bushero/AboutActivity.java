package com.apptech.android.bushero;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void onClickButtonBack(View view) {
        finish();
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, AboutActivity.class);
    }
}
