package com.mv.Model;

/**
 * Created by user on 11/23/2018.
 */

public class ImageData {

    private int position;
    private String imagePath;
    private String imageName;
    private String imageUniqueId;

    public int getPosition() {
        return position;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
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
