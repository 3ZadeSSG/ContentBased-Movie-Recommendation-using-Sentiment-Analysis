package com.example.majorproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.github.ybq.android.spinkit.style.DoubleBounce;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

public class MovieRatingSearchFragment extends Fragment implements View.OnClickListener {
    Bitmap moviePoster;
    Button buttonMovieSearch;
    String APIKEY = "815fe2a8";
    String address = "https://www.omdbapi.com/?apikey=" + APIKEY + "&t=";
    String movieName = "Avengers+Endgame";
    ProgressBar progressBar;
    LinearLayout details;
    String movieRating;
    String movieTitle;
    EditText inputBox;
    String posterUrl = "https://m.media-amazon.com/images/M/MV5BMTc5MDE2ODcwNV5BMl5BanBnXkFtZTgwMzI2NzQ2NzM@._V1_SX300.jpg";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_rating_search, container, false);

        details = view.findViewById(R.id.details);
        progressBar = view.findViewById(R.id.spin_kit);
        DoubleBounce myProgressBar = new DoubleBounce();
        progressBar.setIndeterminateDrawable(myProgressBar);
        inputBox = view.findViewById(R.id.editTextMovieName);
        buttonMovieSearch = view.findViewById(R.id.buttonMovieSearch);
        buttonMovieSearch.setOnClickListener(this);
        return view;
    }

    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity())
                .setActionBarTitle("Search Movie Rating");
    }

    public void showAlertDialog(String TITLE, String MESSAGE) {
        new AlertDialog.Builder(getActivity())
                .setTitle(TITLE)
                .setMessage(MESSAGE)
                .setNegativeButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void startMoviePosterDownloader() {
        DownloadImageFromInternet imageDownload = new DownloadImageFromInternet();
        imageDownload.execute(posterUrl);
    }

    @Override
    public void onClick(View view) {
        if (view == buttonMovieSearch) {
            movieName = inputBox.getText().toString();
            String[] input = inputBox.getText().toString().split(" ");
            String address2 = "";
            for (int i = 0; i < input.length - 1; i++) {
                address2 += input[i] + "+";
            }
            address2 += input[input.length - 1];
            String url = address + address2;

            IMDBAsyncTask task = new IMDBAsyncTask();
            task.execute(url);
        }
    }

    public boolean isNetworkAvailable() {
        try {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            return mNetworkInfo != null;

        } catch (NullPointerException e) {
            return false;

        }
    }

    public class IMDBAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            details.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            if (isNetworkAvailable()) {
                String res = "str";
                try {
                    URL url = new URL(strings[0]);
                    Log.v("IMDB URL:", "\n\t\t\t====================" + strings[0]);
                    InputStream inputStream;
                    HttpsURLConnection requestConnection = (HttpsURLConnection) url.openConnection();
                    requestConnection.setReadTimeout(200000);
                    requestConnection.setConnectTimeout(100000);
                    requestConnection.setRequestMethod("GET");
                    requestConnection.connect();
                    inputStream = requestConnection.getInputStream();
                    res = readFromStream(inputStream);
                    requestConnection.disconnect();
                    return res;
                } catch (Exception e) {
                    Log.v("Stack Track SSSS:", "\n\t\t\t====================" + e.getStackTrace());
                    e.printStackTrace();
                }
            }
            return "Error";
        }

        @Override
        protected void onPostExecute(String result) {
            doActions(result);
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
        JSONObject root = null;
        if (result.equals("Error")) {
            showAlertDialog("Connection Error", "No data connection found!");
            progressBar.setVisibility(View.INVISIBLE);
            details.setVisibility(View.VISIBLE);
        } else {
            try {
                root = new JSONObject(result);
                if (root.getString("Response").equals("True")) {
                    movieTitle = root.getString("Title");
                    posterUrl = root.getString("Poster");
                    JSONArray ratingsArray = root.getJSONArray("Ratings");
                    JSONObject ratings = ratingsArray.getJSONObject(0);
                    movieRating = ratings.getString("Value");
                    posterUrl = root.getString("Poster");
                    Log.v("Poster URL:", "\n\t\t\t====================" + posterUrl);
                    startMoviePosterDownloader();
                } else {
                    showAlertDialog("Error", root.getString("Error"));
                    progressBar.setVisibility(View.INVISIBLE);
                    details.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * Movie poster downloader activity
     */
    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                bimage = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            sendMoviePoster(result);
        }
    }

    public void sendMoviePoster(Bitmap result) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        Intent viewPoster = new Intent(getActivity(), MovieDetailsActivity.class);
        viewPoster.putExtra("moviePoster", byteArray);
        viewPoster.putExtra("movieRating", movieRating);
        viewPoster.putExtra("movieTitle", movieTitle);
        progressBar.setVisibility(View.INVISIBLE);
        details.setVisibility(View.VISIBLE);
        startActivity(viewPoster);
    }
}
