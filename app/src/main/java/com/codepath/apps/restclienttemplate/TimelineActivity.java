package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.codepath.apps.restclienttemplate.databinding.ActivityTimelineBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetDao;
import com.codepath.apps.restclienttemplate.models.TweetWithUser;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity implements ComposeFragment.ComposeFragmentListener {

    public static final String TAG = "TimelineActivity";
    private final int REQUEST_CODE = 20;

    TweetDao tweetDao;
    TwitterClient client;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    ActivityTimelineBinding binding;
    EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTimelineBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setLogo(R.drawable.twitter_logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.twitter_logo);
        //getSupportActionBar().setIcon(R.drawable.ic_launcher_twitter);

        client = TwitterApp.getRestClient(this);
        tweetDao = ((TwitterApp) getApplicationContext()).getMyDatabase().tweetDao();

        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "resfresh");
                populateHomeTimeline();
            }
        });

        // Configure the refreshing colors
        binding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Init the list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        // Recycler view setup: layout manager and the adapter
        binding.rvTweets.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvTweets.setLayoutManager(layoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "onLoadMore" + page);
                loadMoreData();
            }
        };
        // Add scroll listener to the Recycler View
        binding.rvTweets.addOnScrollListener(scrollListener);

        //Query for existing tweets in the database
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Showing data from database");
                List<TweetWithUser> tweetWithUsers = tweetDao.recentItems();
                List<Tweet> tweetsFromDB = TweetWithUser.getTweetList(tweetWithUsers);
                adapter.clear();
                adapter.addAll(tweetsFromDB);
            }
        });
    }

    private void loadMoreData() {
        long maxId = tweets.get(tweets.size() - 1).id;
        client.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess for loadMoreDate" + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
                    adapter.addAll(tweets);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure for loadMoreDate", throwable);
            }
        }, maxId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.Compose) {
            FragmentManager fm = getSupportFragmentManager();
            ComposeFragment composeFragment = ComposeFragment.newInstance();
            composeFragment.show(fm, "fragment_compose");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        populateHomeTimeline();
    }

    @Override
    public void onFinishTweet(Parcelable parcels) {
        Tweet tweet = Parcels.unwrap(parcels);
        tweets.add(0, tweet);
        adapter.notifyItemInserted(0);
        binding.rvTweets.smoothScrollToPosition(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Get data from intent
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            // Update recycler view with tweet
            tweets.add(0, tweet);
            adapter.notifyItemInserted(0);
            binding.rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess");
                JSONArray jsonArray = json.jsonArray;
                try {
                    final List<Tweet> tweetsFromNetwork = Tweet.fromJsonArray(jsonArray);
                    adapter.clear();
                    adapter.addAll(tweetsFromNetwork);
                    binding.swipeContainer.setRefreshing(false);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Saving data into database");
                            List<User> usersFromNetwork = User.fromJsonTweetArray(tweetsFromNetwork);
                            tweetDao.insertModel(usersFromNetwork.toArray(new User[0]));
                            tweetDao.insertModel(tweetsFromNetwork.toArray(new Tweet[0]));
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure" + response, throwable);
            }
        });
    }
}