package com.codepath.apps.restclienttemplate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.databinding.ActivityTimelineBinding;
import com.codepath.apps.restclienttemplate.databinding.ItemTweetBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.parceler.Parcels;

import java.util.List;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    Activity context;
    List<Tweet> tweets;

    // Pass in the context and list of tweets
    public TweetsAdapter(Activity context, List<Tweet> tweets) {
        Log.d("TweetsAdapter", "constructor");
        this.context = context;
        this.tweets = tweets;
    }

    // For each row, inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TweetsAdapter", "viewholdercreate");
        //View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        ItemTweetBinding binding = ItemTweetBinding.inflate(context.getLayoutInflater());
        View view = binding.getRoot();
        //setContentView(view);
        return new ViewHolder(view, binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data at position
        Tweet tweet = tweets.get(position);
        // Bind the tweet with view holder
        Log.d("TweetsAdapter", "bindcalled");
        holder.bind(tweet);
    }

    // Bind values based on the position of the element
    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Clear all elements in the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of tweets to the recycler
    public void addAll(List<Tweet> tweetsAdd) {
        tweets.addAll(tweetsAdd);
        notifyDataSetChanged();
    }

    // Define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

//        ImageView ivProfileImage;
//        TextView tvBody;
//        TextView tvScreenName;
//        TextView tvName;
//        TextView tvCreatedAt;
//        ImageView ivMedia;
        ItemTweetBinding binding;

        public ViewHolder(@NonNull View itemView, ItemTweetBinding bind) {
            super(itemView);
//            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
//            tvBody = itemView.findViewById(R.id.tvBody);
//            tvScreenName = itemView.findViewById(R.id.tvScreenName);
//            tvName = itemView.findViewById(R.id.tvName);
//            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
//            ivMedia = itemView.findViewById(R.id.ivMedia);
            bind.getRoot();
            binding = bind;
            itemView.setOnClickListener(this);
            binding.ivRetweet.setOnClickListener(retweetListener);
        }

        public void bind(Tweet tweet) {
            binding.tvBody.setText(tweet.body);
            binding.tvScreenName.setText(String.format("@%s", tweet.user.screenName));
            binding.tvName.setText(tweet.user.name);
            binding.tvCreatedAt.setText(tweet.createdAt);
            Glide.with(context).load(tweet.user.profileImageUrl).transform(new RoundedCorners(8)).into(binding.ivProfileImage);
            if (tweet.imageUrl != null) {
                Glide.with(context).load(tweet.imageUrl).into(binding.ivMedia);
                binding.ivMedia.setVisibility(View.VISIBLE);
            }
            else if (tweet.imageUrl == null) {
                binding.ivMedia.setVisibility(View.GONE);
            }
            Log.i("TweetsAdapter", "binded");
        }

        // Calls the DetailsActivity when a row is clicked
        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Tweet tweet = tweets.get(position);
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                context.startActivity(intent);
            }
        }

        public View.OnClickListener retweetListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorStateList csl = AppCompatResources.getColorStateList(v.getContext(), R.color.retweetGreen);
                ImageViewCompat.setImageTintList(binding.ivRetweet, csl);
            }
        };
    }
}
