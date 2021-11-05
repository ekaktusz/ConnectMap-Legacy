package com.example.connectmap.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connectmap.R;
import com.example.connectmap.database.FirebaseManager;
import com.example.connectmap.database.Post;

import java.util.ArrayList;

public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.PostViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";

    private Context mContext;
    private ArrayList<Post> posts;
    private FirebaseManager firebaseManager = FirebaseManager.getInstance();

    public PostRecyclerViewAdapter(Context context, ArrayList<Post> posts) {
        this.mContext = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_element_layout, parent, false);
        PostViewHolder holder = new PostViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, final int position) {
        if (posts.get(position).isVisible()) {
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
        holder.tvDate.setText(posts.get(position).getDateString());
        holder.tvPostText.setText(posts.get(position).getText());
        holder.tvScore.setText(Integer.toString(posts.get(position).getScore()));
        if (posts.get(position).getImageUrl().equals("")) {
            holder.postImage.setVisibility(View.GONE);
        } else {
            holder.postImage.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(posts.get(position).getImageUrl()).into(holder.postImage);
        }
        holder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseManager.getPostsDatabase().child(posts.get(position).getPostId()).child("score").setValue(posts.get(position).getScore() + 1);
            }
        });
        holder.btnDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseManager.getPostsDatabase().child(posts.get(position).getPostId()).child("score").setValue(posts.get(position).getScore() - 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout parentLayout;
        TextView tvDate;
        TextView tvPostText;
        TextView tvScore;
        ImageView postImage;
        Button btnLike;
        Button btnDislike;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPostText = itemView.findViewById(R.id.tvPostText);
            tvScore = itemView.findViewById(R.id.score);
            parentLayout = itemView.findViewById(R.id.parent_layout);
            postImage = itemView.findViewById(R.id.post_image);
            btnLike = itemView.findViewById(R.id.like_button);
            btnDislike = itemView.findViewById(R.id.dislike_button);
        }
    }

}
