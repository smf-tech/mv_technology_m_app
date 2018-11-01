package com.mv.Activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mv.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class ScreenShotActivity extends AppCompatActivity {

    private TextView textView;
    private ImageView imageView;
    private Bitmap mbitmap;
    private Button captureScreenShot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_shot);


        textView = (TextView) findViewById(R.id.textView);
        textView.setText("Your ScreenShot Image:");

        captureScreenShot = (Button) findViewById(R.id.capture_screen_shot);
        imageView = (ImageView) findViewById(R.id.imageView);

    }

    public void screenShot(View view) {
        mbitmap = getBitmapOFRootView(captureScreenShot);
        imageView.setImageBitmap(mbitmap);
        createImage(mbitmap);
    }

    private Bitmap getBitmapOFRootView(View v) {
        View rootview = v.getRootView();
        rootview.setDrawingCacheEnabled(true);
        return rootview.getDrawingCache();
    }

    private void createImage(Bitmap bmp) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        File file = new File(Environment.getExternalStorageDirectory() +
                "/capturedscreenandroid.jpg");
        try {
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(bytes.toByteArray());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}