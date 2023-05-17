package com.example.agro_project;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.agro_project.database.AppDatabase;
import com.example.agro_project.database.DatabaseClient;
import com.example.agro_project.database.Offer;
import com.example.agro_project.database.Request;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AgroViewModel extends AndroidViewModel {
    private final AppDatabase appDatabase;
    private final MutableLiveData<List<Request>> requests;
    private final MutableLiveData<List<Offer>> offers;
    private boolean isRefreshing;
    private boolean isOffering;

    public AgroViewModel(@NonNull Application application) {
        super(application);
        requests = new MutableLiveData<>();
        offers = new MutableLiveData<>();
        appDatabase = DatabaseClient.getInstance(getApplication()).getAppDatabase();
        isRefreshing = false;
        isOffering = false;
    }

    public LiveData<List<Request>> getRequests() {
        return requests;
    }


    public LiveData<List<Offer>> getOffers() {
        return offers;
    }

    public void addRequest(String name, int weight, int price, String city, String utilWhenUnnecessaryDate) {
        if (!TextUtils.isEmpty(name) || weight != 0 || price != 0 || !TextUtils.isEmpty(utilWhenUnnecessaryDate)) {
            AsyncTask.execute(() -> {
                @SuppressLint({"NewApi", "LocalSuppress"}) LocalDate currentDate = LocalDate.now();
                @SuppressLint({"NewApi", "LocalSuppress"}) DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                @SuppressLint({"NewApi", "LocalSuppress"}) String formatDate = currentDate.format(formatter);
                String sendDate = String.valueOf(formatDate);
                Request request = new Request(name, weight, price, city, utilWhenUnnecessaryDate, sendDate);
                appDatabase.requestDao().insert(request);
                refreshRequestList();
                addRequestToFirebase(request);
            });
        } else {
            Toast.makeText(getApplication(), "Նոր հայտ չի ներմուծվել", Toast.LENGTH_SHORT).show();
        }
    }

    public void addRequestToFirebase(Request request) {
        AsyncTask.execute(() -> {
            DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("requests");
            String requestId = requestRef.push().getKey();
            request.firebaseKey = requestId;
            requestRef.child(Objects.requireNonNull(requestId)).setValue(request);
        });
    }

    public void addOffer(String name, int weight, int price, String city, String utilWhenUnnecessaryDate) {
        if (!TextUtils.isEmpty(name) || weight != 0 || price != 0 || !TextUtils.isEmpty(utilWhenUnnecessaryDate)) {
            AsyncTask.execute(() -> {
                @SuppressLint({"NewApi", "LocalSuppress"}) LocalDate currentDate = LocalDate.now();
                @SuppressLint({"NewApi", "LocalSuppress"}) DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                @SuppressLint({"NewApi", "LocalSuppress"}) String formatDate = currentDate.format(formatter);
                String sendDate = String.valueOf(formatDate);
                Offer offer = new Offer(name, weight, price, city, utilWhenUnnecessaryDate, sendDate);
                appDatabase.offerDao().insert(offer);
                refreshOfferList();
                addOfferToFirebase(offer);
            });
        } else {
            Toast.makeText(getApplication(), "Նոր առաջարկ չի ներմուծվել", Toast.LENGTH_SHORT).show();
        }
    }

    public void addOfferToFirebase(Offer offer) {
        AsyncTask.execute(() -> {
            DatabaseReference offerRef = FirebaseDatabase.getInstance().getReference("offers");
            String offerId = offerRef.push().getKey();
            offer.firebaseKey = offerId;
            offerRef.child(Objects.requireNonNull(offerId)).setValue(offer);
        });
    }

    public void deleteRequest(Request request) {
        AsyncTask.execute(() -> {
            appDatabase.requestDao().deleteRequest(request);
            deleteRequestFromFirebase(request.firebaseKey);
            refreshRequestList();
        });
    }

    public void deleteRequestFromFirebase(String firebaseKey) {
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("requests").child(firebaseKey);
        requestRef.removeValue().addOnSuccessListener(unused -> {
            Toast.makeText(getApplication(), "Հայտը ջնջված է", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e("FirebaseDeleteError", "Error deleting request from Firebase", e);
        });
    }

    public void deleteOffer(Offer offer) {
        AsyncTask.execute(() -> {
            appDatabase.offerDao().deleteOffer(offer);
            deleteOfferFromFirebase(offer.firebaseKey);
            refreshOfferList();
        });
    }

    public void deleteOfferFromFirebase(String firebaseKey) {
        DatabaseReference offerRef = FirebaseDatabase.getInstance().getReference("offers").child(firebaseKey);
        offerRef.removeValue().addOnSuccessListener(unused -> {
            Toast.makeText(getApplication(), "Առաջարկը ջնջված է", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e("FirebaseDeleteError", "Error deleting request from Firebase", e);
        });
    }

    private void refreshRequestList() {
        AsyncTask.execute(() -> {
            if (isRefreshing) {
                return;
            }
            isRefreshing = true;
            List<Request> updateList = appDatabase.requestDao().getAllRequests();
            requests.postValue(updateList);
            isRefreshing = false;
        });
    }

    private void refreshOfferList() {
        AsyncTask.execute(() -> {
            if (isOffering) {
                return;
            }
            isOffering = true;
            List<Offer> updateList = appDatabase.offerDao().getAllOffers();
            offers.postValue(updateList);
            isOffering = false;
        });
    }

    public void readRequest() {

        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("requests");
        requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Request> requests = new ArrayList<>();
                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    Request request = requestSnapshot.getValue(Request.class);
                    if (request != null) {
                        requests.add(request);
                    }
                }
                AsyncTask.execute(() -> {
                    appDatabase.requestDao().deleteAllRequests();
                    appDatabase.requestDao().insertAll(requests);
                    refreshRequestList();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "Error reading requests from Firebase", error.toException());
            }
        });
    }


    public void readOffer() {
        DatabaseReference offerRef = FirebaseDatabase.getInstance().getReference("offers");
        offerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Offer> offers = new ArrayList<>();
                for (DataSnapshot offerSnapshot : snapshot.getChildren()) {
                    Offer offer = offerSnapshot.getValue(Offer.class);
                    if (offer != null) {
                        offers.add(offer);
                    }
                }
                AsyncTask.execute(() -> {
                    appDatabase.offerDao().deleteAllOffers();
                    appDatabase.offerDao().insertAll(offers);
                    refreshOfferList();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "Error reading offers from Firebase", error.toException());
            }
        });
    }
}
