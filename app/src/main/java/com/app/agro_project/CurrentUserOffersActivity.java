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

import com.app.agro_project.database.Offer;
import com.app.agro_project.databinding.ItemRecyclerVewCurrentUserInputsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CurrentUserOffersActivity extends AppCompatActivity {
    private AgroViewModel agroViewModel;
    private CurrentUserOffersActivity.CurrentOfferAdapter adapter;
    private TextView noOfferText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_user_offers);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Իմ առաջարկները");

        noOfferText = findViewById(R.id.no_offer_text);
        noOfferText.setVisibility(View.GONE);
        ProgressBar progressBar = findViewById(R.id.progress_bar);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_offers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new CurrentUserOffersActivity.CurrentOfferAdapter();
        recyclerView.setAdapter(adapter);

        agroViewModel = new ViewModelProvider(this).get(AgroViewModel.class);
        progressBar.setVisibility(View.VISIBLE);

        agroViewModel.getOffers().observe(this, offers -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String currentEmail = Objects.requireNonNull(user).getEmail();
            adapter.setData(offers, currentEmail);
            updateNoOfferVisibility(offers);
            progressBar.setVisibility(View.GONE);
        });

        agroViewModel.readOffer();
    }
    private void updateNoOfferVisibility(List<Offer> offers) {
        if (!offers.isEmpty()) {
            noOfferText.setVisibility(View.GONE);
        } else {
            noOfferText.setVisibility(View.VISIBLE);
        }
    }
    private class CurrentOfferAdapter extends RecyclerView.Adapter<CurrentUserOffersActivity.CurrentOfferAdapter.ViewHolder> {
        private List<Offer> offers;

        @SuppressLint("NotifyDataSetChanged")
        public void setData(List<Offer> offers, String currentEmail) {
            this.offers = new ArrayList<>();
            for (Offer offer : offers) {
                if (offer.currentUserEmail.equals(currentEmail)) {
                    this.offers.add(offer);
                }
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CurrentUserOffersActivity.CurrentOfferAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemRecyclerVewCurrentUserInputsBinding binding = ItemRecyclerVewCurrentUserInputsBinding.inflate(inflater, parent, false);
            return new CurrentUserOffersActivity.CurrentOfferAdapter.ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull CurrentUserOffersActivity.CurrentOfferAdapter.ViewHolder holder, int position) {

            holder.binding.nameOfProduct.setText(offers.get(position).offerNameOfProduct);
            holder.binding.productWeight.setText(String.valueOf(offers.get(position).offerWeight));
            holder.binding.productPrice.setText(String.valueOf(offers.get(position).offerPrice));
            holder.binding.city.setText(String.valueOf(offers.get(position).offerCity));
            holder.binding.sendDate.setText(offers.get(position).offerSendDate);
            holder.binding.userName.setText(offers.get(position).currentUserName);

        }

        @Override
        public int getItemCount() {
            return offers != null ? offers.size() : 0;
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
                            .setPositiveButton("Ջնջել", (dialog, id) -> agroViewModel.deleteOffer(offers.get(getAbsoluteAdapterPosition())))
                            .setNegativeButton("Չեղարկել", (dialog, id) -> dialog.dismiss());
                    builder.create().show();
                });
            }
        }
    }
}