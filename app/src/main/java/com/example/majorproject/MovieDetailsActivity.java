package com.example.majorproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;
/*Activity similar to Explore Movie details, except it shows details of movie with rating */

public class MovieDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    Bitmap moviePoster;
    ImageView posterImageView;
    TextView movieRating;
    TextView movieTitleTextView;
    CircleDisplay cd;
    String movieTitle;
    float score;
    String likeURL;
    String dislikeURL;
    String uID;
    Boolean flag = true;
    FirebaseAuth firebaseAuth;
    Button buttonLaunchGoogle;
    Button buttonLike, buttonDislike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        likeURL = "https://major-project-final-246818.appspot.com/addLike/";
        dislikeURL = "https://major-project-final-246818.appspot.com/addDislike/";
        buttonLaunchGoogle = findViewById(R.id.buttonLaunchInternet);
        buttonLike = findViewById(R.id.buttonLike);
        buttonDislike = findViewById(R.id.buttonDislike);


        Bundle extras = getIntent().getExtras();
        byte[] byteArray = extras.getByteArray("moviePoster");
        moviePoster = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        posterImageView = findViewById(R.id.image_view);
        posterImageView.setImageBitmap(moviePoster);
        movieRating = findViewById(R.id.textViewMovieRating);

        String tempRating = extras.getString("movieRating");
        movieRating.setText("Rating: " + tempRating);
        movieTitleTextView = findViewById(R.id.movieTitleTextView);
        movieTitle = extras.getString("movieTitle");
        movieTitleTextView.setText(movieTitle);
        cd = findViewById(R.id.circleDisplay);
        score = Float.parseFloat((tempRating.split("/"))[0]);
        showCircle();
        posterImageView.setOnClickListener(this);
        movieTitleTextView.setOnClickListener(this);
        buttonLaunchGoogle.setOnClickListener(this);
        buttonLike.setOnClickListener(this);
        buttonDislike.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();
        uID = "user" + firebaseAuth.getCurrentUser().getUid();

        String[] input = movieTitle.split(" ");
        String address2 = "";
        for (int i = 0; i < input.length - 1; i++) {
            address2 += input[i] + "+";
        }
        address2 += input[input.length - 1];
        likeURL = likeURL + uID + "+" + address2;
        dislikeURL = dislikeURL + uID + "+" + address2;
    }

    void showCircle() {
        cd.setAnimDuration(3000);
        cd.setValueWidthPercent(55f);
        cd.setTextSize(16f);
        score = score * 10;
        cd.setColor(getResources().getColor(R.color.PrimaryPurple));
        cd.setDrawText(true);
        cd.setDrawInnerCircle(true);
        cd.setFormatDigits(1);
        cd.setTouchEnabled(false);
        cd.setUnit("%");
        cd.setStepSize(0.5f);
        cd.showValue(score, 100f, true);
    }

    @Override
    public void onClick(View view) {
        if (view == posterImageView) {
            callGooogle();
        }
        if (view == movieTitleTextView) {
            callGooogle();
        }
        if (view == buttonLaunchGoogle) {
            String escapedQuery = null;
            try {
                escapedQuery = URLEncoder.encode(movieTitle, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Uri uri = Uri.parse("http://www.google.com/#q=" + escapedQuery);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        if (view == buttonLike) {
            flag = true;
            buttonLike.setBackground(getResources().getDrawable(R.drawable.ic_button_like_red));
            buttonDislike.setBackground(getResources().getDrawable(R.drawable.ic_dislike));
            MovieDetailsActivity.LikeDislikeAsyncTask task = new MovieDetailsActivity.LikeDislikeAsyncTask();
            task.execute(likeURL);

        }
        if (view == buttonDislike) {
            flag = false;
            buttonLike.setBackground(getResources().getDrawable(R.drawable.ic_like));
            buttonDislike.setBackground(getResources().getDrawable(R.drawable.ic_button_dislike_red));
            MovieDetailsActivity.LikeDislikeAsyncTask task = new MovieDetailsActivity.LikeDislikeAsyncTask();
            task.execute(dislikeURL);
        }

    }

    public void callGooogle() {
        String escapedQuery = null;
        try {
            escapedQuery = URLEncoder.encode(movieTitle, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Uri uri = Uri.parse("http://www.google.com/#q=" + escapedQuery);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public boolean isNetworkAvailable() {
        try {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            return mNetworkInfo != null;

        } catch (NullPointerException e) {
            return false;
        }
    }

    public void doActions(String result) {
        if (result.equals("Error")) {
            showToast("Server communication failure, please try again later");
        } else {
            if (flag == true) {
                showToast("Added to likes");
            } else {
                showToast("Added to dislikes");
            }
        }
    }

    public void showToast(String toastMessage) {
        Toast.makeText(MovieDetailsActivity.this, toastMessage,
                Toast.LENGTH_LONG).show();
    }

    public class LikeDislikeAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            if (isNetworkAvailable()) {
                String res = "str";
                try {
                    URL url = new URL(strings[0]);
                    InputStream inputStream;
                    HttpsURLConnection requestConnection = (HttpsURLConnection) url.openConnection();
                    requestConnection.setReadTimeout(200000);
                    requestConnection.setConnectTimeout(100000);
                    requestConnection.setRequestMethod("GET");
                    requestConnection.connect();
                    //if (requestConnection.getResponseCode() == 200) {
                    inputStream = requestConnection.getInputStream();
                    res = readFromStream(inputStream);
                    //}
                    requestConnection.disconnect();
                    Log.v("Result:", "\n\t\t\t====================" + res);
                    return res;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return "Error";
        }

        @Override
        protected void onPostExecute(String result) {
            doActions(result);
            ///progressBar.setVisibility(View.INVISIBLE);
            //details.setVisibility(View.VISIBLE);
        }

        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }
    }
}
