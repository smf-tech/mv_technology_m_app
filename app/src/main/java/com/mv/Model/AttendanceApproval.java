package com.mv.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by user on 7/31/2018.
 */

public class AttendanceApproval implements Parcelable {

    @SerializedName("Id")
    @Expose
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getAttendanceDateC() {
        return attendanceDateC;
    }

    public void setAttendanceDateC(String attendanceDateC) {
        this.attendanceDateC = attendanceDateC;
    }

    public Float getCheckInTimeC() {
        return checkInTimeC;
    }

    public void setCheckInTimeC(Float checkInTimeC) {
        this.checkInTimeC = checkInTimeC;
    }

    public Float getCheckOutTimeC() {
        return checkOutTimeC;
    }

    public void setCheckOutTimeC(Float checkOutTimeC) {
        this.checkOutTimeC = checkOutTimeC;
    }

    public String getStatusC() {
        return statusC;
    }

    public void setStatusC(String statusC) {
        this.statusC = statusC;
    }

    public String getUser_Name__c() {
        return User_Name__c;
    }

    public void setUser_Name__c(String User_Name__c) {
        this.User_Name__c = User_Name__c;
    }


    @SerializedName("Name")
    @Expose
    private String name;

    @SerializedName("CreatedDate")
    @Expose
    private String createdDate;

    @SerializedName("Attendance_Date__c")
    @Expose
    private String attendanceDateC;

    @SerializedName("checkInTime__c")
    @Expose
    private Float checkInTimeC;

    @SerializedName("checkOutTime__c")
    @Expose
    private Float checkOutTimeC;

    @SerializedName("status__c")
    @Expose
    private String statusC;

    @SerializedName("User_Name__c")
    @Expose
    private String User_Name__c;

    public final static Parcelable.Creator<AttendanceApproval> CREATOR = new Creator<AttendanceApproval>() {


        @SuppressWarnings({
                "unchecked"
        })
        public AttendanceApproval createFromParcel(Parcel in) {
            return new AttendanceApproval(in);
        }

        public AttendanceApproval[] newArray(int size) {
            return (new AttendanceApproval[size]);
        }

    }
            ;

    protected AttendanceApproval(Parcel in) {

        this.id = ((String) in.readValue((String.class.getClassLoader())));
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.createdDate = ((String) in.readValue((String.class.getClassLoader())));
        this.attendanceDateC = ((String) in.readValue((String.class.getClassLoader())));
        this.checkInTimeC = ((Float) in.readValue((Float.class.getClassLoader())));
        this.checkOutTimeC = ((Float) in.readValue((Float.class.getClassLoader())));
        this.statusC = ((String) in.readValue((String.class.getClassLoader())));
        this.User_Name__c = ((String) in.readValue((String.class.getClassLoader())));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeValue(id);
        dest.writeValue(name);
        dest.writeValue(createdDate);
        dest.writeValue(attendanceDateC);
        dest.writeValue(checkInTimeC);
        dest.writeValue(checkOutTimeC);
        dest.writeValue(statusC);
        dest.writeValue(User_Name__c);

    }
}
