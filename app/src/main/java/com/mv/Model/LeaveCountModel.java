package com.mv.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mv.Utils.Constants;

@Entity(tableName = Constants.TABLE_HOLIDAY_Count)
public class LeaveCountModel implements Parcelable {


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

    public String getMV_Role__c() {
        return MV_Role__c;
    }

    public void setMV_Role__c(String MV_Role__c) {
        this.MV_Role__c = MV_Role__c;
    }

    public Boolean getUse_User_Leave_Detail__c() {
        return Use_User_Leave_Detail__c;
    }

    public void setUse_User_Leave_Detail__c(Boolean use_User_Leave_Detail__c) {
        Use_User_Leave_Detail__c = use_User_Leave_Detail__c;
    }

    public Integer getTotal_CL_SL_Leave__c() {
        return Total_CL_SL_Leave__c;
    }

    public void setTotal_CL_SL_Leave__c(Integer total_CL_SL_Leave__c) {
        Total_CL_SL_Leave__c = total_CL_SL_Leave__c;
    }

    public Integer getAvailable_CL_SL_Leave__c() {
        return Available_CL_SL_Leave__c;
    }

    public void setAvailable_CL_SL_Leave__c(Integer available_CL_SL_Leave__c) {
        Available_CL_SL_Leave__c = available_CL_SL_Leave__c;
    }

    public Integer getAvailable_Comp_Off_Leave__c() {
        return Available_Comp_Off_Leave__c;
    }

    public void setAvailable_Comp_Off_Leave__c(Integer available_Comp_Off_Leave__c) {
        Available_Comp_Off_Leave__c = available_Comp_Off_Leave__c;
    }

    public Integer getTotal_Comp_Off_Leave__c() {
        return Total_Comp_Off_Leave__c;
    }

    public void setTotal_Comp_Off_Leave__c(Integer total_Comp_Off_Leave__c) {
        Total_Comp_Off_Leave__c = total_Comp_Off_Leave__c;
    }

    public Integer getTotal_Unpaid_Leave__c() {
        return Total_Unpaid_Leave__c;
    }

    public void setTotal_Unpaid_Leave__c(Integer total_Unpaid_Leave__c) {
        Total_Unpaid_Leave__c = total_Unpaid_Leave__c;
    }

    public Integer getTotal_Paid_Leave__c() {
        return Total_Paid_Leave__c;
    }

    public void setTotal_Paid_Leave__c(Integer total_Paid_Leave__c) {
        Total_Paid_Leave__c = total_Paid_Leave__c;
    }

    public Integer getAvailable_Paid_Leave__c() {
        return Available_Paid_Leave__c;
    }

    public void setAvailable_Paid_Leave__c(Integer available_Paid_Leave__c) {
        Available_Paid_Leave__c = available_Paid_Leave__c;
    }

    public Integer getAvailable_Unpaid_Leave__c() {
        return Available_Unpaid_Leave__c;
    }

    public void setAvailable_Unpaid_Leave__c(Integer available_Unpaid_Leave__c) {
        Available_Unpaid_Leave__c = available_Unpaid_Leave__c;
    }

    @ColumnInfo(name = "Id")
    @SerializedName("Id")
    @Expose
    private String Id;

    @ColumnInfo(name = "MV_Role__c")
    @SerializedName("MV_Role__c")
    @Expose
    private String MV_Role__c;

    @ColumnInfo(name = "Use_User_Leave_Detail__c")
    @SerializedName("Use_User_Leave_Detail__c")
    @Expose
    private Boolean Use_User_Leave_Detail__c;

    @ColumnInfo(name = "Total_CL_SL_Leave__c")
    @SerializedName("Total_CL_SL_Leave__c")
    @Expose
    private Integer Total_CL_SL_Leave__c=0;

    @ColumnInfo(name = "Available_CL_SL_Leave__c")
    @SerializedName("Available_CL_SL_Leave__c")
    @Expose
    private Integer Available_CL_SL_Leave__c=0;

    @ColumnInfo(name = "Available_Comp_Off_Leave__c")
    @SerializedName("Available_Comp_Off_Leave__c")
    @Expose
    private Integer Available_Comp_Off_Leave__c=0;



    @ColumnInfo(name = "Total_Comp_Off_Leave__c")
    @SerializedName("Total_Comp_Off_Leave__c")
    @Expose
    private Integer Total_Comp_Off_Leave__c=0;

    @ColumnInfo(name = "Total_Unpaid_Leave__c")
    @SerializedName("Total_Unpaid_Leave__c")
    @Expose
    private Integer Total_Unpaid_Leave__c=0;

    @ColumnInfo(name = "Total_Paid_Leave__c")
    @SerializedName("Total_Paid_Leave__c")
    @Expose
    private Integer Total_Paid_Leave__c=0;

    @ColumnInfo(name = "Available_Paid_Leave__c")
    @SerializedName("Available_Paid_Leave__c")
    @Expose
    private Integer Available_Paid_Leave__c=0;

    @ColumnInfo(name = "Available_Unpaid_Leave__c")
    @SerializedName("Available_Unpaid_Leave__c")
    @Expose
    private Integer Available_Unpaid_Leave__c=0;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.Unique_Id);
        dest.writeString(this.Id);
        dest.writeString(this.MV_Role__c);
        dest.writeValue(this.Use_User_Leave_Detail__c);
        dest.writeValue(this.Total_CL_SL_Leave__c);
        dest.writeValue(this.Available_CL_SL_Leave__c);
        dest.writeValue(this.Available_Comp_Off_Leave__c);
        dest.writeValue(this.Total_Comp_Off_Leave__c);
        dest.writeValue(this.Total_Unpaid_Leave__c);
        dest.writeValue(this.Total_Paid_Leave__c);
        dest.writeValue(this.Available_Paid_Leave__c);
        dest.writeValue(this.Available_Unpaid_Leave__c);
    }

    public LeaveCountModel() {
    }

    protected LeaveCountModel(Parcel in) {
        this.Unique_Id = in.readInt();
        this.Id = in.readString();
        this.MV_Role__c = in.readString();
        this.Use_User_Leave_Detail__c = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.Total_CL_SL_Leave__c = (Integer) in.readValue(Integer.class.getClassLoader());
        this.Available_CL_SL_Leave__c = (Integer) in.readValue(Integer.class.getClassLoader());
        this.Available_Comp_Off_Leave__c = (Integer) in.readValue(Integer.class.getClassLoader());
        this.Total_Comp_Off_Leave__c = (Integer) in.readValue(Integer.class.getClassLoader());
        this.Total_Unpaid_Leave__c = (Integer) in.readValue(Integer.class.getClassLoader());
        this.Total_Paid_Leave__c = (Integer) in.readValue(Integer.class.getClassLoader());
        this.Available_Paid_Leave__c = (Integer) in.readValue(Integer.class.getClassLoader());
        this.Available_Unpaid_Leave__c = (Integer) in.readValue(Integer.class.getClassLoader());
    }

    public static final Creator<LeaveCountModel> CREATOR = new Creator<LeaveCountModel>() {
        @Override
        public LeaveCountModel createFromParcel(Parcel source) {
            return new LeaveCountModel(source);
        }

        @Override
        public LeaveCountModel[] newArray(int size) {
            return new LeaveCountModel[size];
        }
    };
}