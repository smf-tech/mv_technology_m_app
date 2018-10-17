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
@Entity(tableName = Constants.TABLE_SALARY)
public class Salary implements Serializable {


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

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getMonth() {
        return Month;
    }

    public void setMonth(String month) {
        Month = month;
    }

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        Amount = amount;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "unique_Id")
    private int UniqueId;

    @ColumnInfo(name = "Id")
    @SerializedName("Id")
    @Expose
    private String Id;

    @ColumnInfo(name = "SalaryDate")
    @SerializedName("Salary_Date__c")
    @Expose
    private String Date;

    @ColumnInfo(name = "SalaryMonth")
    @SerializedName("Salary_Month__c")
    @Expose
    private String Month;

    @ColumnInfo(name = "Amount")
    @SerializedName("Salary_Amount__c")
    @Expose
    private String Amount;

    public Salary() {
    }

    @ColumnInfo(name = "Consolidated_Basic__c")
    @SerializedName("Consolidated_Basic__c")
    @Expose
    private String Consolidated_Basic__c;

    @ColumnInfo(name = "MV_User__c")
    @SerializedName("MV_User__c")
    @Expose
    private String MV_User__c;

    @ColumnInfo(name = "User_Full_Name__c")
    @SerializedName("User_Full_Name__c")
    @Expose
    private String Name;

    public String getTelephone_Expense__c() {
        return Telephone_Expense__c;
    }

    public void setTelephone_Expense__c(String telephone_Expense__c) {
        Telephone_Expense__c = telephone_Expense__c;
    }

    @ColumnInfo(name = "Telephone_Expense__c")
    @SerializedName("Telephone_Expense__c")
    @Expose
    private String Telephone_Expense__c;


    public String getNet_Salary__c() {
        return Net_Salary__c;
    }

    public void setNet_Salary__c(String net_Salary__c) {
        Net_Salary__c = net_Salary__c;
    }

    @ColumnInfo(name = "Net_Salary__c")
    @SerializedName("Net_Salary__c")
    @Expose
    private String Net_Salary__c;


    @ColumnInfo(name = "Perks__c")
    @SerializedName("Perks__c")
    @Expose
    private String Perks__c;

    @ColumnInfo(name = "IsDeleted")
    @SerializedName("IsDeleted")
    @Expose
    private String IsDeleted;

    @ColumnInfo(name = "Arrears__c")
    @SerializedName("Arrears__c")
    @Expose
    private String Arrears__c;

    @ColumnInfo(name = "Medical_Allowance__c")
    @SerializedName("Medical_Allowance__c")
    @Expose
    private String Medical_Allowance__c;

    @ColumnInfo(name = "Special_Allowance__c")
    @SerializedName("Special_Allowance__c")
    @Expose
    private String Special_Allowance__c;

    @ColumnInfo(name = "Conveyance_Allowance__c")
    @SerializedName("Conveyance_Allowance__c")
    @Expose
    private String Conveyance_Allowance__c;

    @ColumnInfo(name = "HRA__c")
    @SerializedName("HRA__c")
    @Expose
    private String HRA__c;

    @ColumnInfo(name = "Security_Fund__c")
    @SerializedName("Security_Fund__c")
    @Expose
    private String Security_Fund__c;

    @ColumnInfo(name = "Salary_Advance__c")
    @SerializedName("Salary_Advance__c")
    @Expose
    private String Salary_Advance__c;

    @ColumnInfo(name = "TDS__c")
    @SerializedName("TDS__c")
    @Expose
    private String TDS__c;

    @ColumnInfo(name = "Profession_Tax__c")
    @SerializedName("Profession_Tax__c")
    @Expose
    private String Profession_Tax__c;

    @ColumnInfo(name = "Provident_Fund__c")
    @SerializedName("Provident_Fund__c")
    @Expose
    private String Provident_Fund__c;

    @ColumnInfo(name = "Total_Amount_to_Bank__c")
    @SerializedName("Total_Amount_to_Bank__c")
    @Expose
    private String Total_Amount_to_Bank__c;

    @ColumnInfo(name = "Total_Reimbursement__c")
    @SerializedName("Total_Reimbursement__c")
    @Expose
    private String Total_Reimbursement__c;

    @ColumnInfo(name = "Gross_Earning_for_the_Month__c")
    @SerializedName("Gross_Earning_for_the_Month__c")
    @Expose
    private String Gross_Earning_for_the_Month__c;

    @ColumnInfo(name = "Any_other__c")
    @SerializedName("Any_other__c")
    @Expose
    private String Any_other__c;

    @ColumnInfo(name = "Travelling_Exp__c")
    @SerializedName("Travelling_Exp__c")
    @Expose
    private String Travelling_Exp__c;

    @ColumnInfo(name = "Appontment_Allowance__c")
    @SerializedName("Appontment_Allowance__c")
    @Expose
    private String Appontment_Allowance__c;

    @ColumnInfo(name = "House_Rent__c")
    @SerializedName("House_Rent__c")
    @Expose
    private String House_Rent__c;


    @ColumnInfo(name = "Total_Deductions__c")
    @SerializedName("Total_Deductions__c")
    @Expose
    private String Total_Deductions__c;

    @ColumnInfo(name = "Unpaid_Leaves__c")
    @SerializedName("Unpaid_Leaves__c")
    @Expose
    private String Unpaid_Leaves__c;

    @ColumnInfo(name = "Paid_Leaves__c")
    @SerializedName("Paid_Leaves__c")
    @Expose
    private String Paid_Leaves__c;

    @ColumnInfo(name = "Present_Days__c")
    @SerializedName("Present_Days__c")
    @Expose
    private String Present_Days__c;

    @ColumnInfo(name = "Total_Leave_Deduction__c")
    @SerializedName("Total_Leave_Deduction__c")
    @Expose
    private String Total_Leave_Deduction__c;


    public String getConsolidated_Basic__c() {
        return Consolidated_Basic__c;
    }

    public void setConsolidated_Basic__c(String consolidated_Basic__c) {
        Consolidated_Basic__c = consolidated_Basic__c;
    }

    public String getMV_User__c() {
        return MV_User__c;
    }

    public void setMV_User__c(String MV_User__c) {
        this.MV_User__c = MV_User__c;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPerks__c() {
        return Perks__c;
    }

    public void setPerks__c(String perks__c) {
        Perks__c = perks__c;
    }

    public String getIsDeleted() {
        return IsDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        IsDeleted = isDeleted;
    }

    public String getArrears__c() {
        return Arrears__c;
    }

    public void setArrears__c(String arrears__c) {
        Arrears__c = arrears__c;
    }

    public String getMedical_Allowance__c() {
        return Medical_Allowance__c;
    }

    public void setMedical_Allowance__c(String medical_Allowance__c) {
        Medical_Allowance__c = medical_Allowance__c;
    }

    public String getSpecial_Allowance__c() {
        return Special_Allowance__c;
    }

    public void setSpecial_Allowance__c(String special_Allowance__c) {
        Special_Allowance__c = special_Allowance__c;
    }

    public String getConveyance_Allowance__c() {
        return Conveyance_Allowance__c;
    }

    public void setConveyance_Allowance__c(String conveyance_Allowance__c) {
        Conveyance_Allowance__c = conveyance_Allowance__c;
    }

    public String getHRA__c() {
        return HRA__c;
    }

    public void setHRA__c(String HRA__c) {
        this.HRA__c = HRA__c;
    }

    public String getSecurity_Fund__c() {
        return Security_Fund__c;
    }

    public void setSecurity_Fund__c(String security_Fund__c) {
        Security_Fund__c = security_Fund__c;
    }

    public String getSalary_Advance__c() {
        return Salary_Advance__c;
    }

    public void setSalary_Advance__c(String salary_Advance__c) {
        Salary_Advance__c = salary_Advance__c;
    }

    public String getTDS__c() {
        return TDS__c;
    }

    public void setTDS__c(String TDS__c) {
        this.TDS__c = TDS__c;
    }

    public String getProfession_Tax__c() {
        return Profession_Tax__c;
    }

    public void setProfession_Tax__c(String profession_Tax__c) {
        Profession_Tax__c = profession_Tax__c;
    }

    public String getProvident_Fund__c() {
        return Provident_Fund__c;
    }

    public void setProvident_Fund__c(String provident_Fund__c) {
        Provident_Fund__c = provident_Fund__c;
    }

    public String getTotal_Amount_to_Bank__c() {
        return Total_Amount_to_Bank__c;
    }

    public void setTotal_Amount_to_Bank__c(String total_Amount_to_Bank__c) {
        Total_Amount_to_Bank__c = total_Amount_to_Bank__c;
    }

    public String getTotal_Reimbursement__c() {
        return Total_Reimbursement__c;
    }

    public void setTotal_Reimbursement__c(String total_Reimbursement__c) {
        Total_Reimbursement__c = total_Reimbursement__c;
    }

    public String getGross_Earning_for_the_Month__c() {
        return Gross_Earning_for_the_Month__c;
    }

    public void setGross_Earning_for_the_Month__c(String gross_Earning_for_the_Month__c) {
        Gross_Earning_for_the_Month__c = gross_Earning_for_the_Month__c;
    }

    public String getAny_other__c() {
        return Any_other__c;
    }

    public void setAny_other__c(String any_other__c) {
        Any_other__c = any_other__c;
    }

    public String getTravelling_Exp__c() {
        return Travelling_Exp__c;
    }

    public void setTravelling_Exp__c(String travelling_Exp__c) {
        Travelling_Exp__c = travelling_Exp__c;
    }

    public String getAppontment_Allowance__c() {
        return Appontment_Allowance__c;
    }

    public void setAppontment_Allowance__c(String appontment_Allowance__c) {
        Appontment_Allowance__c = appontment_Allowance__c;
    }

    public String getHouse_Rent__c() {
        return House_Rent__c;
    }

    public void setHouse_Rent__c(String house_Rent__c) {
        House_Rent__c = house_Rent__c;
    }

    public String getTotal_Deductions__c() {
        return Total_Deductions__c;
    }

    public void setTotal_Deductions__c(String total_Deductions__c) {
        Total_Deductions__c = total_Deductions__c;
    }

    public String getUnpaid_Leaves__c() {
        return Unpaid_Leaves__c;
    }

    public void setUnpaid_Leaves__c(String unpaid_Leaves__c) {
        Unpaid_Leaves__c = unpaid_Leaves__c;
    }

    public String getPaid_Leaves__c() {
        return Paid_Leaves__c;
    }

    public void setPaid_Leaves__c(String paid_Leaves__c) {
        Paid_Leaves__c = paid_Leaves__c;
    }

    public String getPresent_Days__c() {
        return Present_Days__c;
    }

    public void setPresent_Days__c(String present_Days__c) {
        Present_Days__c = present_Days__c;
    }

    public String getTotal_Leave_Deduction__c() {
        return Total_Leave_Deduction__c;
    }

    public void setTotal_Leave_Deduction__c(String total_Leave_Deduction__c) {
        Total_Leave_Deduction__c = total_Leave_Deduction__c;
    }

    public String getAbsent_Days__c() {
        return Absent_Days__c;
    }

    public void setAbsent_Days__c(String absent_Days__c) {
        Absent_Days__c = absent_Days__c;
    }

    @ColumnInfo(name = "Absent_Days__c")
    @SerializedName("Absent_Days__c")
    @Expose
    private String Absent_Days__c;


    public String getTelephone_Expense__Allowance() {
        return Telephone_Expense__Allowance;
    }
    public void setTelephone_Expense__Allowance(String telephone_Expense__Allowance) {
        Telephone_Expense__Allowance = telephone_Expense__Allowance;
    }
    @ColumnInfo(name = "Telephone_Expense_Allowance__c")
    @SerializedName("Telephone_Expense_Allowance__c")
    @Expose
    private String Telephone_Expense__Allowance;


    public String getSecurity_Fund__Allowance() {
        return Security_Fund__Allowance;
    }
    public void setSecurity_Fund__Allowance(String security_Fund__Allowance) {
        Security_Fund__Allowance = security_Fund__Allowance;
    }
    @ColumnInfo(name = "Security_Fund_Allowance__c")
    @SerializedName("Security_Fund_Allowance__c")
    @Expose
    private String Security_Fund__Allowance;


    public String getOther_Deductions__c() {
        return Other_Deductions__c;
    }

    public void setOther_Deductions__c(String other_Deductions__c) {
        Other_Deductions__c = other_Deductions__c;
    }

    @ColumnInfo(name = "Other_Deductions__c")
    @SerializedName("Other_Deductions__c")
    @Expose
    private String Other_Deductions__c;


    public String getTotal_Amount_to_Bank_Net_Salary_Reimbur__c() {
        return Total_Amount_to_Bank_Net_Salary_Reimbur__c;
    }
    public void setTotal_Amount_to_Bank_Net_Salary_Reimbur__c(String total_Amount_to_Bank_Net_Salary_Reimbur__c) {
        Total_Amount_to_Bank_Net_Salary_Reimbur__c = total_Amount_to_Bank_Net_Salary_Reimbur__c;
    }
    @ColumnInfo(name = "Total_Amount_to_Bank_Net_Salary_Reimbur__c")
    @SerializedName("Total_Amount_to_Bank_Net_Salary_Reimbur__c")
    @Expose
    private String Total_Amount_to_Bank_Net_Salary_Reimbur__c;


}
