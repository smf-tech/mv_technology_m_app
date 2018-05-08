package com.mv.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Rohit Gujar on 30-04-2018.
 */

public class Attendance {


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "unique_Id")
    private int Unique_Id;

    @SerializedName("userInfo")
    @Expose
    private UserInfo userInfo;

    @SerializedName("remarks__c")
    @Expose
    private String remarks;

    @SerializedName("checkOutTime__c")
    @Expose
    private String checkOutTime;

    @SerializedName("checkInTime__c")
    @Expose
    private String checkInTime;

    @SerializedName("Id")
    @Expose
    private String id;

    @SerializedName("Attendance_Date__c")
    @Expose
    private String date;

    @SerializedName("status__c")
    @Expose
    private String status;

    @SerializedName("checkInLoc__Latitude__s")
    @Expose
    private String checkInLat;

    @SerializedName("checkInLoc__Longitude__s")
    @Expose
    private String checkInLng;

    @SerializedName("checkOutLoc__Latitude__s")
    @Expose
    private String checkOutLat;

    @SerializedName("checkOutLoc__Longitude__s")
    @Expose
    private String checkOutLng;


    @SerializedName("MV_User__c")
    @Expose
    private String User;

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }

    public int getUnique_Id() {
        return Unique_Id;
    }

    public void setUnique_Id(int unique_Id) {
        Unique_Id = unique_Id;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCheckInLat() {
        return checkInLat;
    }

    public void setCheckInLat(String checkInLat) {
        this.checkInLat = checkInLat;
    }

    public String getCheckInLng() {
        return checkInLng;
    }

    public void setCheckInLng(String checkInLng) {
        this.checkInLng = checkInLng;
    }

    public String getCheckOutLat() {
        return checkOutLat;
    }

    public void setCheckOutLat(String checkOutLat) {
        this.checkOutLat = checkOutLat;
    }

    public String getCheckOutLng() {
        return checkOutLng;
    }

    public void setCheckOutLng(String checkOutLng) {
        this.checkOutLng = checkOutLng;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
