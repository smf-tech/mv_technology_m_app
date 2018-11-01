package com.mv.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by user on 3/8/2018.
 */

public class Asset implements Serializable {
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("isReturnable")
    @Expose
    private boolean isReturnable;

    @SerializedName("availableQuantity")
    @Expose
    private String availableQuantity;

    @SerializedName("Allocation_Quantity__c")
    @Expose
    private String allocationQuantityC;
    @SerializedName("Expected_Issue_Date__c")
    @Expose
    private String expectedIssueDateC;

    @SerializedName("AssetID__c")
    @Expose
    private String assetIDC;

    @SerializedName("Id")
    @Expose
    private String Asset_id;

    @SerializedName("AllocatedBy__c")
    @Expose
    private String allocatedByC;
    @SerializedName("Allocated_Time__c")
    @Expose
    private String allocatedTimeC;
    @SerializedName("Allocation_Status__c")
    @Expose
    private String allocationStatusC;

    @SerializedName("ASSET_STOCK__c")
    @Expose
    private String aSSETSTOCKC;
    @SerializedName("Priority__c")
    @Expose
    private String priorityC;
    @SerializedName("Release_Time__c")
    @Expose
    private String releaseTimeC;

    @SerializedName("expectedIssueDate")
    @Expose
    private String expectedIssueDate;
    @SerializedName("assetName")
    @Expose
    private String assetName;
    @SerializedName("assetModel")
    @Expose
    private String assetModel;
    @SerializedName("assetId")
    @Expose
    private String assetId;
    @SerializedName("assetCount")
    @Expose
    private String assetCount;
    @SerializedName("assetAllocationId")
    @Expose
    private String assetAllocationId;
    @SerializedName("allocationStatus")
    @Expose
    private String allocationStatus;

    @SerializedName("specification")
    @Expose
    private String specification;


    public String getTentativeReturnDate() {
        return tentativeReturnDate;
    }

    public void setTentativeReturnDate(String tentativeReturnDate) {
        this.tentativeReturnDate = tentativeReturnDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @SerializedName("tentativeReturnDate")
    @Expose
    private String tentativeReturnDate;

    @SerializedName("remark")
    @Expose
    private String remark;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("stockId")
    @Expose
    private String stockId;

    @SerializedName("modelNo")
    @Expose
    private String modelNo;
    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("Last_Name__c")
    @Expose
    private String Last_Name__c;


    public String getExpectedIssueDate() {
        return expectedIssueDate;
    }

    public void setExpectedIssueDate(String expectedIssueDate) {
        this.expectedIssueDate = expectedIssueDate;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }


    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public void setAssetCount(String assetCount) {
        this.assetCount = assetCount;
    }

    public String getAssetCount() {
        return assetCount;
    }

    public String getAssetAllocationId() {
        return assetAllocationId;
    }

    public void setAssetAllocationId(String assetAllocationId) {
        this.assetAllocationId = assetAllocationId;
    }

    public String getAllocationStatus() {
        return allocationStatus;
    }

    public void setAllocationStatus(String allocationStatus) {
        this.allocationStatus = allocationStatus;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIsReturnable() {
        return isReturnable;
    }

    public void setIsReturnable(boolean isReturnable) {
        this.isReturnable = isReturnable;
    }


    public boolean isReturnable() {
        return isReturnable;
    }

    public void setReturnable(boolean returnable) {
        isReturnable = returnable;
    }


    public String getExpectedIssueDateC() {
        return expectedIssueDateC;
    }

    public void setExpectedIssueDateC(String expectedIssueDateC) {
        this.expectedIssueDateC = expectedIssueDateC;
    }

    public String getAssetIDC() {
        return assetIDC;
    }

    public void setAssetIDC(String assetIDC) {
        this.assetIDC = assetIDC;
    }

    public String getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(String availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getAllocationQuantityC() {
        return allocationQuantityC;
    }

    public void setAllocationQuantityC(String allocationQuantityC) {
        this.allocationQuantityC = allocationQuantityC;
    }

    public String getAllocatedByC() {
        return allocatedByC;
    }

    public void setAllocatedByC(String allocatedByC) {
        this.allocatedByC = allocatedByC;
    }

    public String getAllocatedTimeC() {
        return allocatedTimeC;
    }

    public void setAllocatedTimeC(String allocatedTimeC) {
        this.allocatedTimeC = allocatedTimeC;
    }

    public String getAllocationStatusC() {
        return allocationStatusC;
    }

    public void setAllocationStatusC(String allocationStatusC) {
        this.allocationStatusC = allocationStatusC;
    }

    public String getaSSETSTOCKC() {
        return aSSETSTOCKC;
    }

    public void setaSSETSTOCKC(String aSSETSTOCKC) {
        this.aSSETSTOCKC = aSSETSTOCKC;
    }

    public String getPriorityC() {
        return priorityC;
    }

    public void setPriorityC(String priorityC) {
        this.priorityC = priorityC;
    }

    public String getReleaseTimeC() {
        return releaseTimeC;
    }

    public void setReleaseTimeC(String releaseTimeC) {
        this.releaseTimeC = releaseTimeC;
    }

    public String getAsset_id() {
        return Asset_id;
    }

    public void setAsset_id(String asset_id) {
        Asset_id = asset_id;
    }

    public String getAssetModel() {
        return assetModel;
    }

    public void setAssetModel(String assetModel) {
        this.assetModel = assetModel;
    }


    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getStockId() {
        return stockId;
    }

    public void setStockId(String stockId) {
        this.stockId = stockId;
    }

    public String getModelNo() {
        return modelNo;
    }

    public void setModelNo(String modelNo) {
        this.modelNo = modelNo;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLast_Name__c() {
        return Last_Name__c;
    }

    public void setLast_Name__c(String last_Name__c) {
        Last_Name__c = last_Name__c;
    }
}
