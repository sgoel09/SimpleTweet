package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.databinding.ActivityDetailsBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import okhttp3.Headers;

import static java.lang.Integer.max;

public class DetailsActivity extends AppCompatActivity {

    Tweet tweet;
    ActivityDetailsBinding binding;
    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_details);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Unwrap the movie passed in via intent, using its simple name as key
        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));

        // Set the title and overview
        binding.tvName.setText(tweet.user.name);
        binding.tvScreenName.setText(String.format("@%s", tweet.user.screenName));
        binding.tvBody.setText(tweet.body);
        Log.i("DetailsActivity", String.valueOf(tweet.retweets));
        binding.tvRetweetCount.setText(String.valueOf(tweet.retweets));
        binding.tvFavorites.setText(String.valueOf(tweet.favorites));
        Glide.with(this).load(tweet.user.profileImageUrl).transform(new RoundedCorners(8)).into(binding.ivProfilePic);
        if (tweet.imageUrl != null) {
            Glide.with(this).load(tweet.imageUrl).into(binding.ivImageMedia);
            binding.ivImageMedia.setVisibility(View.VISIBLE);
        }
        else if (tweet.imageUrl == null) {
            binding.ivImageMedia.setVisibility(View.GONE);
        }
        if (tweet.retweeted) {
            ColorStateList csl = AppCompatResources.getColorStateList(view.getContext(), R.color.retweetGreen);
            ImageViewCompat.setImageTintList(binding.ivRetweet, csl);
        }

        binding.ivRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.i("DetailsActivity", "onClick");
                client = TwitterApp.getRestClient(v.getContext());
                if (tweet.retweeted == false) {
                    client.publishRetweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i("DetailsActivity", "retweeted");
                            ColorStateList csl = AppCompatResources.getColorStateList(v.getContext(), R.color.retweetGreen);
                            ImageViewCompat.setImageTintList(binding.ivRetweet, csl);
                            tweet.retweeted = true;
                            binding.tvRetweetCount.setText(String.valueOf(tweet.retweets + 1));
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
                            binding.tvRetweetCount.setText(String.valueOf(tweet.retweets - 1));
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e("DetailsActivity", response, throwable);
                        }
                    });
                }
            }
        });

        binding.ivFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                client = TwitterApp.getRestClient(v.getContext());
                if (tweet.favorited == false) {
                    client.publishFavorite(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i("DetailsActivity", "favorited");
                            ColorStateList csl = AppCompatResources.getColorStateList(v.getContext(), R.color.favoriteRed);
                            ImageViewCompat.setImageTintList(binding.ivFavorite, csl);
                            tweet.favorited = true;
                            binding.tvFavorites.setText(String.valueOf(tweet.favorites + 1));
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
                            binding.tvFavorites.setText(String.valueOf(tweet.favorites - 1));
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e("DetailsActivity", response, throwable);
                        }
                    });
                }
            }
        });
    }


}