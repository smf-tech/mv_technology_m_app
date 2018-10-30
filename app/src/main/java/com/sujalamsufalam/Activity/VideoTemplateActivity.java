package com.sujalamsufalam.Activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sujalamsufalam.R;
import com.sujalamsufalam.Utils.Constants;
import com.sujalamsufalam.Utils.GetFilePathFromDevice;
import com.sujalamsufalam.Utils.Utills;
import com.sujalamsufalam.databinding.ActivityVideoTemplateBinding;

import java.io.File;
import java.text.SimpleDateFormat;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;


public class VideoTemplateActivity extends AppCompatActivity implements View.OnClickListener {


    private Uri FinalUri = null;
    private Uri outputUri = null;
    String imageFilePath;

    private ActivityVideoTemplateBinding binding;
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;

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
        setActionbar(getString(R.string.Video_template));

    }

    private void setActionbar(String Title) {
        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setOnClickListener(this);
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
        showVideoDialog();
    }

    public void onBtnSubmitClick() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    private void showVideoDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.text_choosevideo));
        String[] items = {getString(R.string.text_gallary),
                getString(R.string.text_camera)};

        dialog.setItems(items, (dialog1, which) -> {
            // TODO Auto-generated method stub
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
           /* Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Video/video.mp4";
            File imageFile = new File(imageFilePath);
            outputUri = Uri.fromFile(imageFile); // convert path to Uri
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFilePath);
            startActivityForResult(takeVideoIntent, Constants.CHOOSE_VIDEO_FROM_CAMERA);*/
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

            // create a file to save the video
            outputUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
            // set the video duration
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
            // set the image file name
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            // set the video image quality to high
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

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
        Bitmap bmThumbnail;
        if (requestCode == Constants.CHOOSE_VIDEO_FROM_CAMERA && resultCode == RESULT_OK) {
            String selectedImagePath = getPath(outputUri);
            bmThumbnail = ThumbnailUtils.createVideoThumbnail(outputUri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
            binding.addVideo.setImageBitmap(bmThumbnail);
        } else if (requestCode == Constants.CHOOSE_VIDEO_FROM_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            String selectedVideoFilePath = GetFilePathFromDevice.getPath(this, selectedImageUri);
            if (selectedVideoFilePath != null) {
                binding.addVideo.setImageBitmap(ThumbnailUtils.createVideoThumbnail(selectedVideoFilePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND));
            }
        }
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    /**
     * Create a file Uri for saving an image or video
     */
    private Uri getOutputMediaFileUri(int type) {

        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
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
        java.util.Date date = new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(date.getTime());

        File mediaFile;

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
