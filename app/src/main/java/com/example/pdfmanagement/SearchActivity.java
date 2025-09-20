package com.example.pdfmanagement;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageView btnBack;
    private RecyclerView rvSearchResults;
    private FilesAdapter adapter;
    private List<FileItem> fullList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        rvSearchResults = findViewById(R.id.rvSearchResults);

        // load full list from repository
        fullList = FileRepository.allFiles;

        // setup adapter with empty list at first
        adapter = new FilesAdapter(this, new ArrayList<>());
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(adapter);

        // hide list initially
        rvSearchResults.setVisibility(View.GONE);

        // back button
        btnBack.setOnClickListener(v -> finish());

        // search input
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String query) {
        if (fullList == null) return;

        if (query.isEmpty()) {
            // hide recycler view when nothing typed
            rvSearchResults.setVisibility(View.GONE);
            adapter.updateList(new ArrayList<>());
            return;
        }

        // filter list
        List<FileItem> filtered = new ArrayList<>();
        for (FileItem f : fullList) {
            if (f.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(f);
            }
        }

        // show results only when search is not empty
        rvSearchResults.setVisibility(View.VISIBLE);
        adapter.updateList(filtered);
    }
}
