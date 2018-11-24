package com.mv.Model;

import android.net.Uri;

/**
 * Created by user on 11/23/2018.
 */

public class ImageData {

    private int position;
    private Uri imageUri;
    private String imageId;
    private String imageUniqueId;

    public int getPosition() {
        return position;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageUniqueId() {
        return imageUniqueId;
    }

    public void setImageUniqueId(String imageUniqueId) {
        this.imageUniqueId = imageUniqueId;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
