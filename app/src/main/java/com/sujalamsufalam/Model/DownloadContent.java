package com.sujalamsufalam.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sujalamsufalam.Utils.Constants;

/**
 * Created by Rohit Gujar on 05-12-2017.
 */
@Entity(tableName = Constants.TABLE_DOWNLOAD_CONTENT)
public class DownloadContent {


    public int getUnique_Id() {
        return Unique_Id;
    }

    public void setUnique_Id(int unique_Id) {
        Unique_Id = unique_Id;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "unique_Id")
    private int Unique_Id;

    @ColumnInfo(name = "Id")
    @SerializedName("Id")
    @Expose
    private String Id;

    @ColumnInfo(name = "Section")
    @SerializedName("Section__c")
    @Expose
    private String Section;

    @ColumnInfo(name = "Lang")
    @SerializedName("Lang__c")
    @Expose
    private String Lang;

    @ColumnInfo(name = "Name")
    @Expose
    @SerializedName("Name")
    private String Name;

    @ColumnInfo(name = "URL__c")
    @Expose
    @SerializedName("URL__c")
    private String url;

    @ColumnInfo(name = "FileType__c")
    @Expose
    @SerializedName("FileType__c")
    private String FileType;

    public String getLang() {
        return Lang;
    }

    public void setLang(String Lang) {
        this.Lang = Lang;
    }


    public String getSection() {
        return Section;
    }

    public void setSection(String Section) {
        this.Section = Section;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }


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
