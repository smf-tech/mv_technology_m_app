package com.mv.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mv.Utils.Constants;

/**
 * Created by Rohit Gujar on 13-09-2017.
 */
@Entity(tableName = Constants.TABLE_COMMUNITY)
public class Community {

    @ColumnInfo(name = "community_id")
    @SerializedName("community_id")
    @Expose
    private String Id;
    @ColumnInfo(name = "community_name")
    @SerializedName("community_name")
    @Expose
    private String Name;
    @ColumnInfo(name = "timestamp")
    @SerializedName("timestamp")
    @Expose
    private String Time;

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public int getUnique_Id() {
        return Unique_Id;
    }

    public void setUnique_Id(int unique_Id) {
        Unique_Id = unique_Id;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "unique_Id")
    private int Unique_Id;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }
}
