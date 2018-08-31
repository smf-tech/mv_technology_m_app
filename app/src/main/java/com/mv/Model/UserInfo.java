package com.mv.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by nanostuffs on 20-02-2018.
 */

public class UserInfo {

    @SerializedName("Id")
    @Expose
    private String Id = "";
    @SerializedName("Project__c")
    @Expose
    private String Project__c = "";
    @SerializedName("Multi_project__c")
    @Expose
    private String Multi_project__c = "";
    @SerializedName("Project_Name__c")
    @Expose
    private String Project_Name__c = "";
    @SerializedName("Name")
    @Expose
    private String Name = "";
    @SerializedName("User_Email__c")
    @Expose
    private String Email = "";
    @SerializedName("User_Cluster__c")
    @Expose
    private String Cluster = "";
    @SerializedName("User_District__c")
    @Expose
    private String District = "";
    @SerializedName("User_Password__c")
    @Expose
    private String Password = "";
    @SerializedName("User_Mobile_No__c")
    @Expose
    private String Phone = "";
    @SerializedName("Role_Name__c")
    @Expose
    private String Roll = "";
    @SerializedName("User_SchoolID__c")
    @Expose
    private String School_Code = "";
    @SerializedName("UserSchoolName__c")
    @Expose
    private String School_Name = "";
    @SerializedName("User_Taluka__c")
    @Expose
    private String Taluka = "";
    @SerializedName("User_Multiple_Taluka__c")
    @Expose
    private String MultipleTaluka = "";
    @SerializedName("Birth_Day__c")
    @Expose
    private String Birth_Day__c;
    @SerializedName("User_Village__c")
    @Expose
    private String Village = "";
    @SerializedName("User_State__c")
    @Expose
    private String State = "";
    @SerializedName("Middle_Name__c")
    @Expose
    private String MiddleName = "";
    @SerializedName("Last_Name__c")
    @Expose
    private String LastName = "";
    @SerializedName("ImageId__c")
    @Expose
    private String ImageId = "";
    @SerializedName("Role_Organization__c")
    @Expose
    private String Organisation = "";
    @SerializedName("Is_Approved__c")
    @Expose
    private String isApproved = "";
    @SerializedName("Mobile_Tab_Name_c__c")
    @Expose
    private String tabNameApproved = "";
    @SerializedName("Before_Approved_Tab_Names__c")
    @Expose
    private String tabNameNoteApproved = "";
    @SerializedName("Attendance_Loc__Latitude__s")
    @Expose
    private String Attendance_Loc_Lat = "";
    @SerializedName("Attendance_Loc__Longitude__s")
    @Expose
    private String Attendance_Loc_Lng = "";
    @SerializedName("Approval_Role_c__c")
    @Expose
    private String approval_role = "";
    @SerializedName("Gender__c")
    @Expose
    private String gender = "";
    @SerializedName("Approver_Comment__c")
    @Expose
    private String Approver_Comment__c = "";
    @SerializedName("Role_Juridiction__c")
    @Expose
    private String Role_Juridiction__c = "";
    @SerializedName("PhoneID__c")
    @Expose
    private String PhoneId = "";
    @SerializedName("User_Mobile_App_Version__c")
    @Expose
    private String User_Mobile_App_Version__c = "";
    @SerializedName("isLocationTrackingAllow__c")
    @Expose
    private String isLocationTrackingAllow__c;
    @SerializedName("isLocationAllow__c")
    @Expose
    private String isLocationAllow__c;
    @SerializedName("Contact_No__c")
    @Expose
    private String Contact_No__c = "";
    @SerializedName("Hangout_URL__c")
    @Expose
    private String Hangout_URL__c = "";
    @SerializedName("Hide_Role_On_Calendar__c")
    @Expose
    private String Hide_Role_On_Calendar__c = "";
    @SerializedName("Languges__c")
    @Expose
    private String Languges__c;
    @SerializedName("Bank_Account_Number__c")
    @Expose
    private String Bank_Account_Number__c;
    @SerializedName("Bank_Name__c")
    @Expose
    private String Bank_Name__c;
    @SerializedName("Employee_Id__c")
    @Expose
    private String Employee_Id__c;
    @SerializedName("IFSC_Code__c")
    @Expose
    private String IFSC_Code__c;
    @SerializedName("UAN_Number__c")
    @Expose
    private String UAN_Number__c;
    @SerializedName("PF_Number__c")
    @Expose
    private String PF_Number__c;
    @SerializedName("User_Address__c")
    @Expose
    private String User_Address__c;

    public String getMultipleTaluka() {
        return MultipleTaluka;
    }

    public void setMultipleTaluka(String MultipleTaluka) {
        MultipleTaluka = MultipleTaluka;
    }

    public String getBirth_Day__c() {
        return Birth_Day__c;
    }

    public void setBirth_Day__c(String birth_Day__c) {
        Birth_Day__c = birth_Day__c;
    }

    public String getAttendance_Loc_Lat() {
        return Attendance_Loc_Lat;
    }

    public void setAttendance_Loc_Lat(String attendance_Loc_Lat) {
        Attendance_Loc_Lat = attendance_Loc_Lat;
    }

    public String getAttendance_Loc_Lng() {
        return Attendance_Loc_Lng;
    }

    public void setAttendance_Loc_Lng(String attendance_Loc_Lng) {
        Attendance_Loc_Lng = attendance_Loc_Lng;
    }

    public String getUser_Mobile_App_Version__c() {
        return User_Mobile_App_Version__c;
    }

    public void setUser_Mobile_App_Version__c(String user_Mobile_App_Version__c) {
        User_Mobile_App_Version__c = user_Mobile_App_Version__c;
    }

    public String getApprover_Comment__c() {
        return Approver_Comment__c;
    }

    public void setApprover_Comment__c(String approver_Comment__c) {
        Approver_Comment__c = approver_Comment__c;
    }

    public String getRole_Juridiction__c() {
        return Role_Juridiction__c;
    }

    public void setRole_Juridiction__c(String role_Juridiction__c) {
        Role_Juridiction__c = role_Juridiction__c;
    }

    public String getPhoneId() {
        return PhoneId;
    }

    public void setPhoneId(String phoneId) {
        PhoneId = phoneId;
    }



    public String getUserMobileAppVersion() {
        return User_Mobile_App_Version__c;
    }

    public void setUserMobileAppVersion(String user_Mobile_App_Version__c) {
        User_Mobile_App_Version__c = user_Mobile_App_Version__c;
    }

    public String getIsLocationTrackingAllow__c() {
        return isLocationTrackingAllow__c;
    }

    public void setIsLocationTrackingAllow__c(String isLocationTrackingAllow__c) {
        this.isLocationTrackingAllow__c = isLocationTrackingAllow__c;
    }

    public String getIsLocationAllow__c() {
        return isLocationAllow__c;
    }

    public void setIsLocationAllow__c(String isLocationAllow__c) {
        this.isLocationAllow__c = isLocationAllow__c;
    }

    public String getContact_No__c() {
        return Contact_No__c;
    }

    public void setContact_No__c(String contact_No__c) {
        Contact_No__c = contact_No__c;
    }

    public String getHangout_URL__c() {
        return Hangout_URL__c;
    }

    public void setHangout_URL__c(String hangout_URL__c) {
        Hangout_URL__c = hangout_URL__c;
    }

    public String getLanguges__c() {
        return Languges__c;
    }

    public void setLanguges__c(String languges__c) {
        Languges__c = languges__c;
    }

    public String getHide_Role_On_Calendar__c() {
        return Hide_Role_On_Calendar__c;
    }

    public void setHide_Role_On_Calendar__c(String hide_Role_On_Calendar__c) {
        Hide_Role_On_Calendar__c = hide_Role_On_Calendar__c;
    }


    public String getPF_Number__c() {
        return PF_Number__c;
    }

    public void setPF_Number__c(String PF_Number__c) {
        this.PF_Number__c = PF_Number__c;
    }


    public String getBank_Account_Number__c() {
        return Bank_Account_Number__c;
    }

    public void setBank_Account_Number__c(String bank_Account_Number__c) {
        Bank_Account_Number__c = bank_Account_Number__c;
    }

    public String getBank_Name__c() {
        return Bank_Name__c;
    }

    public void setBank_Name__c(String bank_Name__c) {
        Bank_Name__c = bank_Name__c;
    }

    public String getEmployee_Id__c() {
        return Employee_Id__c;
    }

    public void setEmployee_Id__c(String employee_Id__c) {
        Employee_Id__c = employee_Id__c;
    }

    public String getIFSC_Code__c() {
        return IFSC_Code__c;
    }

    public void setIFSC_Code__c(String IFSC_Code__c) {
        this.IFSC_Code__c = IFSC_Code__c;
    }

    public String getUAN_Number__c() {
        return UAN_Number__c;
    }

    public void setUAN_Number__c(String UAN_Number__c) {
        this.UAN_Number__c = UAN_Number__c;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProject__c() {
        return Project__c;
    }

    public void setProject__c(String project__c) {
        Project__c = project__c;
    }

    public String getMulti_project__c() {
        return Multi_project__c;
    }

    public void setMulti_project__c(String multi_project__c) {
        Multi_project__c = multi_project__c;
    }

    public String getProject_Name__c() {
        return Project_Name__c;
    }

    public void setProject_Name__c(String project_Name__c) {
        Project_Name__c = project_Name__c;
    }

    public String getTabNameApproved() {
        return tabNameApproved;
    }

    public void setTabNameApproved(String tabNameApproved) {
        this.tabNameApproved = tabNameApproved;
    }

    public String getTabNameNoteApproved() {
        return tabNameNoteApproved;
    }

    public void setTabNameNoteApproved(String tabNameNoteApproved) {
        this.tabNameNoteApproved = tabNameNoteApproved;
    }

    public String getApproval_role() {
        return approval_role;
    }

    public void setApproval_role(String approval_role) {
        this.approval_role = approval_role;
    }

    public String getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(String isApproved) {
        this.isApproved = isApproved;
    }

    public String getOrganisation() {
        return Organisation;
    }

    public void setOrganisation(String organisation) {
        Organisation = organisation;
    }

    public String getImageId() {
        return ImageId;
    }

    public void setImageId(String imageId) {
        ImageId = imageId;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getMiddleName() {
        return MiddleName;
    }

    public void setMiddleName(String middleName) {
        MiddleName = middleName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
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

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getCluster() {
        return Cluster;
    }

    public void setCluster(String cluster) {
        Cluster = cluster;
    }

    public String getDistrict() {
        return District;
    }

    public void setDistrict(String district) {
        District = district;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getRoll() {
        return Roll;
    }

    public void setRoll(String roll) {
        Roll = roll;
    }

    public String getSchool_Code() {
        return School_Code;
    }

    public void setSchool_Code(String school_Code) {
        School_Code = school_Code;
    }

    public String getSchool_Name() {
        return School_Name;
    }

    public void setSchool_Name(String school_Name) {
        School_Name = school_Name;
    }

    public String getTaluka() {
        return Taluka;
    }

    public void setTaluka(String taluka) {
        Taluka = taluka;
    }

    public String getVillage() {
        return Village;
    }

    public void setVillage(String village) {
        Village = village;
    }

    public String getUser_Address__c() {
        return User_Address__c;
    }

    public void setUser_Address__c(String user_Address__c) {
        User_Address__c = user_Address__c;
    }
}
