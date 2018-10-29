package com.mv.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.MediaController;
import android.widget.VideoView;

import com.mv.R;

public class VideoViewActivity extends Activity {

    // Declare variables
    private ProgressDialog pDialog;
    private VideoView videoview;

    // Insert your Video URL
    private String VideoURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the layout from video_main.xml
        setContentView(R.layout.videoview_main);
        if(getIntent().getExtras()!=null) {
            VideoURL = getIntent().getExtras().getString("URL");
        }
        // Find your VideoView in your video_main.xml layout
        videoview = findViewById(R.id.VideoView);
        // Execute StreamVideo AsyncTask
        // Create a progressbar
        pDialog = new ProgressDialog(VideoViewActivity.this);
        // Set progressbar title
        pDialog.setTitle("Video Streaming");
        // Set progressbar message
        pDialog.setMessage("Buffering...Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCanceledOnTouchOutside(false);
        // Show progressbar
        pDialog.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
            return false;
        });
        pDialog.show();

        try {
            // Start the MediaController
            MediaController mediacontroller = new MediaController(
                    VideoViewActivity.this);
            mediacontroller.setAnchorView(videoview);
            // Get the URL from String VideoURL
            Uri video = Uri.parse(VideoURL);
            videoview.setMediaController(mediacontroller);
            videoview.setVideoURI(video);

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        videoview.requestFocus();
        // Close the progress bar and play the video
        videoview.setOnPreparedListener(mp -> {
            pDialog.dismiss();
            videoview.start();
        });

    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }
}
