package com.mv.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mv.Utils.Constants;

/**
 * Created by nanostuffs on 30-10-2017.
 */
@Entity(tableName = Constants.TABLE_LOCATION)
public class LocationModel {
    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    @PrimaryKey()
    @ColumnInfo(name = "Id")
    @SerializedName("Id")
    private String Id;

    @ColumnInfo(name = "Village")
    @SerializedName("Village")
    @Expose
    private String village;
    @ColumnInfo(name = "Taluka")
    @SerializedName("Taluka")
    @Expose
    private String taluka;
    @ColumnInfo(name = "State")
    @SerializedName("State")
    @Expose
    private String state;
    @ColumnInfo(name = "SchoolName")
    @SerializedName("SchoolName")
    @Expose
    private String schoolName;
    @ColumnInfo(name = "SchoolCode")
    @SerializedName("SchoolCode")
    @Expose
    private String schoolCode;
    @ColumnInfo(name = "District")
    @SerializedName("District")
    @Expose
    private String district;
    @ColumnInfo(name = "createdDate")
    @SerializedName("createdDate")
    @Expose
    private String createdDate;
    @ColumnInfo(name = "Cluster")
    @SerializedName("Cluster")
    @Expose
    private String cluster;

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getTaluka() {
        return taluka;
    }

    public void setTaluka(String taluka) {
        this.taluka = taluka;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }



}
