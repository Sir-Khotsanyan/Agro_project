package com.example.agro_project.database;

import android.content.Context;

import androidx.room.Room;

public class DatabaseClient {
   private static final String DB_NAME = "Agro_9.db";
   private static DatabaseClient instance;
   private final AppDatabase appDatabase;

   private DatabaseClient(Context context) {
      appDatabase = Room.databaseBuilder(context, AppDatabase.class, DB_NAME).build();
   }

   public static synchronized DatabaseClient getInstance(Context context) {
      if (instance == null) {
         instance = new DatabaseClient(context);
      }
      return instance;
   }

   public AppDatabase getAppDatabase() {
      return appDatabase;
   }
}
