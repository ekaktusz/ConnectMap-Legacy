package com.example.connectmap.database;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseManager {
    private static FirebaseManager fireBaseManager;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private FirebaseManager() {}

    public static FirebaseManager getInstance(){
        if (fireBaseManager == null) fireBaseManager = new FirebaseManager();
        return fireBaseManager;
    }

    // public DatabaseReference getVotesDatabase() { return database.getReference("votes"); }
    // public DatabaseReference getUsersDatabase() { return database.getReference("users"); }
    public DatabaseReference getPostsDatabase() { return database.getReference("posts"); }
    public StorageReference getImageStorage() { return storage.getReference("posts"); }

}
