package com.mv.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Rohit Gujar on 05-12-2017.
 */

public class DownloadContent {
    @Expose
    @SerializedName("Name")
    String Name;
    @Expose
    @SerializedName("URL__c")
    String url;
    @Expose
    @SerializedName("FileType__c")
    String FileType;

    public String getFileType() {
        return FileType;
    }

    public void setFileType(String fileType) {
        FileType = fileType;
    }


    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
