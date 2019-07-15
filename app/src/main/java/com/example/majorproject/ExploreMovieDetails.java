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
/*
Class to show the movie details: Name, Release date, Overview and Poster.
Functionality:
    Like Button: Add the movie to liked movies list by callnig server with userID and movie title
    Dislike Button: Add the movie to disliked movies list by callnig server with userID and movie title
    Find Out More: Launch browser to find more details about movie from google serach
* */

public class ExploreMovieDetails extends AppCompatActivity implements View.OnClickListener {
    TextView textViewOverview;
    TextView textViewTitle;
    TextView textViewOriginalTitle;
    TextView textViewReleaseDate;
    ImageView imageViewPoster;
    String movieName;
    Button buttonLaunchGoogle;
    Button buttonLike, buttonDislike;
    String likeURL;
    String dislikeURL;
    String uID;
    Boolean flag = true;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_movie_details);


        likeURL = "https://major-project-final-246818.appspot.com/addLike/";
        dislikeURL = "https://major-project-final-246818.appspot.com/addLike/";

        textViewTitle = findViewById(R.id.textViewMovieTitle);
        textViewOriginalTitle = findViewById(R.id.textViewMovieOriginalTitle);
        textViewReleaseDate = findViewById(R.id.textViewReleaseDate);
        textViewOverview = findViewById(R.id.textViewOverview);
        imageViewPoster = findViewById(R.id.imageViewPoster);
        buttonLaunchGoogle = findViewById(R.id.buttonLaunchInternet);
        buttonLike = findViewById(R.id.buttonLike);
        buttonDislike = findViewById(R.id.buttonDislike);

        Bundle extras = getIntent().getExtras();
        byte[] byteArray = extras.getByteArray("moviePoster");
        Bitmap moviePoster = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        imageViewPoster.setImageBitmap(moviePoster);
        textViewOverview.setText("Overview" + "\n" + extras.getString("string_overview"));
        movieName = extras.getString("movieTitle");
        textViewTitle.setText("Title: " + movieName);
        textViewOriginalTitle.setText("Original Title: " + extras.getString("movieOriginalTitle"));
        textViewReleaseDate.setText("Release Date: " + extras.getString("movieReleaseDate"));

        buttonLaunchGoogle.setOnClickListener(this);
        buttonLike.setOnClickListener(this);
        buttonDislike.setOnClickListener(this);

        //get the user id to be added with url request to call API function
        firebaseAuth = FirebaseAuth.getInstance();
        uID = "user" + firebaseAuth.getCurrentUser().getUid();

        String[] input = movieName.split(" ");
        String address2 = "";
        for (int i = 0; i < input.length - 1; i++) {
            address2 += input[i] + "+";
        }
        address2 += input[input.length - 1];
        likeURL = likeURL + uID + "+" + address2;
        dislikeURL = dislikeURL + uID + "+" + address2;

    }

    @Override
    public void onClick(View view) {
        if (view == buttonLaunchGoogle) {
            String escapedQuery = null;
            try {
                escapedQuery = URLEncoder.encode(movieName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Uri uri = Uri.parse("http://www.google.com/#q=" + escapedQuery);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        if (view == buttonLike) {
            flag = true;
            LikeDislikeAsyncTask task = new LikeDislikeAsyncTask();
            task.execute(likeURL);
        }
        if (view == buttonDislike) {
            flag = false;
            LikeDislikeAsyncTask task = new LikeDislikeAsyncTask();
            task.execute(dislikeURL);
        }
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

    /*=============================================================================================
    Send data to url passed, the function written on sever will save data about user accordingly
     */
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
        Toast.makeText(ExploreMovieDetails.this, toastMessage,
                Toast.LENGTH_LONG).show();
    }
}