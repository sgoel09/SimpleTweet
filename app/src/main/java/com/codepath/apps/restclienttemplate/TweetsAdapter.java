package com.codepath.apps.restclienttemplate;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.databinding.ItemTweetBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import java.util.List;

import okhttp3.Headers;

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
            binding.ivFavorite.setOnClickListener(favoriteListener);
        }

        public void bind(Tweet tweet) {
            binding.tvBody.setText(tweet.body);
            binding.tvScreenName.setText(String.format("@%s", tweet.user.screenName));
            binding.tvName.setText(tweet.user.name);
            binding.tvCreatedAt.setText(tweet.createdAt);
            binding.tvRetweetCount.setText(String.valueOf(tweet.retweets));
            binding.tvFavorites.setText(String.valueOf(tweet.favorites));
            Glide.with(context).load(tweet.user.profileImageUrl).transform(new RoundedCorners(8)).into(binding.ivProfileImage);
            if (tweet.imageUrl != null) {
                Glide.with(context).load(tweet.imageUrl).into(binding.ivMedia);
                binding.ivMedia.setVisibility(View.VISIBLE);
            }
            else if (tweet.imageUrl == null) {
                binding.ivMedia.setVisibility(View.GONE);
            }
            setRetweetColor(tweet, binding);
            setFavoriteColor(tweet, binding);
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
            public void onClick(final View v) {
                TwitterClient client = TwitterApp.getRestClient(v.getContext());
                final Tweet tweet = tweets.get(getAdapterPosition());
                if (tweet.retweeted == false) {
                    client.publishRetweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i("DetailsActivity", "retweeted");
                            ColorStateList csl = AppCompatResources.getColorStateList(v.getContext(), R.color.retweetGreen);
                            ImageViewCompat.setImageTintList(binding.ivRetweet, csl);
                            tweet.retweeted = true;
                            tweet.retweets += 1;
                            binding.tvRetweetCount.setText(String.valueOf(tweet.retweets));
                            setRetweetColor(tweet, binding);
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e("DetailsActivity", response, throwable);
                        }
                    });
                } else {
                    client.publishUnretweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i("DetailsActivity", "unretweeted");
                            ColorStateList csl = AppCompatResources.getColorStateList(v.getContext(), R.color.standardGray);
                            ImageViewCompat.setImageTintList(binding.ivRetweet, csl);
                            tweet.retweeted = false;
                            tweet.retweets -= 1;
                            binding.tvRetweetCount.setText(String.valueOf(tweet.retweets));
                            setRetweetColor(tweet, binding);
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e("DetailsActivity", response, throwable);
                        }
                    });
                }
            }
        };

        public View.OnClickListener favoriteListener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                TwitterClient client = TwitterApp.getRestClient(v.getContext());
                final Tweet tweet = tweets.get(getAdapterPosition());
                if (tweet.favorited == false) {
                    client.publishFavorite(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i("DetailsActivity", "favorited");
                            ColorStateList csl = AppCompatResources.getColorStateList(v.getContext(), R.color.favoriteRed);
                            ImageViewCompat.setImageTintList(binding.ivFavorite, csl);
                            tweet.favorited = true;
                            tweet.favorites += 1;
                            binding.tvFavorites.setText(String.valueOf(tweet.favorites));
                            setFavoriteColor(tweet, binding);
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e("DetailsActivity", response, throwable);
                        }
                    });
                } else {
                    client.publishUnfavorite(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i("DetailsActivity", "unfavorited");
                            ColorStateList csl = AppCompatResources.getColorStateList(v.getContext(), R.color.standardGray);
                            ImageViewCompat.setImageTintList(binding.ivFavorite, csl);
                            tweet.favorited = false;
                            tweet.favorites -= 1;
                            binding.tvFavorites.setText(String.valueOf(tweet.favorites));
                            setFavoriteColor(tweet, binding);
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e("DetailsActivity", response, throwable);
                        }
                    });
                }
            }
        };
    }

    private void setRetweetColor(Tweet tweet, ItemTweetBinding binding) {
        if (tweet.retweeted) {
            ColorStateList csl = AppCompatResources.getColorStateList(context, R.color.retweetGreen);
            ImageViewCompat.setImageTintList(binding.ivRetweet, csl);
        } else {
            ColorStateList csl = AppCompatResources.getColorStateList(context, R.color.standardGray);
            ImageViewCompat.setImageTintList(binding.ivRetweet, csl);
        }
    }

    private void setFavoriteColor(Tweet tweet, ItemTweetBinding binding) {
        if (tweet.favorited) {
            ColorStateList csl = AppCompatResources.getColorStateList(context, R.color.favoriteRed);
            ImageViewCompat.setImageTintList(binding.ivFavorite, csl);
        } else {
            ColorStateList csl = AppCompatResources.getColorStateList(context, R.color.standardGray);
            ImageViewCompat.setImageTintList(binding.ivFavorite, csl);
        }
    }
}
