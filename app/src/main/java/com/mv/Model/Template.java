package com.mv.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mv.Utils.Constants;

import java.io.Serializable;

/**
 * Created by Rohit Gujar on 13-09-2017.
 */
@Entity(tableName = Constants.TABLE_PROCESS)
public class Template implements Serializable, Parcelable {


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "unique_Id")
    private int Unique_Id;

    @ColumnInfo(name = "template_id")
    @SerializedName("template_id")
    @Expose
    private String Id;

    public String getUser_Mobile_App_Version__c() {
        return User_Mobile_App_Version__c;
    }

    public void setUser_Mobile_App_Version__c(String user_Mobile_App_Version__c) {
        User_Mobile_App_Version__c = user_Mobile_App_Version__c;
    }

    @ColumnInfo(name = "User_Mobile_App_Version__c")
    @SerializedName("User_Mobile_App_Version__c")
    @Expose
    private String User_Mobile_App_Version__c;

    @ColumnInfo(name = "template_name")
    @SerializedName("template_name")
    @Expose
    private String Name;

    public String getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(String answerCount) {
        this.answerCount = answerCount;
    }

    @SerializedName("answerCount")
    @Expose
    @ColumnInfo(name = "answerCount")
    private String answerCount;


    public String getExpectedCount() {
        return expectedCount;
    }

    public void setExpectedCount(String expectedCount) {
        this.expectedCount = expectedCount;
    }

    public String getSubmittedCount() {
        return submittedCount;
    }

    public void setSubmittedCount(String submittedCount) {
        this.submittedCount = submittedCount;
    }

    @SerializedName("expectedCount")
    @Expose
    @ColumnInfo(name = "expectedCount")
    private String expectedCount;

    @SerializedName("submittedCount")
    @Expose
    @ColumnInfo(name = "submittedCount")
    private String submittedCount;

    public String getCategory__c() {
        return Category__c;
    }

    public void setCategory__c(String category__c) {
        Category__c = category__c;
    }

    public static Creator<Template> getCREATOR() {
        return CREATOR;
    }

    @SerializedName("Category__c")
    @Expose
    @ColumnInfo(name = "Category__c")
    private String Category__c;

    public String getTargated_Date__c() {
        return Targated_Date__c;
    }

    public void setTargated_Date__c(String targated_Date__c) {
        Targated_Date__c = targated_Date__c;
    }

    @ColumnInfo(name = "Targated_Date__c")
    @SerializedName("Targated_Date__c")
    @Expose
    private String Targated_Date__c;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @SerializedName("status")
    @Expose
    private String status;
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

    private Boolean Is_Editable__c;

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

    private Boolean Is_Multiple_Entry_Allowed__c;

    public Template() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.Unique_Id);
        dest.writeString(this.Id);
        dest.writeString(this.User_Mobile_App_Version__c);
        dest.writeString(this.Name);
        dest.writeString(this.answerCount);
        dest.writeString(this.expectedCount);
        dest.writeString(this.submittedCount);
        dest.writeString(this.Category__c);
        dest.writeString(this.Targated_Date__c);
        dest.writeValue(this.Location);
        dest.writeString(this.LocationLevel);
        dest.writeString(this.type);
        dest.writeString(this.url);
        dest.writeString(this.status);
        dest.writeString(this.MV_Process__c);
        dest.writeString(this.MV_User__c);
        dest.writeString(this.P1F1__c);
        dest.writeString(this.P1F2__c);
        dest.writeString(this.P1F3__c);
        dest.writeString(this.P1F4__c);
        dest.writeString(this.P1F5__c);
        dest.writeValue(this.Is_Editable__c);
        dest.writeValue(this.Is_Multiple_Entry_Allowed__c);
    }

    protected Template(Parcel in) {
        this.Unique_Id = in.readInt();
        this.Id = in.readString();
        this.User_Mobile_App_Version__c = in.readString();
        this.Name = in.readString();
        this.answerCount = in.readString();
        this.expectedCount = in.readString();
        this.submittedCount = in.readString();
        this.Category__c = in.readString();
        this.Targated_Date__c = in.readString();
        this.Location = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.LocationLevel = in.readString();
        this.type = in.readString();
        this.url = in.readString();
        this.status = in.readString();
        this.MV_Process__c = in.readString();
        this.MV_User__c = in.readString();
        this.P1F1__c = in.readString();
        this.P1F2__c = in.readString();
        this.P1F3__c = in.readString();
        this.P1F4__c = in.readString();
        this.P1F5__c = in.readString();
        this.Is_Editable__c = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.Is_Multiple_Entry_Allowed__c = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public static final Creator<Template> CREATOR = new Creator<Template>() {
        @Override
        public Template createFromParcel(Parcel source) {
            return new Template(source);
        }

        @Override
        public Template[] newArray(int size) {
            return new Template[size];
        }
    };
}
