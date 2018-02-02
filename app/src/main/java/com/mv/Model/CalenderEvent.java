package com.mv.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mv.Utils.Constants;

/**
 * Created by nanostuffs on 05-12-2017.
 */
@Entity(tableName = Constants.TABLE_CALANDER)
public class CalenderEvent {

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
    String id;

    @ColumnInfo(name = "Description__c")
    @SerializedName("Description__c")
    @Expose
    String description;

    @ColumnInfo(name = "Date__c")
    @SerializedName("Date__c")
    @Expose
    String date;


    @ColumnInfo(name = "MV_User1__c")
    @SerializedName("MV_User1__c")
    @Expose
    String MV_User1__c;

    @ColumnInfo(name = "Title")
    @SerializedName("Title")
    @Expose
    String title;
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMV_User1__c() {
        return MV_User1__c;
    }

    public void setMV_User1__c(String MV_User1__c) {
        this.MV_User1__c = MV_User1__c;
    }


}
