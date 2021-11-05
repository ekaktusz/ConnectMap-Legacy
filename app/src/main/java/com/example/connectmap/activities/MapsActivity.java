package com.example.connectmap.activities;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.connectmap.HelperMethods;
import com.example.connectmap.R;
import com.example.connectmap.database.FirebaseManager;
import com.example.connectmap.database.Post;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";

    private static final int MAX_MARKER_SIZE = 200;
    private static final int MIN_MARKER_SIZE = 10;

    private int maxScore = 1;

    private double latitude;
    private double longitude;

    private SharedPreferences sharedPref;
    int radius;
    private Date dateOrigo;
    int timeInterval;

    private GoogleMap mMap;
    private FirebaseManager firebaseManager = FirebaseManager.getInstance();
    private DatabaseReference mRef = firebaseManager.getPostsDatabase();

    private ArrayList<Post> posts = new ArrayList<>();

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        radius = sharedPref.getInt("radius", 20);
        timeInterval = sharedPref.getInt("time_interval", 24);
        try {
            dateOrigo = format.parse(sharedPref.getString("date_preference", "1970-01-01"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        searchView = findViewById(R.id.search_location_map);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;
                if (location != null || !location.equals("")) {
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addressList != null && !addressList.isEmpty()) {
                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                    } else {
                        Toast.makeText(getBaseContext(), R.string.invalid_adress, Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        setCustomInfoWindow();

        LatLng currentPosition = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMap.clear();
                posts.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post.getScore() > maxScore) maxScore = post.getScore();

                    if (HelperMethods.isPostInTime(post, dateOrigo, timeInterval)) {
                        posts.add(post);
                        addPostToMap(post);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getBaseContext(), R.string.db_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPostToMap(Post post) {
        LatLng postLatLng = new LatLng(post.getLatitude(), post.getLongitude());
        int size = calculateSizeFromScore(post);
        Bitmap mDotMarkerBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mDotMarkerBitmap);
        Drawable shape = getResources().getDrawable(R.drawable.map_dot);
        shape.setTint(Color.BLUE);
        shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
        shape.draw(canvas);
        mMap.addMarker(new MarkerOptions()
                .position(postLatLng)
                .title(post.getDateString())
                .snippet(post.getText())
                .icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)));
    }

    private int calculateSizeFromScore(Post post) {
        if (maxScore == 0) return MIN_MARKER_SIZE;
        double norm = (double) post.getScore() / (double) maxScore;
        return MIN_MARKER_SIZE + (int) Math.round(norm * (MAX_MARKER_SIZE - MIN_MARKER_SIZE));
    }

    private void setCustomInfoWindow() {
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(getApplicationContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getApplicationContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getApplicationContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
    }


}
