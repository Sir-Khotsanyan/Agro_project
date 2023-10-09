package com.app.agro_project.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.app.agro_project.AgroViewModel;
import com.app.agro_project.R;
import com.app.agro_project.database.Offer;
import com.app.agro_project.databinding.FragmentOfferBinding;
import com.app.agro_project.databinding.ItemRecyclerViewBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class OfferFragment extends Fragment {
    private FragmentOfferBinding binding;
    private OfferAdapter adapter;
    private AgroViewModel agroViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Calendar selectedDate;
    private Uri imagePath;
    private ImageView imageView;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private String offerImageUrl;

    @SuppressLint("NotifyDataSetChanged")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentOfferBinding.inflate(inflater, container, false);

        swipeRefreshLayout = binding.getRoot().findViewById(R.id.swipe_refresh_layout_offer);
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        adapter = new OfferAdapter();
        binding.itemListOffer.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.itemListOffer.setAdapter(adapter);

        agroViewModel = new ViewModelProvider(this).get(AgroViewModel.class);
        if (!isInternetConnected()) {
            showNoInternetDialog();
        } else {
            binding.progressBar.setVisibility(View.VISIBLE);
            agroViewModel.getOffers().observe(getViewLifecycleOwner(), offers -> {
                adapter.setData(offers);
                if (!offers.isEmpty()) {
                    binding.noResultsText.setVisibility(View.GONE);
                    binding.itemListOffer.scrollToPosition(offers.size() - 1);
                }
                binding.progressBar.setVisibility(View.GONE);
            });
        }

        binding.addOfferButton.setOnClickListener(vv -> {
            if (isInternetConnected()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                LayoutInflater layoutInflater = requireActivity().getLayoutInflater();
                View dialogView = layoutInflater.inflate(R.layout.dialog_add_offer, null);

                EditText nameEditText = dialogView.findViewById(R.id.edit_text_offer);
                EditText weightEditText = dialogView.findViewById(R.id.edit_text_weight);
                EditText priceEditText = dialogView.findViewById(R.id.edit_text_price);
                EditText cityEditText = dialogView.findViewById(R.id.edit_text_city);
                TextView utilWhenUnnecessaryTextView = dialogView.findViewById(R.id.util_when_unnecessary);
                imageView = dialogView.findViewById(R.id.offer_image);
                nameEditText.requestFocus();

                imageView.setOnClickListener(view -> {
                    imagePickerLauncher.launch(createImagePickerIntent());
                    uploadImage();
                });

                cityEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
                    if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        openDatePicker(utilWhenUnnecessaryTextView);
                        return true;
                    }
                    return false;
                });

                utilWhenUnnecessaryTextView.setOnClickListener(view -> openDatePicker(utilWhenUnnecessaryTextView));

                builder.setView(dialogView)
                        .setTitle("Ավելացնել նոր առաջարկ")
                        .setPositiveButton("Ուղարկել", null)
                        .setNegativeButton("Չեղարկել", (dialog, which) -> {
                            //Do nothing
                        });

                AlertDialog dialog = builder.create();

                dialog.setOnShowListener(dialogInterface -> {
                    nameEditText.requestFocus();
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                });

                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(buttonView -> {
                    String name = nameEditText.getText().toString().trim().toLowerCase();
                    String weightString = weightEditText.getText().toString().trim();
                    String priceString = priceEditText.getText().toString().trim();
                    String city = cityEditText.getText().toString().trim();
                    city = city.substring(0, 1).toUpperCase() + city.substring(1).toLowerCase();

                    if (name.isEmpty() || weightString.isEmpty() || priceString.isEmpty() || city.isEmpty()) {
                        Toast.makeText(requireContext(), "Խնդրում ենք լրացրեք բոլոր տվյալները", Toast.LENGTH_SHORT).show();
                    } else {
                        int weight = Integer.parseInt(weightString);
                        int price = Integer.parseInt(priceString);
                        String formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.getTime());
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        String currentUserName = Objects.requireNonNull(firebaseUser).getDisplayName();
                        String currentUserEmail = firebaseUser.getEmail();
                        agroViewModel.addOffer(name, weight, price, city, formattedDate, offerImageUrl, currentUserName, currentUserEmail);
                        dialog.dismiss();
                    }
                });
            } else {
                Toast.makeText(requireContext(), "Ցանցին միացում չկա", Toast.LENGTH_SHORT).show();
            }
        });

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            imagePath = data.getData();
                            getImageInImageView(imagePath);
                        }
                    }
                });

        agroViewModel.readOffer();
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    private Intent createImagePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        return intent;
    }

    private void refreshData() {
        if (isInternetConnected()) {
            agroViewModel.readOffer();
        } else {
            showNoInternetDialog();
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Որոնել․․․");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                List<Offer> filterOffer = new ArrayList<>();
                List<Offer> offers = agroViewModel.getOffers().getValue();
                if (offers != null) {
                    for (Offer offer : offers) {
                        if (offer.offerNameOfProduct.toLowerCase().contains(newText.toLowerCase())) {
                            filterOffer.add(offer);
                        }
                    }
                }
                adapter.setData(filterOffer);
                if (filterOffer.isEmpty()) {
                    binding.noResultsText.setVisibility(View.VISIBLE);
                } else {
                    binding.noResultsText.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }

    private void openDatePicker(TextView utilWhenUnnecessaryTextView) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(), R.style.CustomDatePickerDialog,
                (datePicker, year1, month1, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(Calendar.YEAR, year1);
                    selectedCalendar.set(Calendar.MONTH, month1);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    selectedDate = selectedCalendar;

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String currentDateString = dateFormat.format(selectedCalendar.getTime());
                    utilWhenUnnecessaryTextView.setText(currentDateString);

                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(utilWhenUnnecessaryTextView.getWindowToken(), 0);
                }, year, month, day);

        datePickerDialog.setTitle("Նշեք պիտանելիության ժամկետը");
        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Հաստատել", datePickerDialog);
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Չեղարկել", datePickerDialog);

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void getImageInImageView(Uri imagePath) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);
        uploadImage();
    }

    private void uploadImage() {
        if (imagePath == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage("Վերբեռնում...")
                .setCancelable(false);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        String imageName = UUID.randomUUID().toString();

        FirebaseStorage.getInstance().getReference("images/" + imageName)
                .putFile(imagePath)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseStorage.getInstance().getReference("images/" + imageName)
                                .getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    offerImageUrl = uri.toString();
                                    Toast.makeText(requireActivity(), "Նկարը հաջողությամբ վերբեռնվեց", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(requireActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(requireActivity(), Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                    alertDialog.dismiss();
                });
    }

    private void showNoInternetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Ցանցին միացում չկա")
                .setMessage("Խնդրում ենք ստուգել ձեր կապը համացանցին և նորից փորձել։")
                .setPositiveButton("Նորից փորձել", (dialog, which) -> {
                    if (isInternetConnected()) {
                        dialog.dismiss();
                    } else {
                        showNoInternetDialog();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.ViewHolder> {
        private List<Offer> offers;

        @SuppressLint("NotifyDataSetChanged")
        public void setData(List<Offer> offers) {
            this.offers = offers;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemRecyclerViewBinding binding = ItemRecyclerViewBinding.inflate(inflater, parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final Offer offer = offers.get(position);

            holder.binding.nameOfProduct.setText(offers.get(position).offerNameOfProduct);
            holder.binding.productWeight.setText(String.valueOf(offers.get(position).offerWeight));
            holder.binding.productPrice.setText(String.valueOf(offers.get(position).offerPrice));
            holder.binding.city.setText(String.valueOf(offers.get(position).offerCity));
            holder.binding.sendDate.setText(offers.get(position).offerSendDate);
            holder.binding.userName.setText(offers.get(position).currentUserName);

            holder.itemView.setOnClickListener(v -> showItemDetails(holder.itemView, offer));

        }

        @Override
        public int getItemCount() {
            return offers != null ? offers.size() : 0;
        }

        private void showItemDetails(View itemView, Offer offer) {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            LayoutInflater layoutInflater = requireActivity().getLayoutInflater();
            View dialogView = layoutInflater.inflate(R.layout.dialog_display_offer, null);

            TextView offerName = dialogView.findViewById(R.id.offer_name);
            TextView offerWeight = dialogView.findViewById(R.id.offer_weight);
            TextView offerPrice = dialogView.findViewById(R.id.offer_price);
            TextView offerUtilWhenUnnecessaryTitle = dialogView.findViewById(R.id.offer_util_when_unnecessary);
            TextView offerCity = dialogView.findViewById(R.id.offer_city);
            TextView offerSendDate = dialogView.findViewById(R.id.offer_send_date);
            TextView offerUsename = dialogView.findViewById(R.id.offer_username);
            TextView offerUserEmail = dialogView.findViewById(R.id.offer_user_email);
            ImageView offerImage = dialogView.findViewById(R.id.offer_image);
            ProgressBar progressBar = dialogView.findViewById(R.id.progress_bar);

            offerName.setText(offer.offerNameOfProduct);
            offerWeight.setText(String.valueOf(offer.offerWeight));
            offerPrice.setText(String.valueOf(offer.offerPrice));
            offerUtilWhenUnnecessaryTitle.setText(offer.offerUtilWhenUnnecessaryDate);
            offerCity.setText(offer.offerCity);
            offerSendDate.setText(offer.offerSendDate);
            offerUsename.setText(offer.currentUserName);
            offerUserEmail.setText(offer.currentUserEmail);

            if (offer.offerImageUrl == null || offer.offerImageUrl.isEmpty()) {
                offerImage.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            } else {
                offerImage.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                Picasso.get().load(offer.offerImageUrl).into(offerImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            builder.setView(dialogView)
                    .setNegativeButton("Փակել", (dialog, which) -> {
                        //Do nothing
                    });

            offerUserEmail.setOnClickListener(v -> {
                String subject = offer.offerNameOfProduct + " " + offer.offerWeight + "կգ(" + offer.offerSendDate + ")";
                String recipientEmail = offer.currentUserEmail;
                openMailWindow(subject, recipientEmail);
            });

            offerUsename.setOnClickListener(v -> {
                String subject = offer.offerNameOfProduct + " " + offer.offerWeight + "կգ(" + offer.offerSendDate + ")";
                String recipientEmail = offer.currentUserEmail;
                openMailWindow(subject, recipientEmail);
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void openMailWindow(String subject, String address){
            Intent intent=new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("mailto:?subject="+subject+"&to="+address));
            startActivity(Intent.createChooser(intent,"Նամակն ուղարկել․․․"));
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemRecyclerViewBinding binding;

            public ViewHolder(@NonNull ItemRecyclerViewBinding binding) {
                super(binding.getRoot());
                this.binding = binding;

            }
        }
    }
}
