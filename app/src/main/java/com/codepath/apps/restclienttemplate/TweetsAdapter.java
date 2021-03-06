package com.codepath.apps.restclienttemplate;

import android.app.Activity;
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

// Represents the adapter for tweets in a recyclerview
public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    private Activity context;
    private List<Tweet> tweets;

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
        ItemTweetBinding binding = ItemTweetBinding.inflate(context.getLayoutInflater());
        View view = binding.getRoot();
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
    void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of tweets to the recycler
    void addAll(List<Tweet> tweetsAdd) {
        tweets.addAll(tweetsAdd);
        notifyDataSetChanged();
    }

    // Define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ItemTweetBinding binding;

        public ViewHolder(@NonNull View itemView, ItemTweetBinding bind) {
            super(itemView);
            bind.getRoot();
            binding = bind;
            itemView.setOnClickListener(this);
            binding.ivRetweet.setOnClickListener(retweetListener);
            binding.ivFavorite.setOnClickListener(favoriteListener);
            binding.ivReply.setOnClickListener(replyListener);
        }

        // Bind the attributes of the tweet to their corresponding views
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

        // Updates the retweet views when a retweet is made
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

        // Updates the favorite views when a tweet is favorited
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

        // Displays the reply fragment when the reply icon is clicked
        View.OnClickListener replyListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Tweet tweet = tweets.get(getAdapterPosition());
                FragmentManager fm = ((TimelineActivity) context).getSupportFragmentManager();
                ComposeFragment composeFragment = ComposeFragment.newInstance(tweet.user.screenName);
                composeFragment.show(fm, "fragment_compose");
            }
        };

    }

    // Set the color of the retweet icon based on the user's status with that tweet
    private void setRetweetColor(Tweet tweet, ItemTweetBinding binding) {
        if (tweet.retweeted) {
            ColorStateList csl = AppCompatResources.getColorStateList(context, R.color.retweetGreen);
            ImageViewCompat.setImageTintList(binding.ivRetweet, csl);
        } else {
            ColorStateList csl = AppCompatResources.getColorStateList(context, R.color.standardGray);
            ImageViewCompat.setImageTintList(binding.ivRetweet, csl);
        }
    }

    // Set the color of the retweet icon based on the user's status with that tweet
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
