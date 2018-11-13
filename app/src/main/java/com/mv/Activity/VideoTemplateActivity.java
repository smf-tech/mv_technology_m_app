package com.mv.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.GetFilePathFromDevice;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityVideoTemplateBinding;

import java.io.File;
import java.text.SimpleDateFormat;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class VideoTemplateActivity extends AppCompatActivity implements View.OnClickListener {

    private Uri outputUri = null;
    private ActivityVideoTemplateBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_template);
        binding.setActivity(this);

        initViews();

        Utills.makedirs(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Video");
        Utills.makedirs(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image");
    }

    private void initViews() {
        setActionbar();
    }

    private void setActionbar() {
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(getString(R.string.Video_template));

        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
        }
    }

    public void onAddVideoClick() {
        if (!Utills.isMediaPermissionGranted(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO}, Constants.MEDIA_PERMISSION_REQUEST);
            }
        } else {
            showVideoDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.MEDIA_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showVideoDialog();
                }
                break;
        }
    }

    public void onBtnSubmitClick() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    private void showVideoDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.text_choosevideo));
        String[] items = {getString(R.string.text_gallary), getString(R.string.text_camera)};

        dialog.setItems(items, (dialog1, which) -> {
            switch (which) {
                case 0:
                    chooseVideoFromGallery();
                    break;

                case 1:
                    takeVideoFromCamera();
                    break;
            }
        });

        dialog.show();
    }

    private void chooseVideoFromGallery() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), Constants.CHOOSE_VIDEO_FROM_GALLERY);
    }

    private void takeVideoFromCamera() {
        try {
            // create a file to save the video
            outputUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

            // set the video duration
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // start the Video Capture Intent
            startActivityForResult(intent, Constants.CHOOSE_VIDEO_FROM_CAMERA);
        } catch (ActivityNotFoundException anfe) {
            String errorMessage = "Whoops - your device doesn't support capturing images!";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.CHOOSE_VIDEO_FROM_CAMERA && resultCode == RESULT_OK) {
            Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(outputUri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
            binding.addVideo.setImageBitmap(bmThumbnail);
        } else if (requestCode == Constants.CHOOSE_VIDEO_FROM_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            String selectedVideoFilePath = GetFilePathFromDevice.getPath(this, selectedImageUri);
            if (selectedVideoFilePath != null) {
                binding.addVideo.setImageBitmap(ThumbnailUtils.createVideoThumbnail(
                        selectedVideoFilePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND));
            }
        }
    }

    /**
     * Create a file Uri for saving an image or video
     */
    private Uri getOutputMediaFileUri(int type) {
        // return Uri.fromFile(getOutputMediaFile(type));
        File videoFile = getOutputMediaFile(type);
        return FileProvider.getUriForFile(getApplicationContext(),
                getPackageName() + ".fileprovider", videoFile);
    }

    /**
     * Create a File for saving an image or video
     */
    @SuppressLint("SimpleDateFormat")
    private File getOutputMediaFile(int type) {
        // Check that the SDCard is mounted
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Video");

        // Create the storage directory(MyCameraVideo) if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Toast.makeText(VideoTemplateActivity.this, "Failed to create directory MyCameraVideo.",
                        Toast.LENGTH_LONG).show();

                Log.d("MyCameraVideo", "Failed to create directory MyCameraVideo.");
                return null;
            }
        }

        // Create a media file name
        // For unique file name appending current timeStamp with file name
        File mediaFile;
        java.util.Date date = new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date.getTime());

        if (type == MEDIA_TYPE_VIDEO) {
            // For unique video file name appending current timeStamp with file name
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }
}
