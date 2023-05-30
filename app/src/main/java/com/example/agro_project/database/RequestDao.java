package com.example.agro_project.database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface RequestDao {
    @Insert
    void insert(Request request);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Request> requests);

    @Delete
    void deleteRequest(Request request);

    @Query("SELECT * FROM requests")
    List<Request> getAllRequests();

    @Query("DELETE FROM requests")
    void deleteAllRequests();

}
