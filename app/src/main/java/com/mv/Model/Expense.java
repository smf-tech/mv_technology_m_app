package com.mv.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mv.Utils.Constants;

import java.io.Serializable;

/**
 * Created by nanostuffs on 03-02-2018.
 */
@Entity(tableName = Constants.TABLE_EXPENSE)
public class Expense implements Serializable {


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

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "unique_Id")
    private int UniqueId;

    @ColumnInfo(name = "Id")
    @SerializedName("Id")
    @Expose
    private String Id;

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        Amount = amount;
    }

    @ColumnInfo(name = "Amount")
    @SerializedName("Amount")
    @Expose
    private String Amount;

    @ColumnInfo(name = "Date")
    @SerializedName("Date")
    @Expose
    private String Date;

    @ColumnInfo(name = "Decription")
    @SerializedName("Decription")
    @Expose
    private String Decription;

    @ColumnInfo(name = "voucherId")
    @SerializedName("voucherId")
    @Expose
    private String voucherId;

    public String getPartuculars() {
        return partuculars;
    }

    public void setPartuculars(String partuculars) {
        this.partuculars = partuculars;
    }

    @ColumnInfo(name = "partuculars")
    @SerializedName("partuculars")
    @Expose
    private String partuculars;

    public Expense() {
    }


}
