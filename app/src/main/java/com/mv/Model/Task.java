package com.mv.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mv.Utils.Constants;

/**
 * Created by nanostuffs on 26-09-2017.
 */
@Entity(tableName = Constants.TABLE_TASK)
public class Task implements Parcelable {


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
    private String Id;

    @ColumnInfo(name = "Name")
    private String Name;

    @ColumnInfo(name = "Is_Completed__c")
    private Boolean Is_Completed__c;
    @SerializedName("Is_Response_Mnadetory")
    @Expose
    @ColumnInfo(name = "Is_Response_Mnadetory__c")
    private Boolean Is_Response_Mnadetory__c;

    @ColumnInfo(name = "MV_Process__c")
    @SerializedName("MV_Process")
    @Expose
    private String MV_Process__c;

    @ColumnInfo(name = "Task_Response__c")
    @SerializedName("Answer")
    @Expose
    private String Task_Response__c = "";
    @SerializedName("Task_Text")
    @Expose
    @ColumnInfo(name = "Task_Text__c")
    private String Task_Text__c;

    public String getTask_Text___Lan_c() {
        return Task_Text___Lan_c;
    }

    public void setTask_Text___Lan_c(String task_Text___Lan_c) {
        Task_Text___Lan_c = task_Text___Lan_c;
    }

    public String getPicklist_Value_Lan__c() {
        return Picklist_Value_Lan__c;
    }

    public void setPicklist_Value_Lan__c(String picklist_Value_Lan__c) {
        Picklist_Value_Lan__c = picklist_Value_Lan__c;
    }

    @SerializedName("lanTsaskText")
    @Expose

    @ColumnInfo(name = "Task_Text__Lan_c")
    private String Task_Text___Lan_c;

    @SerializedName("Task_type")
    @Expose
    @ColumnInfo(name = "Task_type__c")
    private String Task_type__c;
    @SerializedName("Picklist_Value")
    @Expose
    @ColumnInfo(name = "Picklist_Value")
    private String Picklist_Value__c;


    @SerializedName("lanPicklistValue")
    @Expose
    @ColumnInfo(name = "Picklist_Value_lan")
    private String Picklist_Value_Lan__c;

    @ColumnInfo(name = "MV_Task__c_Id")
    @SerializedName("MV_Task")
    @Expose
    private String MV_Task__c_Id;

    @ColumnInfo(name = "Timestamp__c")
    @SerializedName("Timestamp")
    @Expose
    private String Timestamp__c;

    @SerializedName("Unique_Idd")
    @Expose
    @ColumnInfo(name = "Unique_Id__c")
    private String Unique_Id__c;

    @ColumnInfo(name = "MTUser__c")
    @SerializedName("MV_User")
    @Expose
    private String MTUser__c;
    @ColumnInfo(name = "isSave")
    @SerializedName("isSave ")
    @Expose
    private String isSave;
    @ColumnInfo(name = "IsApproved__c")
    @SerializedName("IsApproved")
    @Expose
    private String IsApproved__c;
    @ColumnInfo(name = "Location_Level__c")
    @SerializedName("Location_Level")
    @Expose
    private String LocationLevel;
    @ColumnInfo(name = "validation")
    @SerializedName("Validaytion_on_text")
    @Expose
    private String validation;
    @SerializedName("Section_Name")
    @Expose
    private String Section_Name__c;
    @ColumnInfo(name = "status__c")
    @SerializedName("status__c")
    @Expose
    private String status;

    @ColumnInfo(name = "IsEditable")
    @SerializedName("IsEditable")
    @Expose
    private String IsEditable;


    public String getIsEditable__c() {
        return IsEditable;
    }

    public void setIsEditable__c(String IsEditable) {
        this.IsEditable = IsEditable;
    }

    public String getStatus__c() {
        return status;
    }

    public void setStatus__c(String status) {
        this.status = status;
    }

    public String getIsSave() {
        return isSave;
    }

    public void setIsSave(String isSave) {
        this.isSave = isSave;
    }

    public String getIsApproved__c() {
        return IsApproved__c;
    }

    public void setIsApproved__c(String isApproved__c) {
        IsApproved__c = isApproved__c;
    }


    public String getLocationLevel() {
        return LocationLevel;
    }

    public void setLocationLevel(String locationLevel) {
        LocationLevel = locationLevel;
    }


    public String getValidation() {
        return validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public String getSection_Name__c() {
        return Section_Name__c;
    }

    public void setSection_Name__c(String section_Name__c) {
        Section_Name__c = section_Name__c;
    }


    public String getPicklist_Value__c() {
        return Picklist_Value__c;
    }

    public void setPicklist_Value__c(String picklist_Value__c) {
        Picklist_Value__c = picklist_Value__c;
    }

    public String getMV_Task__c_Id() {
        return MV_Task__c_Id;
    }

    public void setMV_Task__c_Id(String MV_Task__c_Id) {
        this.MV_Task__c_Id = MV_Task__c_Id;
    }

    public String getTimestamp__c() {
        return Timestamp__c;
    }

    public void setTimestamp__c(String timestamp__c) {
        Timestamp__c = timestamp__c;
    }

    public String getUnique_Id__c() {
        return Unique_Id__c;
    }

    public void setUnique_Id__c(String unique_Id__c) {
        Unique_Id__c = unique_Id__c;
    }

    public String getMTUser__c() {
        return MTUser__c;
    }

    public void setMTUser__c(String MTUser__c) {
        this.MTUser__c = MTUser__c;
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

    public Boolean getIs_Completed__c() {
        return Is_Completed__c;
    }

    public void setIs_Completed__c(Boolean is_Completed__c) {
        Is_Completed__c = is_Completed__c;
    }

    public Boolean getIs_Response_Mnadetory__c() {
        return Is_Response_Mnadetory__c;
    }

    public void setIs_Response_Mnadetory__c(Boolean is_Response_Mnadetory__c) {
        Is_Response_Mnadetory__c = is_Response_Mnadetory__c;
    }

    public String getMV_Process__c() {
        return MV_Process__c;
    }

    public void setMV_Process__c(String MV_Process__c) {
        this.MV_Process__c = MV_Process__c;
    }

    public String getTask_Response__c() {
        return Task_Response__c;
    }

    public void setTask_Response__c(String task_Response__c) {
        Task_Response__c = task_Response__c;
    }

    public String getTask_Text__c() {
        return Task_Text__c;
    }

    public void setTask_Text__c(String task_Text__c) {
        Task_Text__c = task_Text__c;
    }

    public String getTask_type__c() {
        return Task_type__c;
    }

    public void setTask_type__c(String task_type__c) {
        Task_type__c = task_type__c;
    }


    public Task() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.Unique_Id);
        dest.writeString(this.Id);
        dest.writeString(this.Name);
        dest.writeValue(this.Is_Completed__c);
        dest.writeValue(this.Is_Response_Mnadetory__c);
        dest.writeString(this.MV_Process__c);
        dest.writeString(this.Task_Response__c);
        dest.writeString(this.Task_Text__c);
        dest.writeString(this.Task_Text___Lan_c);
        dest.writeString(this.Task_type__c);
        dest.writeString(this.Picklist_Value__c);
        dest.writeString(this.Picklist_Value_Lan__c);
        dest.writeString(this.MV_Task__c_Id);
        dest.writeString(this.Timestamp__c);
        dest.writeString(this.Unique_Id__c);
        dest.writeString(this.MTUser__c);
        dest.writeString(this.isSave);
        dest.writeString(this.IsApproved__c);
        dest.writeString(this.LocationLevel);
        dest.writeString(this.validation);
        dest.writeString(this.Section_Name__c);
        dest.writeString(this.status);
        dest.writeString(this.IsEditable);
    }

    protected Task(Parcel in) {
        this.Unique_Id = in.readInt();
        this.Id = in.readString();
        this.Name = in.readString();
        this.Is_Completed__c = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.Is_Response_Mnadetory__c = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.MV_Process__c = in.readString();
        this.Task_Response__c = in.readString();
        this.Task_Text__c = in.readString();
        this.Task_Text___Lan_c = in.readString();
        this.Task_type__c = in.readString();
        this.Picklist_Value__c = in.readString();
        this.Picklist_Value_Lan__c = in.readString();
        this.MV_Task__c_Id = in.readString();
        this.Timestamp__c = in.readString();
        this.Unique_Id__c = in.readString();
        this.MTUser__c = in.readString();
        this.isSave = in.readString();
        this.IsApproved__c = in.readString();
        this.LocationLevel = in.readString();
        this.validation = in.readString();
        this.Section_Name__c = in.readString();
        this.status = in.readString();
        this.IsEditable = in.readString();
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel source) {
            return new Task(source);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
}
