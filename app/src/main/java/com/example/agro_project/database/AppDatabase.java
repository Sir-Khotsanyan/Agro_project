package com.example.agro_project.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Request.class,Offer.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RequestDao requestDao();
    public abstract OfferDao offerDao();
}
