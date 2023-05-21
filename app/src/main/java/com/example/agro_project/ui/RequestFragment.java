package com.example.agro_project.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.agro_project.AgroViewModel;
import com.example.agro_project.R;
import com.example.agro_project.database.Request;
import com.example.agro_project.databinding.FragmentRequestBinding;
import com.example.agro_project.databinding.ItemRecyclerViewBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RequestFragment extends Fragment {
    private FragmentRequestBinding binding;
    private RequestAdapter adapter;
    private AgroViewModel agroViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Calendar selectedDate;

    @SuppressLint("NotifyDataSetChanged")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRequestBinding.inflate(inflater, container, false);

        swipeRefreshLayout = binding.getRoot().findViewById(R.id.swipe_refresh_layout_request);
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        adapter = new RequestAdapter();
        binding.itemListRequest.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.itemListRequest.setAdapter(adapter);

        agroViewModel = new ViewModelProvider(this).get(AgroViewModel.class);
        agroViewModel.getRequests().observe(getViewLifecycleOwner(), requests -> {
            adapter.setData(requests);
            if (!requests.isEmpty()) {
                binding.noResultsText.setVisibility(View.GONE);
                binding.itemListRequest.scrollToPosition(requests.size() - 1);
            } else if (!isInternetConnected()) {
                agroViewModel.readRequest();
            }
        });

        binding.addRequestButton.setOnClickListener(vv -> {
            if (isInternetConnected()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                LayoutInflater layoutInflater = requireActivity().getLayoutInflater();
                View dialogView = layoutInflater.inflate(R.layout.dialog_add_request, null);

                EditText nameEditText = dialogView.findViewById(R.id.edit_text_request);
                EditText weightEditText = dialogView.findViewById(R.id.edit_text_weight);
                EditText priceEditText=dialogView.findViewById(R.id.edit_text_price);
                EditText cityEditText=dialogView.findViewById(R.id.edit_text_city);
                TextView utilWhenUnnecessaryTextView = dialogView.findViewById(R.id.util_when_unnecessary);

                nameEditText.requestFocus();
                Context context = requireContext();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(nameEditText, InputMethodManager.SHOW_IMPLICIT);

                cityEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
                    if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        openDatePicker(utilWhenUnnecessaryTextView);
                        return true;
                    }
                    return false;
                });
                utilWhenUnnecessaryTextView.setOnClickListener(view -> openDatePicker(utilWhenUnnecessaryTextView));

                builder.setView(dialogView)
                        .setTitle("Ավելացնել նոր հայտ")
                        .setPositiveButton("Ուղարկել", null)
                        .setNegativeButton("Չեղարկել", (dialog, which) -> {
                            // Do nothing
                        });

                AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(buttonView -> {
                    String name = nameEditText.getText().toString().trim();
                    String weightString = weightEditText.getText().toString().trim();
                    String priceString=priceEditText.getText().toString().trim();
                    String city=cityEditText.getText().toString().trim();

                    if (name.isEmpty() || weightString.isEmpty()|| priceString.isEmpty()|| city.isEmpty()) {
                        Toast.makeText(requireContext(), "Խնդրում ենք լրացրեք բոլոր տվյալները", Toast.LENGTH_SHORT).show();
                    } else {
                        binding.progressBar.setVisibility(View.VISIBLE);
                        int weight = Integer.parseInt(weightString);
                        int price=Integer.parseInt(priceString);
                        String formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.getTime());
                        agroViewModel.addRequest(name, weight,price,city,formattedDate);
                        dialog.dismiss();
                    }
                });
            } else {
                Toast.makeText(requireContext(), "Ցանցին միացում չկա", Toast.LENGTH_SHORT).show();
            }
        });

        agroViewModel.readRequest();
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    private void refreshData() {
        if (isInternetConnected()) {
            agroViewModel.readRequest();
        } else {
            Toast.makeText(requireContext(), "Ցանցին միացում չկա", Toast.LENGTH_SHORT).show();
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
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

                List<Request> filterRequest = new ArrayList<>();
                List<Request> requests = agroViewModel.getRequests().getValue();
                if (requests != null) {
                    for (Request request : requests) {
                        if (request.requestNameOfProduct.toLowerCase().contains(newText.toLowerCase())) {
                            filterRequest.add(request);
                        }
                    }
                }
                adapter.setData(filterRequest);
                if (filterRequest.isEmpty()) {
                    binding.noResultsText.setVisibility(View.VISIBLE);
                } else {
                    binding.noResultsText.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }
    @SuppressLint("ResourceAsColor")
    private void openDatePicker(TextView utilWhenUnnecessaryTextView){
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
        }, year, month, day);

        datePickerDialog.setTitle("Ընտրեք, թե մինչև երբ է անհրաժեշտ ");
        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Հաստատել", datePickerDialog);
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Չեղարկել", datePickerDialog);

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private class RequestAdapter extends RecyclerView.Adapter<RequestFragment.RequestAdapter.ViewHolder> {
        private List<Request> requests;

        @SuppressLint("NotifyDataSetChanged")
        public void setData(List<Request> requests) {
            this.requests = requests;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RequestFragment.RequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemRecyclerViewBinding binding = ItemRecyclerViewBinding.inflate(inflater, parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.binding.nameOfProduct.setText(requests.get(position).requestNameOfProduct);
            holder.binding.productWeight.setText(String.valueOf(requests.get(position).requestWeight));
            holder.binding.productPrice.setText(String.valueOf(requests.get(position).requestPrice));
            holder.binding.city.setText(String.valueOf(requests.get(position).requestCity));
            holder.binding.sendDate.setText(requests.get(position).requestSendDate);
        }

        @Override
        public int getItemCount() {
            return requests != null ? requests.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemRecyclerViewBinding binding;

            public ViewHolder(@NonNull ItemRecyclerViewBinding binding) {
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