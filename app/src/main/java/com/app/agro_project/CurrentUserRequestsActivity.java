package com.app.agro_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.agro_project.database.Request;
import com.app.agro_project.databinding.ItemRecyclerVewCurrentUserInputsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CurrentUserRequestsActivity extends AppCompatActivity {

    private AgroViewModel agroViewModel;
    private CurrentRequestAdapter adapter;
    private TextView noRequestText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_user_requests);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Իմ հայտերը");

        noRequestText = findViewById(R.id.no_request_text);
        noRequestText.setVisibility(View.GONE);
        ProgressBar progressBar = findViewById(R.id.progress_bar);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_requests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new CurrentRequestAdapter();
        recyclerView.setAdapter(adapter);

        agroViewModel = new ViewModelProvider(this).get(AgroViewModel.class);
        progressBar.setVisibility(View.VISIBLE);

        agroViewModel.getRequests().observe(this, requests -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String currentEmail = Objects.requireNonNull(user).getEmail();
            adapter.setData(requests, currentEmail);
            updateNoRequestVisibility(requests);
            progressBar.setVisibility(View.GONE);
        });

        agroViewModel.readRequest();
    }
    private void updateNoRequestVisibility(List<Request> requests) {
        if (!requests.isEmpty()) {
            noRequestText.setVisibility(View.GONE);
        } else {
            noRequestText.setVisibility(View.VISIBLE);
        }
    }

    private class CurrentRequestAdapter extends RecyclerView.Adapter<CurrentRequestAdapter.ViewHolder> {
        private List<Request> requests;

        @SuppressLint("NotifyDataSetChanged")
        public void setData(List<Request> requests, String currentEmail) {
            this.requests = new ArrayList<>();
            for (Request request : requests) {
                if (request.currentUserEmail.equals(currentEmail)) {
                    this.requests.add(request);
                }
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CurrentRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemRecyclerVewCurrentUserInputsBinding binding = ItemRecyclerVewCurrentUserInputsBinding.inflate(inflater, parent, false);
            return new CurrentRequestAdapter.ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull CurrentRequestAdapter.ViewHolder holder, int position) {

            holder.binding.nameOfProduct.setText(requests.get(position).requestNameOfProduct);
            holder.binding.productWeight.setText(String.valueOf(requests.get(position).requestWeight));
            holder.binding.productPrice.setText(String.valueOf(requests.get(position).requestPrice));
            holder.binding.city.setText(String.valueOf(requests.get(position).requestCity));
            holder.binding.sendDate.setText(requests.get(position).requestSendDate);
            holder.binding.userName.setText(requests.get(position).currentUserName);

        }

        @Override
        public int getItemCount() {
            return requests != null ? requests.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemRecyclerVewCurrentUserInputsBinding binding;

            public ViewHolder(@NonNull ItemRecyclerVewCurrentUserInputsBinding binding) {
                super(binding.getRoot());
                this.binding = binding;

                binding.messageDelete.setOnClickListener(view -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                    builder.setMessage("Վստա՞հ եք, որ ուզում եք ջնջել")
                            .setTitle("Ջնջել")
                            .setPositiveButton("Ջնջել", (dialog, id) -> agroViewModel.deleteRequest(requests.get(getAbsoluteAdapterPosition())))
                            .setNegativeButton("Չեղարկել", (dialog, id) -> dialog.dismiss());
                    builder.create().show();
                });
            }
        }
    }
}