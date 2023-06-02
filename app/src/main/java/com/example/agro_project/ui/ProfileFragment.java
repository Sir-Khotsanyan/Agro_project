package com.example.agro_project.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.agro_project.CurrentUserOffersActivity;
import com.example.agro_project.CurrentUserRequestsActivity;
import com.example.agro_project.LoginActivity;
import com.example.agro_project.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {
    private Handler handler;
    private AlertDialog alertDialog;
    private boolean isLoggingOut = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentProfileBinding binding = FragmentProfileBinding.inflate(inflater, container, false);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String username = user.getDisplayName();
            String gmail = user.getEmail();

            binding.profileUsername.setText(username);
            binding.profileEmail.setText(gmail);
        }

        binding.currentRequests.setOnClickListener(view -> {
            Intent intent=new Intent(requireActivity(), CurrentUserRequestsActivity.class);
            startActivity(intent);
        });

        binding.currentOffers.setOnClickListener(view -> {
            Intent intent=new Intent(requireActivity(), CurrentUserOffersActivity.class);
            startActivity(intent);
        });

        binding.logoutButton.setOnClickListener(view -> {
            if (isLoggingOut) {
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setMessage("Դուրս գալ...");

            alertDialog = builder.create();

            alertDialog.setOnShowListener(dialog -> {
                TextView messageTextView = alertDialog.findViewById(android.R.id.message);
                if (messageTextView != null) {
                    messageTextView.setTextColor(Color.RED);
                }
            });

            alertDialog.show();

            isLoggingOut = true;
            handler = new Handler();
            handler.postDelayed(() -> {
                alertDialog.dismiss();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                startActivity(intent);
                isLoggingOut = false;
            }, 2000);
        });

        return binding.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }
}

