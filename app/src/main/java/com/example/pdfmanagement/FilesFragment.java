package com.example.pdfmanagement;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FilesFragment extends Fragment {

    private static final int REQ_READ = 100;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private FilesAdapter adapter;
    private List<FileItem> allFiles = new ArrayList<>();

    private LinearLayout permissionLayout;
    private Button btnGrantPermission;
    private int currentTabIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        tabLayout = view.findViewById(R.id.tabLayout);
        recyclerView = view.findViewById(R.id.recyclerView);
        fab = view.findViewById(R.id.fab);
        permissionLayout = view.findViewById(R.id.permissionLayout);
        btnGrantPermission = view.findViewById(R.id.btnGrantPermission);

        // Toolbar menu
        toolbar.setTitle("All PDF Reader");
        toolbar.inflateMenu(R.menu.menu_files_appbar);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_search) {
                Intent intent = new Intent(getContext(), SearchActivity.class);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.action_sort) { Toast.makeText(getContext(), "Sort clicked", Toast.LENGTH_SHORT).show(); return true; }
            else if (id == R.id.action_select) { Toast.makeText(getContext(), "Select clicked", Toast.LENGTH_SHORT).show(); return true; }
            else if (id == R.id.action_settings) { Toast.makeText(getContext(), "Settings clicked", Toast.LENGTH_SHORT).show(); return true; }
            return false;
        });

        // Tabs
        String[] tabs = {"All", "PDF", "Word", "Excel", "PPT"};
        for (String t : tabs) tabLayout.addTab(tabLayout.newTab().setText(t));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                currentTabIndex = tab.getPosition();
                applyFilter(tab.getText().toString());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) { applyFilter(tab.getText().toString()); }
        });

        // RecyclerView + adapter
        adapter = new FilesAdapter(getContext(), new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> Toast.makeText(getContext(), "Import / Add clicked", Toast.LENGTH_SHORT).show());

        btnGrantPermission.setOnClickListener(v -> openPermissionSettings());

        checkPermissionAndLoad();

        return view;
    }

    private void openPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_READ);
        }
    }

    private void checkPermissionAndLoad() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                showFiles();
            } else {
                showPermissionNotice();
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                showFiles();
            } else {
                showPermissionNotice();
            }
        }
    }

    private void showPermissionNotice() {
        permissionLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        // show demo files here
        List<FileItem> demoList = Arrays.asList(
                new FileItem("Demo PDF.pdf", "", "08/29/2025 • 120 KB", "pdf", "application/pdf"),
                new FileItem("Demo DOCX.docx", "", "08/29/2025 • 19 KB", "doc", "application/msword"),
                new FileItem("Demo Excel.xlsx", "", "08/29/2025 • 45 KB", "xls", "application/vnd.ms-excel"),
                new FileItem("Demo PPT.pptx", "", "08/29/2025 • 250 KB", "ppt", "application/vnd.ms-powerpoint")
        );
        adapter.updateList(demoList);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showFiles() {
        permissionLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        loadFilesFromDevice();
    }

    @Override
    public void onResume() {
        super.onResume();
        // When coming back from Settings
        checkPermissionAndLoad();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_READ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showFiles();
            } else {
                Toast.makeText(getContext(), "Permission required to read files", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadFilesFromDevice() {
        ExecutorService exe = Executors.newSingleThreadExecutor();
        exe.execute(() -> {
            List<FileItem> list = new ArrayList<>();

            Uri collection = MediaStore.Files.getContentUri("external");
            String[] projection = {
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.DATE_MODIFIED
            };

            String selection = MediaStore.Files.FileColumns.MIME_TYPE + "=? OR "
                    + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR "
                    + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR "
                    + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR "
                    + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR "
                    + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR "
                    + MediaStore.Files.FileColumns.MIME_TYPE + "=?";
            String[] selectionArgs = new String[] {
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.ms-powerpoint",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            };
            String sort = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC";

            Cursor cursor = null;
            try {
                cursor = requireContext().getContentResolver().query(collection, projection, selection, selectionArgs, sort);
                if (cursor != null) {
                    int idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                    int nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                    int sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                    int mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);
                    int dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED);

                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(idCol);
                        String name = cursor.getString(nameCol);
                        long size = cursor.getLong(sizeCol);
                        String mime = cursor.getString(mimeCol);
                        long dateModified = cursor.getLong(dateCol);

                        Uri contentUri = ContentUris.withAppendedId(collection, id);
                        String meta = formatDate(dateModified) + " • " + Formatter.formatShortFileSize(requireContext(), size);
                        String type = detectTypeFromMimeOrName(mime, name);

                        list.add(new FileItem(name, contentUri.toString(), meta, type, mime));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) cursor.close();
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    allFiles = list;
                    FileRepository.allFiles = list;
                    applyFilter(tabLayout.getTabAt(currentTabIndex).getText().toString());
                });
            }
        });
    }

    private String detectTypeFromMimeOrName(String mime, String name) {
        if (mime == null) mime = "";
        String lower = name == null ? "" : name.toLowerCase(Locale.getDefault());
        if (mime.contains("pdf") || lower.endsWith(".pdf")) return "pdf";
        if (mime.contains("word") || lower.endsWith(".doc") || lower.endsWith(".docx")) return "doc";
        if (mime.contains("spreadsheet") || lower.endsWith(".xls") || lower.endsWith(".xlsx")) return "xls";
        if (mime.contains("powerpoint") || lower.endsWith(".ppt") || lower.endsWith(".pptx")) return "ppt";
        return "pdf";
    }

    private String formatDate(long seconds) {
        long ms = seconds * 1000L;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        return sdf.format(new Date(ms));
    }

    private void applyFilter(String tab) {
        if (allFiles == null) return;
        if (tab.equals("All")) {
            adapter.updateList(allFiles);
            return;
        }
        String expectedType = "";
        switch (tab) {
            case "PDF": expectedType = "pdf"; break;
            case "Word": expectedType = "doc"; break;
            case "Excel": expectedType = "xls"; break;
            case "PPT": expectedType = "ppt"; break;
        }
        List<FileItem> filtered = new ArrayList<>();
        for (FileItem f : allFiles) {
            if (expectedType.equals("") || f.getType().equals(expectedType)) filtered.add(f);
        }
        adapter.updateList(filtered);
    }
}
