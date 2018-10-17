package com.sujalamsufalam.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sujalamsufalam.Utils.Constants;

import java.io.Serializable;

/**
 * Created by nanostuffs on 03-02-2018.
 */
@Entity(tableName = Constants.TABLE_ADAVANCE)
public class Adavance implements Serializable {


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

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        Amount = amount;
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

    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }

    //adding two fields for advance approved amount facility
    public String getApproved_Amount__c() {
        return Approved_Amount__c;
    }

    public void setApproved_Amount__c(String approved_Amount__c) {
        Approved_Amount__c = approved_Amount__c;
    }

    public String getRemark__c() {
        return Remark__c;
    }

    public void setRemark__c(String remark__c) {
        Remark__c = remark__c;
    }


    @ColumnInfo(name = "Approved_Amount__c")
    @SerializedName("Approved_Amount__c")
    @Expose
    private String Approved_Amount__c;

    @ColumnInfo(name = "Remark__c")
    @SerializedName("Remark__c")
    @Expose
    private String Remark__c;

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
    @SerializedName("Request_Date__c")
    @Expose
    private String Date;

    @ColumnInfo(name = "voucherId")
    @SerializedName("Voucher__c")
    @Expose
    private String voucherId;

    @ColumnInfo(name = "Decription")
    @SerializedName("Description__c")
    @Expose
    private String Decription;

    @ColumnInfo(name = "amount")
    @SerializedName("Amount__c")
    @Expose
    private String Amount;

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }

    @ColumnInfo(name = "User")
    @SerializedName("MV_User__c")
    @Expose
    private String User;

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getRespondDate() {
        return RespondDate;
    }

    public void setRespondDate(String respondDate) {
        RespondDate = respondDate;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public String getApprovalPerson() {
        return ApprovalPerson;
    }

    public void setApprovalPerson(String approvalPerson) {
        ApprovalPerson = approvalPerson;
    }

    @ColumnInfo(name = "Status")
    @SerializedName("Status__c")
    @Expose
    private String Status;

    @ColumnInfo(name = "Respond_Date")
    @SerializedName("Respond_Date__c")
    @Expose
    private String RespondDate;

    @ColumnInfo(name = "Comment")
    @SerializedName("Comment__c")
    @Expose
    private String Comment;

    @ColumnInfo(name = "ApprovalPerson")
    @SerializedName("Approval_Person__c")
    @Expose
    private String ApprovalPerson;

    public Adavance() {
    }


}
