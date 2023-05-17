package com.example.agro_project.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.agro_project.LoginActivity;
import com.example.agro_project.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()!=null){
            String uid= mAuth.getCurrentUser().getUid();
            DatabaseReference userRef= FirebaseDatabase.getInstance().getReference().child("users").child(uid);

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        String username=snapshot.child("username").getValue(String.class);
                        String email=snapshot.child("email").getValue(String.class);

                        binding.profileUsername.setText(username);
                        binding.profileEmail.setText(email);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            Log.e("TAG","Current user is null");
        }

        binding.logoutButton.setOnClickListener(view -> {
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Դուրս գալ...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progressDialog.dismiss();

            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();

        });

        return binding.getRoot();
    }
}