package com.example.connectmap.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.connectmap.HelperMethods;
import com.example.connectmap.adapters.PostRecyclerViewAdapter;
import com.example.connectmap.R;
import com.example.connectmap.database.FirebaseManager;
import com.example.connectmap.database.Post;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.example.connectmap.HelperMethods.isNetworkAvailable;
import static com.example.connectmap.HelperMethods.isPostInTime;
import static com.example.connectmap.HelperMethods.isPostNearby;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private static final String TAG = "MainActivity";
    private static final int LOCATION_ACCESS_REQUEST = 2;
    private static final int SETTINGS_REQUEST = 3;

    private RecyclerView recyclerView;
    private PostRecyclerViewAdapter adapter;
    private BottomAppBar bottomAppBar;
    private SearchView searchView;
    private ExtendedFloatingActionButton btnNewPost;

    private ArrayList<Post> posts = new ArrayList<>();

    private double longitude;
    private double latitude;

    private int radius;
    private Date dateOrigo;
    private int timeInterval;

    private enum SortModes {SCORE, DATE}

    private SortModes currentSortMode = SortModes.SCORE;
    private SharedPreferences sharedPref;

    private FirebaseManager firebaseManager = FirebaseManager.getInstance();
    private DatabaseReference mRef = firebaseManager.getPostsDatabase();

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));

        recyclerView = findViewById(R.id.recyclerview_main);
        bottomAppBar = findViewById(R.id.bar);
        btnNewPost = findViewById(R.id.fab);

        updateDeviceLocation();
        initRecyclerView();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        radius = sharedPref.getInt("radius", 20);

        if (!isNetworkAvailable(this)) {
            Toast.makeText(getBaseContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
        }

        try {
            dateOrigo = format.parse(sharedPref.getString("date_preference", "1970-01-01"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        timeInterval = sharedPref.getInt("time_interval", 24);

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                posts.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    retrievePostFromSnapshot(postSnapshot);
                }
                sortPosts();
                if (searchView != null) search(searchView.getQuery().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getBaseContext(), R.string.db_error, Toast.LENGTH_SHORT).show();
            }
        });

        btnNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewPostActivity();
            }
        });

        bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.sort:
                        showPopup(findViewById(R.id.sort));
                        return true;
                    case R.id.map:
                        openMapsActivity();
                        return true;
                    default:
                        return false;
                }
            }
        });

        bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettingsActivity();
            }
        });
    }

    private class SortByScore implements Comparator<Post> {
        public int compare(Post a, Post b) {
            return b.getScore() - a.getScore();
        }
    }

    private class SortByDate implements Comparator<Post> {
        public int compare(Post a, Post b) {
            return Long.compare(b.getDateLong(), a.getDateLong());
        }
    }

    private void sortPosts() {
        if (currentSortMode == SortModes.SCORE) {
            Collections.sort(posts, new SortByScore());
        } else {
            Collections.sort(posts, new SortByDate());
        }
        adapter.notifyDataSetChanged();
    }

    private void retrievePostFromSnapshot(DataSnapshot postSnapshot) {
        Post post = postSnapshot.getValue(Post.class);
        if (isPostInTime(post, dateOrigo, timeInterval) && isPostNearby(post, latitude, longitude, radius))
            posts.add(post);
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_needed)
                        .setMessage(R.string.loc_permission_explain)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_ACCESS_REQUEST);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finishAndRemoveTask();
                            }
                        })
                        .create().show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_ACCESS_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_ACCESS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateDeviceLocation();
                } else {
                    finishAndRemoveTask();
                }
            }
        }
    }

    private void updateDeviceLocation() {
        requestLocationPermission();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            List<String> providers = locationManager.getProviders(true);
            Location bestLocation = null;
            for (String provider : providers) {
                Location location = locationManager.getLastKnownLocation(provider);
                if (location == null) {
                    continue;
                }
                if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = location;
                }
            }
            if (bestLocation != null) {
                latitude = bestLocation.getLatitude();
                longitude = bestLocation.getLongitude();
            } else {
                Toast.makeText(getBaseContext(), R.string.location_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.sort_popup_menu);
        popup.show();
    }

    private void openMapsActivity() {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startActivity(intent);
    }

    private void openNewPostActivity() {
        Intent intent = new Intent(getApplicationContext(), NewPostActivity.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startActivity(intent);
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivityForResult(intent, SETTINGS_REQUEST);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_score:
                currentSortMode = SortModes.SCORE;
                sortPosts();
                return true;
            case R.id.sort_date:
                currentSortMode = SortModes.DATE;
                sortPosts();
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return false;
            }
        });
        return true;
    }

    private void search(String str) {
        if (str.isEmpty()) {
            for (Post post : posts) {
                post.setVisible(true);
            }
            initRecyclerView();
            return;
        }
        for (Post post : posts) {
            if (post.getText().toLowerCase().contains((str.toLowerCase()))) {
                post.setVisible(true);
            } else {
                post.setVisible(false);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void initRecyclerView() {
        adapter = new PostRecyclerViewAdapter(getApplicationContext(), posts);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST) {
            recreate(); //apply new settings
        }
    }

}
