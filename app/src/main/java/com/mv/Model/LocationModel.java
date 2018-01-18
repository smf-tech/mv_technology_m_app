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
 * Created by nanostuffs on 30-10-2017.
 */
@Entity(tableName = Constants.TABLE_LOCATION)
public class LocationModel implements Parcelable {
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.Id);
        dest.writeString(this.village);
        dest.writeString(this.taluka);
        dest.writeString(this.state);
        dest.writeString(this.schoolName);
        dest.writeString(this.schoolCode);
        dest.writeString(this.district);
        dest.writeString(this.createdDate);
        dest.writeString(this.cluster);
    }

    public LocationModel() {
    }

    protected LocationModel(Parcel in) {
        this.Id = in.readString();
        this.village = in.readString();
        this.taluka = in.readString();
        this.state = in.readString();
        this.schoolName = in.readString();
        this.schoolCode = in.readString();
        this.district = in.readString();
        this.createdDate = in.readString();
        this.cluster = in.readString();
    }

    public static final Parcelable.Creator<LocationModel> CREATOR = new Parcelable.Creator<LocationModel>() {
        @Override
        public LocationModel createFromParcel(Parcel source) {
            return new LocationModel(source);
        }

        @Override
        public LocationModel[] newArray(int size) {
            return new LocationModel[size];
        }
    };
}
