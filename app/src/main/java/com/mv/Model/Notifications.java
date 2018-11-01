package com.mv.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mv.Utils.Constants;

/**
 * Created by user on 7/25/2018.
 */

@Entity(tableName = Constants.TABLE_NOTIFICATION)
public class Notifications {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "unique_Id")
    private int UniqueId;
    @ColumnInfo(name = "Id")
    @SerializedName("Id")
    @Expose
    private String Id;
    @ColumnInfo(name = "Title")
    @SerializedName("Title")
    @Expose
    private String Title;
    @ColumnInfo(name = "Description")
    @SerializedName("Description")
    @Expose
    private String Description;
    @ColumnInfo(name = "Status")
    @SerializedName("Status")
    @Expose
    private String Status;


    public int getUniqueId() {
        return UniqueId;
    }

    public void setUniqueId(int uniqueId) {
        UniqueId = uniqueId;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
