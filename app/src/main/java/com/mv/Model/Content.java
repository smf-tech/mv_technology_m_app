package com.mv.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mv.Utils.Constants;

import java.io.Serializable;

/**
 * Created by Rohit Gujar on 13-09-2017.
 */
@Entity(tableName = Constants.TABLE_CONTENT)
public class Content implements Serializable {


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "unique_Id")
    private int Unique_Id;



    @ColumnInfo(name = "isAttachmentPresent")
    @SerializedName("isAttachmentPresent")
    @Expose
    String isAttachmentPresent;

    @ColumnInfo(name = "synchStatus")
    String synchStatus;
    @ColumnInfo(name = "Id")
    @SerializedName("Id")
    @Expose
    String id;
    @ColumnInfo(name = "UserId")
    @SerializedName("UserId")
    @Expose
    String user_id;
    @ColumnInfo(name = "CommunityId")
    @SerializedName("CommunityId")
    @Expose
    String community_id;
    @ColumnInfo(name = "District")
    @SerializedName("District")
    @Expose
    String district;
    @ColumnInfo(name = "taluka")
    @SerializedName("taluka")
    @Expose
    String taluka;

    @ColumnInfo(name = "likeCount")
    @SerializedName("likeCount")
    @Expose
    int likeCount;
    @ColumnInfo(name = "commentCount")
    @SerializedName("commentCount")
    @Expose
    int commentCount;
    @ColumnInfo(name = "isLike")
    @SerializedName("isLike")
    @Expose
    Boolean isLike;
    @ColumnInfo(name = "Issue_Type")
    @SerializedName("Issue_Type")
    @Expose
    String issue_type;
    @ColumnInfo(name = "Report_Type")
    @SerializedName("Report_Type")
    @Expose
    String reporting_type;
    @ColumnInfo(name = "Priority")
    @SerializedName("Priority")
    @Expose
    String issue_priority;
    @ColumnInfo(name = "Title")
    @SerializedName("Title")
    @Expose
    String title;
    @ColumnInfo(name = "Description")
    @SerializedName("Description")
    @Expose
    String description;
    @ColumnInfo(name = "TemplateId")
    @SerializedName("TemplateId")
    @Expose
    String template;


    @ColumnInfo(name = "TemplateName")
    String templateName;
    @ColumnInfo(name = "userAttachmentId")
    @SerializedName("userAttachmentId")
    @Expose
    String userAttachmentId;
    @ColumnInfo(name = "CreatedDate")
    @SerializedName("CreatedDate")
    @Expose
    String time;
    @ColumnInfo(name = "isbroadcast")
    @SerializedName("isbroadcast")
    @Expose
    String isBroadcast;


    @ColumnInfo(name = "userName")
    @SerializedName("userName")
    @Expose
    String userName;


    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getIsTheatMessage() {
        return isTheatMessage;
    }

    public void setIsTheatMessage(String isTheatMessage) {
        this.isTheatMessage = isTheatMessage;
    }

    @ColumnInfo(name = "contentType")
    @SerializedName("contentType")
    @Expose
    String contentType;

    @ColumnInfo(name = "isTheatMessage")
    @SerializedName("isTheatMessage")
    @Expose
    String isTheatMessage;
    public String getIsAttachmentPresent() {
        return isAttachmentPresent;
    }

    public void setIsAttachmentPresent(String isAttachmentPresent) {
        this.isAttachmentPresent = isAttachmentPresent;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSynchStatus() {
        return synchStatus;
    }

    public void setSynchStatus(String synchStatus) {
        this.synchStatus = synchStatus;
    }

    public String getIsBroadcast() {
        return isBroadcast;
    }

    public void setIsBroadcast(String isBroadcast) {
        this.isBroadcast = isBroadcast;
    }

    public int getUnique_Id() {
        return Unique_Id;
    }

    public void setUnique_Id(int unique_Id) {
        Unique_Id = unique_Id;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }


    public Boolean getIsLike() {
        return isLike;
    }

    public void setIsLike(Boolean isLike) {
        this.isLike = isLike;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUserAttachmentId() {
        return userAttachmentId;
    }

    public void setUserAttachmentId(String userAttachmentId) {
        this.userAttachmentId = userAttachmentId;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    @SerializedName("attachmentId")
    @Expose
    String attachmentId;

    public String getCommunity_id() {
        return community_id;
    }

    public void setCommunity_id(String community_id) {
        this.community_id = community_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getTaluka() {
        return taluka;
    }

    public void setTaluka(String taluka) {
        this.taluka = taluka;
    }

    public String getIssue_type() {
        return issue_type;
    }

    public void setIssue_type(String issue_type) {
        this.issue_type = issue_type;
    }

    public String getReporting_type() {
        return reporting_type;
    }

    public void setReporting_type(String reporting_type) {
        this.reporting_type = reporting_type;
    }

    public String getIssue_priority() {
        return issue_priority;
    }

    public void setIssue_priority(String issue_priority) {
        this.issue_priority = issue_priority;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

}
