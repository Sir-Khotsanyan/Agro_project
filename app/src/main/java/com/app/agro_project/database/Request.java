package com.app.agro_project.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "requests")
public class Request {

    @PrimaryKey(autoGenerate = true)
    public long requestId;

    @ColumnInfo(name = "requestNameOfProduct")
    public String requestNameOfProduct;

    @ColumnInfo(name="requestWeight")
    public int requestWeight;

    @ColumnInfo(name="requestPrice")
    public int requestPrice;

    @ColumnInfo(name="requestSendDate")
    public String requestSendDate;

    @ColumnInfo(name = "requestCity")
    public String requestCity;

    @ColumnInfo(name="requestUtilWhenUnnecessaryDate")
    public String requestUtilWhenUnnecessaryDate;

    @ColumnInfo(name="firebaseKey")
    public String firebaseKey;
    @ColumnInfo(name = "currentUserName")
    public String currentUserName;

    @ColumnInfo(name = "currentUserEmail")
    public String currentUserEmail;

    public Request() {}

    public Request(String requestNameOfProduct, int requestWeight,int requestPrice, String requestCity ,
                   String requestUtilWhenUnnecessaryDate,String requestSendDate,String currentUserName, String currentUserEmail) {
        this.requestNameOfProduct = requestNameOfProduct;
        this.requestWeight = requestWeight;
        this.requestPrice=requestPrice;
        this.requestCity=requestCity;
        this.requestUtilWhenUnnecessaryDate=requestUtilWhenUnnecessaryDate;
        this.requestSendDate=requestSendDate;
        this.currentUserName=currentUserName;
        this.currentUserEmail=currentUserEmail;
    }
}

