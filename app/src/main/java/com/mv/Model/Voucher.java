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
@Entity(tableName = Constants.TABLE_VOUCHER)
public class Voucher implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "unique_Id")
    private int UniqueId;

    @ColumnInfo(name = "Id")
    @SerializedName("Id")
    @Expose
    private String Id;

    @ColumnInfo(name = "Project")
    @SerializedName("Project__c")
    @Expose
    private String Project;

    @ColumnInfo(name = "Date")
    @SerializedName("Date__c")
    @Expose
    private String Date;

    @ColumnInfo(name = "Decription")
    @SerializedName("Description__c")
    @Expose
    private String Decription;

    @ColumnInfo(name = "NoOfPeople")
    @SerializedName("No_Of_Peopel_Travelled__c")
    @Expose
    private String NoOfPeople;

    @ColumnInfo(name = "User")
    @SerializedName("MV_User__c")
    @Expose
    private String User;

    @ColumnInfo(name = "FromDate")
    @SerializedName("FromDate__c")
    @Expose
    private String FromDate;
    @ColumnInfo(name = "ToDate")
    @SerializedName("ToDate__c")
    @Expose
    private String ToDate;
    public String getPlace() {
        return Place;
    }
    public void setPlace(String place) {
        Place = place;
    }
    @ColumnInfo(name = "Place")
    @SerializedName("Place__c")
    @Expose
    private String Place;



    public String getProject() {
        return Project;
    }

    public void setProject(String project) {
        Project = project;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getDecription() {
        return Decription;
    }

    public void setDecription(String decription) {
        Decription = decription;
    }

    public String getNoOfPeople() {
        return NoOfPeople;
    }

    public void setNoOfPeople(String noOfPeople) {
        NoOfPeople = noOfPeople;
    }

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


    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }

    public Voucher() {
    }

    public String getFromDate() {
        return FromDate;
    }

    public void setFromDate(String fromDate) {
        FromDate = fromDate;
    }

    public String getToDate() {
        return ToDate;
    }

    public void setToDate(String toDate) {
        ToDate = toDate;
    }

}
