package com.mv.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Report {

//    @SerializedName("attributes")
//    @Expose
//    private Attributes attributes;

    @SerializedName("Id")
    @Expose
    private String id;

    @SerializedName("Name")
    @Expose
    private String name;

    @SerializedName("Report_Name__c")
    @Expose
    private String reportNameC;

    @SerializedName("Category__c")
    @Expose
    private String categoryC;

    @SerializedName("Role__c")
    @Expose
    private String roleC;

    @SerializedName("Tableau_Link__c")
    @Expose
    private String tableauLinkC;

    @SerializedName("is_Active__c")
    @Expose
    private Boolean isActiveC;

    @SerializedName("Project__c")
    @Expose
    private String projectC;

//    public Attributes getAttributes() {
//        return attributes;
//    }
//
//    public void setAttributes(Attributes attributes) {
//        this.attributes = attributes;
//    }

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

    public String getReportNameC() {
        return reportNameC;
    }

    public void setReportNameC(String reportNameC) {
        this.reportNameC = reportNameC;
    }

    public String getCategoryC() {
        return categoryC;
    }

    public void setCategoryC(String categoryC) {
        this.categoryC = categoryC;
    }

    public String getRoleC() {
        return roleC;
    }

    public void setRoleC(String roleC) {
        this.roleC = roleC;
    }

    public String getTableauLinkC() {
        return tableauLinkC;
    }

    public void setTableauLinkC(String tableauLinkC) {
        this.tableauLinkC = tableauLinkC;
    }

    public Boolean getIsActiveC() {
        return isActiveC;
    }

    public void setIsActiveC(Boolean isActiveC) {
        this.isActiveC = isActiveC;
    }

    public String getProjectC() {
        return projectC;
    }

    public void setProjectC(String projectC) {
        this.projectC = projectC;
    }
}
