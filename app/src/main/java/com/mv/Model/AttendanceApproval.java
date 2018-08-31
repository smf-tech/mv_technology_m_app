package com.mv.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by user on 7/31/2018.
 */

public class AttendanceApproval implements Serializable {

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

    public String getCheckInTimeC() {
        return checkInTimeC;
    }

    public void setCheckInTimeC(String checkInTimeC) {
        this.checkInTimeC = checkInTimeC;
    }

    public String getCheckOutTimeC() {
        return checkOutTimeC;
    }

    public void setCheckOutTimeC(String checkOutTimeC) {
        this.checkOutTimeC = checkOutTimeC;
    }

    public String getStatusC() {
        return statusC;
    }

    public void setStatusC(String statusC) {
        this.statusC = statusC;
    }

    public String getReason() {
        return Remarks;
    }

    public void setReason(String remarks) {
        this.Remarks = remarks;
    }

    public String getUser_Name__c() {
        return User_Name__c;
    }

    public void setUser_Name__c(String User_Name__c) {
        this.User_Name__c = User_Name__c;
    }

    public String getUser_Role__c() {
        return User_Role__c;
    }

    public void setUser_Role__c(String User_Role__c) {
        this.User_Role__c = User_Role__c;
    }

    public String getCheck_In_Location_Difference__c() {
        return Check_In_Location_Difference__c;
    }

    public void setCheck_In_Location_Difference__c(String Check_In_Location_Difference__c) {
        this.Check_In_Location_Difference__c = Check_In_Location_Difference__c;
    }

    public String getCheck_Out_Location_Difference__c() {
        return Check_Out_Location_Difference__c;
    }

    public void setCheck_Out_Location_Difference__c(String Check_Out_Location_Difference__cc) {
        this.Check_Out_Location_Difference__c = Check_Out_Location_Difference__c;

    }

    public String getApprover_User__c() {
        return Approver_User__c;
    }

    public void setApprover_User__c(String Approver_User__c) {
        this.Approver_User__c = Approver_User__c;
    }

    public String getFinal_Status__c() {
        return Final_Status__c;
    }

    public void setFinal_Status__c(String Final_Status__c) {
        this.Final_Status__c = Final_Status__c;
    }


    @SerializedName("User_Role__c")
    @Expose
    private String User_Role__c;

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
    private String checkInTimeC;

    @SerializedName("checkOutTime__c")
    @Expose
    private String checkOutTimeC;

    @SerializedName("status__c")
    @Expose
    private String statusC;

    @SerializedName("remarks__c")
    @Expose
    private String  Remarks;

    @SerializedName("User_Name__c")
    @Expose
    private String User_Name__c;

    @SerializedName("Check_In_Location_Difference__c")
    @Expose
    private String Check_In_Location_Difference__c;

    @SerializedName("Check_Out_Location_Difference__c")
    @Expose
    private String Check_Out_Location_Difference__c;



    @SerializedName("Approver_User__c")
    @Expose
    private String Approver_User__c;

    @SerializedName("Final_Status__c")
    @Expose
    private String Final_Status__c;


    /*public final static Parcelable.Creator<AttendanceApproval> CREATOR = new Creator<AttendanceApproval>() {


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
            ;*/

    protected AttendanceApproval(Parcel in) {

        this.id = ((String) in.readValue((String.class.getClassLoader())));
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.createdDate = ((String) in.readValue((String.class.getClassLoader())));
        this.attendanceDateC = ((String) in.readValue((String.class.getClassLoader())));
        this.checkInTimeC = ((String) in.readValue((String.class.getClassLoader())));
        this.checkOutTimeC = ((String) in.readValue((String.class.getClassLoader())));
        this.statusC = ((String) in.readValue((String.class.getClassLoader())));
        this.User_Name__c = ((String) in.readValue((String.class.getClassLoader())));
        this.User_Role__c = ((String) in.readValue((String.class.getClassLoader())));
        this.Check_In_Location_Difference__c = ((String) in.readValue((String.class.getClassLoader())));
        this.Check_Out_Location_Difference__c = ((String) in.readValue((String.class.getClassLoader())));
        this.Remarks = ((String) in.readValue((String.class.getClassLoader())));
    }

   /* @Override
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
        dest.writeValue(User_Role__c);
        dest.writeValue(Check_In_Location_Difference__c);
        dest.writeValue(Check_Out_Location_Difference__c);

    }*/

}
