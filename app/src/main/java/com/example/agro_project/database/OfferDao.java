package com.example.agro_project.database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface OfferDao {
    @Insert
    void insert(Offer offer);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Offer> offers);

    @Delete
    void deleteOffer(Offer offer);

    @Query("SELECT * FROM offers")
    List<Offer> getAllOffers();

    @Query("DELETE FROM offers")
    void deleteAllOffers();

}
