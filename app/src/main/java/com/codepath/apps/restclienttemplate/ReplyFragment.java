package com.codepath.apps.restclienttemplate;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReplyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReplyFragment extends Fragment {

    public interface ComposeFragmentListener {
        void onFinishTweet(Parcelable parcels);
    }

    // TODO: Rename and change types of parameters
    EditText etCompose;
    Button btnTweet;
    TextView tvCharacterCount;

    TwitterClient client;

    public ReplyFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ReplyFragment newInstance() {
        ReplyFragment fragment = new ReplyFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reply, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etCompose = view.findViewById(R.id.etCompose);
        btnTweet = view.findViewById(R.id.btnTweet);
        tvCharacterCount = view.findViewById(R.id.tvCharacterCount);
        tvCharacterCount.setText("0/280");

        etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int tweetLength = etCompose.getText().toString().length();
                String charsLeftMessage = tweetLength + "/280";
                tvCharacterCount.setText(charsLeftMessage);
                if (tweetLength > 280) {
                    tvCharacterCount.setTextColor(Color.parseColor("#FFFFFF"));
                } else {
                    tvCharacterCount.setTextColor(Color.parseColor("#657786"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String tweetContent = etCompose.getText().toString();
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
//                            Intent i = new Intent();
//                            i.putExtra("tweet", Parcels.wrap(tweet));
//                            setResult(RESULT_OK, i);
                            ComposeFragment.ComposeFragmentListener listener = (ComposeFragment.ComposeFragmentListener) getActivity();
                            listener.onFinishTweet(Parcels.wrap(tweet));
                            //dismiss();
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
    }
}