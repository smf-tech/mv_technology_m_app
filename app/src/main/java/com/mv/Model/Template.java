package com.mv.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mv.Utils.Constants;

import java.io.Serializable;

/**
 * Created by Rohit Gujar on 13-09-2017.
 */
@Entity(tableName = Constants.TABLE_PROCESS)
public class Template implements Serializable{


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "unique_Id")
    private int Unique_Id;

    @ColumnInfo(name = "template_id")
    @SerializedName("template_id")
    @Expose
    private String Id;

    @ColumnInfo(name = "template_name")
    @SerializedName("template_name")
    @Expose
    private String Name;


    public Boolean getLocation() {
        return Location;
    }

    public void setLocation(Boolean location) {
        Location = location;
    }

    @ColumnInfo(name = "Location")
    private Boolean Location;
    @ColumnInfo(name = "LocationLevel")
    private String LocationLevel;


    public int getUnique_Id() {
        return Unique_Id;
    }

    public void setUnique_Id(int unique_Id) {
        Unique_Id = unique_Id;
    }



    public String getLocationLevel() {
        return LocationLevel;
    }

    public void setLocationLevel(String locationLevel) {
        LocationLevel = locationLevel;
    }


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

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    private String url;

    public String getMV_Process__c() {
        return MV_Process__c;
    }

    public void setMV_Process__c(String MV_Process__c) {
        this.MV_Process__c = MV_Process__c;
    }

    public String getMV_User__c() {
        return MV_User__c;
    }

    public void setMV_User__c(String MV_User__c) {
        this.MV_User__c = MV_User__c;
    }

    public String getP1F1__c() {
        return P1F1__c;
    }

    public void setP1F1__c(String p1F1__c) {
        P1F1__c = p1F1__c;
    }

    public String getP1F2__c() {
        return P1F2__c;
    }

    public void setP1F2__c(String p1F2__c) {
        P1F2__c = p1F2__c;
    }

    public String getP1F3__c() {
        return P1F3__c;
    }

    public void setP1F3__c(String p1F3__c) {
        P1F3__c = p1F3__c;
    }

    public String getP1F4__c() {
        return P1F4__c;
    }

    public void setP1F4__c(String p1F4__c) {
        P1F4__c = p1F4__c;
    }

    public String getP1F5__c() {
        return P1F5__c;
    }

    public void setP1F5__c(String p1F5__c) {
        P1F5__c = p1F5__c;
    }

    @SerializedName("MV_Process__c")
    @Expose
    private String MV_Process__c;

    @SerializedName("MV_User__c")
    @Expose
    private String MV_User__c;

    @SerializedName("P1F1__c")
    @Expose
    private String P1F1__c;


    @SerializedName("P1F2__c")
    @Expose
    private String P1F2__c;

    @SerializedName("P1F3__c")
    @Expose
    private String P1F3__c;

    @SerializedName("P1F4__c")
    @Expose
    private String P1F4__c;

    @SerializedName("P1F5__c")
    @Expose
    private String P1F5__c;

    private  Boolean Is_Editable__c;

    public Boolean getIs_Editable__c() {
        return Is_Editable__c;
    }

    public void setIs_Editable__c(Boolean is_Editable__c) {
        Is_Editable__c = is_Editable__c;
    }

    public Boolean getIs_Multiple_Entry_Allowed__c() {
        return Is_Multiple_Entry_Allowed__c;
    }

    public void setIs_Multiple_Entry_Allowed__c(Boolean is_Multiple_Entry_Allowed__c) {
        Is_Multiple_Entry_Allowed__c = is_Multiple_Entry_Allowed__c;
    }

    private  Boolean Is_Multiple_Entry_Allowed__c;
}
