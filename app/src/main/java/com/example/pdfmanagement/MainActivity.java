package com.example.pdfmanagement;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);

        // load default fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new FilesFragment())
                .commit();

        // Handle bottom navigation item clicks
        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_files) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new FilesFragment()).commit();
                    return true;
                } else if (item.getItemId() == R.id.nav_recent) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new RecentFragment()).commit();
                    return true;
                } else if (item.getItemId() == R.id.nav_bookmarks) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new BookmarksFragment()).commit();
                    return true;
                } else if (item.getItemId() == R.id.nav_tools) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ToolsFragment()).commit();
                    return true;
                }
                return false;
            }
        });
    }
}
