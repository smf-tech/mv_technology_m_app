package com.mv.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mv.Utils.Constants;

import java.io.Serializable;

/**
 * Created by nanostuffs on 03-02-2018.
 */
@Entity(tableName = Constants.TABLE_ATTENDANCE)
public class Attendance implements Serializable {


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "unique_Id")
    private int Unique_Id;

    @SerializedName("Attendance_Date__c")
    @Expose
    @ColumnInfo(name = "Attendance_Date__c")
    private String date;

    @SerializedName("status__c")
    @Expose
    @ColumnInfo(name = "status__c")
    private String status;

    @SerializedName("checkIn_Attendance_Address__c")
    @Expose
    @ColumnInfo(name = "checkIn_Attendance_Address__c")
    private String checkIn_Attendance_Address__c;

    @SerializedName("checkOut_Attendance_Address__c")
    @Expose
    @ColumnInfo(name = "checkOut_Attendance_Address__c")
    private String checkOut_Attendance_Address__c;

    @SerializedName("MV_User__c")
    @Expose
    @ColumnInfo(name = "MV_User__c")
    private String User;

    @SerializedName("remarks__c")
    @Expose
    @ColumnInfo(name = "remarks__c")
    private String remarks;

    @SerializedName("checkOutTime__c")
    @Expose
    @ColumnInfo(name = "checkOutTime__c")
    private String checkOutTime;

    @SerializedName("checkInTime__c")
    @Expose
    @ColumnInfo(name = "checkInTime__c")
    private String checkInTime;

    @ColumnInfo(name = "Synch")
    private String Synch;

    @SerializedName("Id")
    @Expose
    @ColumnInfo(name = "Id")
    private String Id;

    @SerializedName("checkInLoc__Latitude__s")
    @Expose
    @ColumnInfo(name = "checkInLoc__Latitude__s")
    private String checkInLat;

    @SerializedName("checkInLoc__Longitude__s")
    @Expose
    @ColumnInfo(name = "checkInLoc__Longitude__s")
    private String checkInLng;

    @SerializedName("checkOutLoc__Latitude__s")
    @Expose
    @ColumnInfo(name = "checkOutLoc__Latitude__s")
    private String checkOutLat;

    @SerializedName("checkOutLoc__Longitude__s")
    @Expose
    @ColumnInfo(name = "checkOutLoc__Longitude__s")
    private String checkOutLng;

    public String getId() {
        return Id;
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    public String getSynch() {
        return Synch;
    }

    public void setSynch(String synch) {
        Synch = synch;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCheckIn_Attendance_Address__c() {
        return checkIn_Attendance_Address__c;
    }

    public void setCheckIn_Attendance_Address__c(String checkIn_Attendance_Address__c) {
        this.checkIn_Attendance_Address__c = checkIn_Attendance_Address__c;
    }

    public String getCheckOut_Attendance_Address__c() {
        return checkOut_Attendance_Address__c;
    }

    public void setCheckOut_Attendance_Address__c(String checkOut_Attendance_Address__c) {
        this.checkOut_Attendance_Address__c = checkOut_Attendance_Address__c;
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


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getUnique_Id() {
        return Unique_Id;
    }

    public void setUnique_Id(int unique_Id) {
        Unique_Id = unique_Id;
    }

    public Attendance() {
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

}
