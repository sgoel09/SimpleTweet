package com.codepath.apps.restclienttemplate.models;

import android.text.format.DateUtils;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Parcel
@Entity(foreignKeys = @ForeignKey(entity=User.class, parentColumns="id", childColumns="userId"))
public class Tweet {

    @ColumnInfo
    @PrimaryKey
    public long id;

    @ColumnInfo
    public String body;

    @ColumnInfo
    public String createdAt;

    @ColumnInfo
    public String imageUrl;

    @ColumnInfo
    public long userId;

    @Ignore
    public User user;

    @ColumnInfo
    public int retweets;
    public boolean retweeted;

    @ColumnInfo
    public int favorites;
    public boolean favorited;

    public Tweet() {}

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = getRelativeTimeAgo(jsonObject.getString("created_at"));
        User user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.user = user;
        tweet.userId = user.id;
        tweet.imageUrl = getImageUrl(jsonObject);
        tweet.id = jsonObject.getLong("id");
        tweet.retweets = jsonObject.getInt("retweet_count");
        tweet.retweeted = jsonObject.getBoolean("retweeted");
        tweet.favorites = jsonObject.getInt("favorite_count");
        tweet.favorited = jsonObject.getBoolean("favorited");
        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }

    public static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }

    public static String getImageUrl(JSONObject jsonObject) {
        if(jsonObject.has("extended_entities")){
            String type = null;
            try {
                type = jsonObject.getJSONObject("entities").getJSONArray("media").getJSONObject(0).getString("type");
                if(type.equals("photo")){
                    return jsonObject.getJSONObject("extended_entities").getJSONArray("media").getJSONObject(0).getString("media_url_https");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getBody() {
        return body;
    }

    public User getUser() {
        return user;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
