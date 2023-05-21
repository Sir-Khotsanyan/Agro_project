package com.example.agro_project.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "offers")
public class Offer {
    @PrimaryKey(autoGenerate = true)
    public int offerId;

    @ColumnInfo(name = "offerNameOfProduct")
    public String offerNameOfProduct;

    @ColumnInfo(name="offerWeight")
    public int offerWeight;

    @ColumnInfo(name="offerPrice")
    public int offerPrice;

    @ColumnInfo(name="offerSendDate")
    public String offerSendDate;

    @ColumnInfo(name = "offerCity")
    public String offerCity;

    @ColumnInfo(name="offerUtilWhenUnnecessaryDate")
    public String offerUtilWhenUnnecessaryDate;

    public Offer(){}

    @ColumnInfo(name="firebaseKey")
    public String firebaseKey;

    public Offer(String offerNameOfProduct, int offerWeight, int offerPrice, String offerCity, String offerUtilWhenUnnecessaryDate,String offerSendDate ) {
        this.offerNameOfProduct = offerNameOfProduct;
        this.offerWeight = offerWeight;
        this.offerPrice = offerPrice;
        this.offerSendDate = offerSendDate;
        this.offerCity = offerCity;
        this.offerUtilWhenUnnecessaryDate = offerUtilWhenUnnecessaryDate;
    }
}