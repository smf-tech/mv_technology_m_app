package com.mv.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mv.Utils.Constants;

@Entity(tableName = Constants.TABLE_HOLIDAY)
public class HolidayListModel implements Parcelable {


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

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    @ColumnInfo(name = "Name")
    @SerializedName("Name")
    @Expose
    private String Name;

    @ColumnInfo(name = "Category__c")
    @SerializedName("Category__c")
    @Expose
    private String Category__c;

    @ColumnInfo(name = "description__c")
    @SerializedName("description__c")
    @Expose
    private String description__c;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getCategory__c() {
        return Category__c;
    }

    public void setCategory__c(String category__c) {
        Category__c = category__c;
    }

    public String getDescription__c() {
        return description__c;
    }

    public void setDescription__c(String description__c) {
        this.description__c = description__c;
    }

    public String getHoliday_Date__c() {
        return Holiday_Date__c;
    }

    public void setHoliday_Date__c(String holiday_Date__c) {
        Holiday_Date__c = holiday_Date__c;
    }

    public String getIsActive__c() {
        return isActive__c;
    }

    public void setIsActive__c(String isActive__c) {
        this.isActive__c = isActive__c;
    }

    public String getOrganisation__c() {
        return Organisation__c;
    }

    public void setOrganisation__c(String organisation__c) {
        Organisation__c = organisation__c;
    }

    @ColumnInfo(name = "Holiday_Date__c")
    @SerializedName("Holiday_Date__c")
    @Expose
    private String Holiday_Date__c;

    @ColumnInfo(name = "isActive__c")
    @SerializedName("isActive__c")
    @Expose
    private String isActive__c;

    @ColumnInfo(name = "Organisation__c")
    @SerializedName("Organisation__c")
    @Expose
    private String Organisation__c;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.Unique_Id);
        dest.writeString(this.Id);
        dest.writeString(this.Category__c);
        dest.writeString(this.description__c);
        dest.writeString(this.Holiday_Date__c);
        dest.writeString(this.isActive__c);
        dest.writeString(this.Organisation__c);
        dest.writeString(this.Name);
    }

    public HolidayListModel() {
    }

    protected HolidayListModel(Parcel in) {
        this.Unique_Id = in.readInt();
        this.Id = in.readString();
        this.Category__c = in.readString();
        this.description__c = in.readString();
        this.Holiday_Date__c = in.readString();
        this.isActive__c = in.readString();
        this.Organisation__c = in.readString();
        this.Name = in.readString();
    }

    public static final Creator<HolidayListModel> CREATOR = new Creator<HolidayListModel>() {
        @Override
        public HolidayListModel createFromParcel(Parcel source) {
            return new HolidayListModel(source);
        }

        @Override
        public HolidayListModel[] newArray(int size) {
            return new HolidayListModel[size];
        }
    };
}