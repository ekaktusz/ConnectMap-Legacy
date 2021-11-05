package com.example.connectmap.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.connectmap.R;
import com.example.connectmap.database.FirebaseManager;
import com.example.connectmap.database.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import static com.example.connectmap.HelperMethods.isNetworkAvailable;

public class NewPostActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "NewPostActivity";

    private TextView locationIndicator;
    private EditText inputText;
    private Button btnAddImage;
    private Button btnClearImage;
    private FloatingActionButton btnSendPost;
    private ImageView attachmentImage;
    private BottomAppBar bar;
    private ProgressBar progressBar;

    private Uri imageUri;
    private String uploadImgUri = "";
    private FirebaseManager firebaseManager = FirebaseManager.getInstance();
    private StorageTask uploadImageTask;

    private double longitude;
    private double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);

        //Set the top bar text and color
        getSupportActionBar().setTitle(R.string.new_post);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));

        locationIndicator = findViewById(R.id.location_info);
        inputText = findViewById(R.id.post_text_input);
        btnAddImage = findViewById(R.id.add_img_button);
        btnClearImage = findViewById(R.id.clear_img_button);
        btnSendPost = findViewById(R.id.fab_send);
        attachmentImage = findViewById(R.id.attachment_img);
        bar = findViewById(R.id.bar_send);
        progressBar = findViewById(R.id.progress_horizontal);

        locationIndicator.setText(Double.toString(latitude) + "," + Double.toString(longitude));

        btnSendPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uploadImageTask != null && uploadImageTask.isInProgress()) {
                    Toast.makeText(NewPostActivity.this, R.string.upload_is_in_prog, Toast.LENGTH_SHORT).show();
                } else {
                    if (isNetworkAvailable(getApplicationContext())) {
                        addNewPost();
                    } else {
                        Toast.makeText(NewPostActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        btnClearImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearImage();
            }
        });
    }

    public void addNewPost() {

        String text = inputText.getText().toString();
        if (text.length() > 225) {
            Toast.makeText(getBaseContext(), R.string.txt_too_long, Toast.LENGTH_SHORT).show();
            return;
        }
        if (text.length() < 5) {
            Toast.makeText(getBaseContext(), R.string.txt_too_short, Toast.LENGTH_SHORT).show();
            return;
        }
        btnSendPost.setEnabled(false);
        btnAddImage.setEnabled(false);
        btnClearImage.setEnabled(false);
        inputText.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        if (imageUri != null) {
            postWithImage();
        } else {
            postWithoutImage();
        }
    }

    private void postWithImage() {
        StorageReference fileReference = firebaseManager.getImageStorage().child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
        uploadImageTask = fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (taskSnapshot.getMetadata() != null) {
                    if (taskSnapshot.getMetadata().getReference() != null) {
                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                uploadImgUri = uri.toString();
                                postWithoutImage();
                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        fileReference.putFile(imageUri);
    }

    private void postWithoutImage() {
        String id = firebaseManager.getPostsDatabase().push().getKey();
        String text = inputText.getText().toString();
        Post post = new Post(id, text, uploadImgUri, longitude, latitude, ServerValue.TIMESTAMP);
        firebaseManager.getPostsDatabase().child(id).setValue(post, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                progressBar.setIndeterminate(false);
                Toast.makeText(getBaseContext(), R.string.posted_success, Toast.LENGTH_SHORT).show();
                if (databaseError == null) {
                    openMainActivity();
                } else {
                    Toast.makeText(getBaseContext(), R.string.db_error, Toast.LENGTH_SHORT).show();
                    btnSendPost.setEnabled(true); //Possibility to try again
                    btnSendPost.setEnabled(true);
                    btnAddImage.setEnabled(true);
                    btnClearImage.setEnabled(true);
                    inputText.setEnabled(true);
                }
            }
        });
    }

    private void openMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            btnAddImage.setText(R.string.change_img);
            btnClearImage.setVisibility(View.VISIBLE);
            attachmentImage.setVisibility(View.VISIBLE);
        }
        Picasso.get().load(imageUri).into(attachmentImage);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void clearImage() {
        imageUri = null;
        attachmentImage.setImageDrawable(null);
        attachmentImage.setVisibility(View.GONE);
        btnClearImage.setVisibility(View.GONE);
        btnAddImage.setText(R.string.add_img);
    }
}
