package com.mv.Activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mv.R;
import com.mv.Utils.LocaleManager;

public class AboutUsActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        setActionbar(getString(R.string.about_us));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void setActionbar(String Title) {
        String str = Title;
        if (str.contains("\n")) {
            str = str.replace("\n", " ");
        }

        ((TextView) findViewById(R.id.toolbar_title)).setText(str);

        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        findViewById(R.id.img_list).setVisibility(View.GONE);
        findViewById(R.id.img_filter).setVisibility(View.GONE);
        findViewById(R.id.img_logout).setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
        }
    }
}
