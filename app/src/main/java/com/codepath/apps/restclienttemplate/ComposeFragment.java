package com.codepath.apps.restclienttemplate;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.databinding.FragmentComposeBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ComposeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
// Fragment that represents composing and replying to a tweet
public class ComposeFragment extends DialogFragment {

    // Interface for its listener
    public interface ComposeFragmentListener {
        void onFinishTweet(Parcelable parcels);
    }

    FragmentComposeBinding binding;
    TwitterClient client;

    // Required empty public constructor
    public ComposeFragment() {

    }

    // Create instance of a compose fragment
    public static ComposeFragment newInstance() {
        ComposeFragment fragment = new ComposeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    // Create instance of a reply fragment (has @username included)
    public static ComposeFragment newInstance(String handle) {
        ComposeFragment fragment = new ComposeFragment();
        Bundle args = new Bundle();
        args.putString("handle", handle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentComposeBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Updates the EditText if the tweet is a reply
        boolean hasHandle = getArguments().containsKey("handle");
        if (hasHandle) {
            String screenName = getArguments().getString("handle");
            String handle = String.format("@%s", screenName);
            binding.etCompose.setText(handle);
        }
        String charCount = String.valueOf(binding.etCompose.getText().length());
        binding.tvCharacterCount.setText(String.format("%s/280", charCount));

        // Displays current character count of the tweet
        binding.etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int tweetLength = binding.etCompose.getText().toString().length();
                String charsLeftMessage = tweetLength + "/280";
                binding.tvCharacterCount.setText(charsLeftMessage);
                if (tweetLength > 280) {
                    binding.tvCharacterCount.setTextColor(Color.parseColor("#FB2D47"));
                } else {
                    binding.tvCharacterCount.setTextColor(Color.parseColor("#657786"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Publish tweet once button is clicked
        binding.btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String tweetContent = binding.etCompose.getText().toString();
                if (tweetContent.isEmpty()) {
                    Toast.makeText(v.getContext(), "Sorry, your tweet cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                } else if (tweetContent.length() > 280) {
                    Toast.makeText(v.getContext(), "Sorry, your tweet is too long", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(v.getContext(), tweetContent, Toast.LENGTH_LONG).show();
                // Make API call to Twitter to tweet
                client = TwitterApp.getRestClient(v.getContext());
                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i("ComposeFragment", "onSuccess to publish tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i("ComposeFragment", "Published tweet says: " + tweet);
                            ComposeFragmentListener listener = (ComposeFragmentListener) getActivity();
                            listener.onFinishTweet(Parcels.wrap(tweet));
                            dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e("ComposeFragment", "onFailure to publish tweet", throwable);
                    }
                });
            }
        });

        binding.ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}