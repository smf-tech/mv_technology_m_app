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
 * Created by nanostuffs on 05-12-2017.
 */
@Entity(tableName = Constants.TABLE_CALANDER)
public class CalenderEvent implements Parcelable {

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
    String id;

    @ColumnInfo(name = "Description__c")
    @SerializedName("Description__c")
    @Expose
    String description;

    @ColumnInfo(name = "Date__c")
    @SerializedName("Date__c")
    @Expose
    String date;

    public String getEnd_Date__c() {
        return End_Date__c;
    }

    public void setEnd_Date__c(String end_Date__c) {
        End_Date__c = end_Date__c;
    }

    public String getEvent_End_Time__c() {
        return Event_End_Time__c;
    }

    public void setEvent_End_Time__c(String event_End_Time__c) {
        Event_End_Time__c = event_End_Time__c;
    }

    @ColumnInfo(name = "End_Date__c")
    @SerializedName("End_Date__c")
    @Expose
    String End_Date__c;

    @ColumnInfo(name = "Event_End_Time__c")
    @SerializedName("Event_End_Time__c")
    @Expose
    String Event_End_Time__c;

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    @ColumnInfo(name = "Status__c")
    @SerializedName("Status__c")
    @Expose
    String Status;

    public String getOrganization__c() {
        return organization__c;
    }

    public void setOrganization__c(String organization__c) {
        this.organization__c = organization__c;
    }

    @ColumnInfo(name = "organization__c")
    @SerializedName("organization__c")
    @Expose
    String organization__c;


    @ColumnInfo(name = "MV_User__c")
    @SerializedName("MV_User__c")
    @Expose
    String MV_User1__c;

    @ColumnInfo(name = "Title__c")
    @SerializedName("Title__c")
    @Expose
    String title;

    public String getEvent_Time__c() {
        return Event_Time__c;
    }

    public void setEvent_Time__c(String event_Time__c) {
        Event_Time__c = event_Time__c;
    }

    @ColumnInfo(name = "Event_Time__c")
    @SerializedName("Event_Time__c")
    @Expose
    String Event_Time__c;
    @ColumnInfo(name = "Village__c")
    @SerializedName("Village__c")

    @Expose
    String Village__c;

    @ColumnInfo(name = "Taluka__c")
    @SerializedName("Taluka__c")
    @Expose
    String Taluka__c;

    @ColumnInfo(name = "State__c")
    @SerializedName("State__c")
    @Expose
    String State__c;

    public String getCreatedUserData() {
        return CreatedUserData;
    }

    public void setCreatedUserData(String CreatedUserData) {
        this.CreatedUserData = CreatedUserData;
    }

    public String getProceesTotalCount() {
        return ProceesTotalCount;
    }

    public void setProceesTotalCount(String ProceesTotalCount) {
        this.ProceesTotalCount = ProceesTotalCount;
    }

    public String getProceesSubmittedCount() {
        return ProceesSubmittedCount;
    }

    public void setProceesSubmittedCount(String proceesSubmittedCount) {
        this.ProceesSubmittedCount = proceesSubmittedCount;
    }

    @ColumnInfo(name = "CreatedRoleData")
    @SerializedName("Assigned_By_Name__c")
    @Expose
    String CreatedUserData;

    @ColumnInfo(name = "ProceesTotalCount")
    @SerializedName("Total_Form__c")
    @Expose
    String ProceesTotalCount;

    @ColumnInfo(name = "ProceesSubmittedCount")
    @SerializedName("Filled_Form__c")
    @Expose
    String ProceesSubmittedCount;

    public String getDistrict__c() {
        return District__c;
    }

    public void setDistrict__c(String district__c) {
        District__c = district__c;
    }

    public String getRole__c() {
        return Role__c;
    }

    public void setRole__c(String role__c) {
        Role__c = role__c;
    }

    @ColumnInfo(name = "Role__c")
    @SerializedName("Role__c")

    @Expose
    String Role__c = "";
    @ColumnInfo(name = "Cluster__c")
    @SerializedName("Cluster__c")

    @Expose
    String Cluster__c;

    @ColumnInfo(name = "School__c")
    @SerializedName("School__c")
    @Expose
    String School__c;

    @ColumnInfo(name = "District__c")
    @SerializedName("District__c")
    @Expose
    String District__c;

    @ColumnInfo(name = "Description_New__c")
    @SerializedName("Description_New__c")
    @Expose
    String Description_New__c;

    @ColumnInfo(name = "Is_Event_for_All_Role__c")
    @SerializedName("Is_Event_for_All_Role__c")
    @Expose
    String Is_Event_for_All_Role__c;


    @ColumnInfo(name = "Assigned_User_Ids__c")
    @SerializedName("Assigned_User_Ids__c")
    @Expose

    String Assigned_User_Ids__c;

    @ColumnInfo(name = "Assign_id_name__c")
    @SerializedName("Assign_id_name__c")
    @Expose
    String Assign_id_name__c;


    @ColumnInfo(name = "MV_Process__c")
    @SerializedName("MV_Process__c")
    @Expose
    String MV_Process__c;

    @ColumnInfo(name = "Present_User__c")
    @SerializedName("Present_User__c")
    @Expose
    String Present_User__c;

    public String getPresent_User__c() {
        return Present_User__c;
    }

    public void setPresent_User__c(String present_User__c) {
        Present_User__c = present_User__c;
    }

    public String getVillage__c() {
        return Village__c;
    }

    public void setVillage__c(String village__c) {
        Village__c = village__c;
    }

    public String getTaluka__c() {
        return Taluka__c;
    }

    public void setTaluka__c(String taluka__c) {
        Taluka__c = taluka__c;
    }

    public String getState__c() {
        return State__c;
    }

    public void setState__c(String state__c) {
        State__c = state__c;
    }

    public String getCluster__c() {
        return Cluster__c;
    }

    public void setCluster__c(String cluster__c) {
        Cluster__c = cluster__c;
    }

    public String getSchool__c() {
        return School__c;
    }

    public void setSchool__c(String school__c) {
        School__c = school__c;
    }

    public String getDescription_New__c() {
        return Description_New__c;
    }

    public void setDescription_New__c(String description_New__c) {
        Description_New__c = description_New__c;
    }

    public String getIs_Event_for_All_Role__c() {
        return Is_Event_for_All_Role__c;
    }

    public void setIs_Event_for_All_Role__c(String is_Event_for_All_Role__c) {
        Is_Event_for_All_Role__c = is_Event_for_All_Role__c;
    }

    public static Creator<CalenderEvent> getCREATOR() {
        return CREATOR;
    }


    public String getAssigned_User_Ids__c() {
        return Assigned_User_Ids__c;
    }

    public void setAssigned_User_Ids__c(String assigned_User_Ids__c) {
        Assigned_User_Ids__c = assigned_User_Ids__c;
    }

    public String getAssign_id_name__c() {
        return Assign_id_name__c;
    }

    public void setAssign_id_name__c(String assign_id_name__c) {
        Assign_id_name__c = assign_id_name__c;
    }

    public String getMV_Process__c() {
        return MV_Process__c;
    }

    public void setMV_Process__c(String MV_Process__c) {
        this.MV_Process__c = MV_Process__c;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMV_User1__c() {
        return MV_User1__c;
    }

    public void setMV_User1__c(String MV_User1__c) {
        this.MV_User1__c = MV_User1__c;
    }


    public CalenderEvent() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.Unique_Id);
        dest.writeString(this.id);
        dest.writeString(this.description);
        dest.writeString(this.date);
        dest.writeString(this.organization__c);
        dest.writeString(this.MV_User1__c);
        dest.writeString(this.title);
        dest.writeString(this.Event_Time__c);
        dest.writeString(this.Village__c);
        dest.writeString(this.Taluka__c);
        dest.writeString(this.State__c);
        dest.writeString(this.Status);
        dest.writeString(this.Role__c);
        dest.writeString(this.Cluster__c);
        dest.writeString(this.School__c);
        dest.writeString(this.District__c);
        dest.writeString(this.Description_New__c);
        dest.writeString(this.Is_Event_for_All_Role__c);
        dest.writeString(this.Assigned_User_Ids__c);
        dest.writeString(this.Assign_id_name__c);
        dest.writeString(this.MV_Process__c);
        dest.writeString(this.End_Date__c);
        dest.writeString(this.Event_End_Time__c);
        dest.writeString(this.Present_User__c);
    }

    protected CalenderEvent(Parcel in) {
        this.Unique_Id = in.readInt();
        this.id = in.readString();
        this.description = in.readString();
        this.date = in.readString();
        this.organization__c = in.readString();
        this.MV_User1__c = in.readString();
        this.title = in.readString();
        this.Event_Time__c = in.readString();
        this.Village__c = in.readString();
        this.Taluka__c = in.readString();
        this.State__c = in.readString();
        this.Status = in.readString();
        this.Role__c = in.readString();
        this.Cluster__c = in.readString();
        this.School__c = in.readString();
        this.District__c = in.readString();
        this.Description_New__c = in.readString();
        this.Is_Event_for_All_Role__c = in.readString();
        this.Assigned_User_Ids__c = in.readString();
        this.Assign_id_name__c = in.readString();
        this.MV_Process__c = in.readString();
        this.End_Date__c = in.readString();
        this.Event_End_Time__c = in.readString();
        this.Present_User__c = in.readString();
    }

    public static final Creator<CalenderEvent> CREATOR = new Creator<CalenderEvent>() {
        @Override
        public CalenderEvent createFromParcel(Parcel source) {
            return new CalenderEvent(source);
        }

        @Override
        public CalenderEvent[] newArray(int size) {
            return new CalenderEvent[size];
        }
    };
}
