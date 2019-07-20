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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.firebase.auth.FirebaseAuth;

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
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class RecommendationFragment extends Fragment {

    //Server API method address to get the JSON response about recommended Movies
    String recommendedMoviesURL = "https://major-project-final-246818.appspot.com/getRecommendation/";

    //Holders for necessary views and userID
    String uID;
    FirebaseAuth firebaseAuth;
    ArrayList<Movie> MoviesList;
    MovieAdapter mAdapter;
    GridView movieGridView;
    ArrayList<Bitmap> posterList;
    LinearLayout recommendationFragmentView;
    TextView recommendationFragmentWarning;
    ProgressBar progressBar;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recommendation, container, false);

        //Initialize all variables
        movieGridView = view.findViewById(R.id.listViewRecommendedMovies);
        recommendationFragmentView = view.findViewById(R.id.linearLayoutRecommendationFragment);
        recommendationFragmentWarning = view.findViewById(R.id.textViewExploreStatusWarning);
        firebaseAuth = FirebaseAuth.getInstance();
        uID = "user" + firebaseAuth.getCurrentUser().getUid();

        MoviesList = new ArrayList<>();
        posterList = new ArrayList<>();
        progressBar = view.findViewById(R.id.spin_kit);
        DoubleBounce myProgressBar = new DoubleBounce();
        progressBar.setIndeterminateDrawable(myProgressBar);

        recommendationFragmentWarning.setVisibility(View.GONE);
        RecommendationFragment.RecommendationAsyncTask task = new RecommendationFragment.RecommendationAsyncTask();
        task.execute(recommendedMoviesURL + uID);

        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Movie currentMovie = mAdapter.getItem(i);
                Intent movieDetailScreen = new Intent(getActivity(), ExploreMovieDetails.class);
                Bitmap tempPoster = currentMovie.getPoster();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                tempPoster.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                movieDetailScreen.putExtra("moviePoster", byteArray);
                movieDetailScreen.putExtra("movieTitle", currentMovie.getTitle());
                movieDetailScreen.putExtra("movieOriginalTitle", currentMovie.getOriginalTitle());
                movieDetailScreen.putExtra("string_overview", currentMovie.getOverview());
                movieDetailScreen.putExtra("movieReleaseDate", currentMovie.getReleaseDate());
                startActivity(movieDetailScreen);
            }
        });
        return view;
    }

    /*If fragment is resumed from a paused state call super and set the title to current "Recommended Movies"
     * */
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity())
                .setActionBarTitle("Recommended Movies");
    }

    /*Method to check if data connection is available*/
    public boolean isNetworkAvailable() {
        try {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            return mNetworkInfo != null;

        } catch (NullPointerException e) {
            return false;
        }
    }

    /*Method to show alert dialog for errors or success messages*/
    public void showAlertDialog(String TITLE, String MESSAGE) {
        new AlertDialog.Builder(getActivity())
                .setTitle(TITLE)
                .setMessage(MESSAGE)
                .setNegativeButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /*Method to perform information retrieval from JSON response*/
    public void doActions(String result) {
        JSONObject root = null;
        if (result.equals("Error")) {
            showAlertDialog("Connection Error", "No data connection found!");
        } else {
            try {
                root = new JSONObject(result);
                //If status is False then there was error on server, show the received message
                if (root.getString("status").equals("False")) {
                    recommendationFragmentWarning.setText(root.getString("result"));
                    recommendationFragmentWarning.setVisibility(View.VISIBLE);
                    recommendationFragmentWarning.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
                //if no error was found then perform retrieve of movie details from JSON response
                else {
                    JSONArray resultsArray = root.getJSONArray("result");
                    String movie_title;
                    String movie_original_title;
                    String language;
                    String overview;
                    String release_date;
                    String poster_url;
                    Bitmap movie_poster;
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject details = resultsArray.getJSONObject(i);
                        movie_title = details.getString("title");
                        movie_original_title = details.getString("title");
                        language = "English";
                        overview = details.getString("overview");
                        release_date = details.getString("release");
                        poster_url = details.getString("poster");
                        MoviesList.add(new Movie(movie_title, movie_original_title, language, overview, release_date, poster_url));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        performPosterRetrival();
    }


    public void performPosterRetrival() {
        //get all poster urls into one single list to download those in a single batch
        if (MoviesList.size() != 0) {
            String[] posterUrls = new String[MoviesList.size()];
            for (int i = 0; i < MoviesList.size(); i++) {
                posterUrls[i] = (MoviesList.get(i).getPosterUrl());
            }
            //After movie details have been collected, call function to download the posters
            RecommendationFragment.DownloadImageFromInternet imageDownload = new RecommendationFragment.DownloadImageFromInternet();
            imageDownload.execute(posterUrls);
        }

    }

    public void sendMoviePoster(List<Bitmap> result) {
        for (int i = 0; i < result.size(); i++) {
            Bitmap b = result.get(i);
            b = Bitmap.createScaledBitmap(b, (b.getWidth() / 3), (b.getHeight() / 3), false);
            MoviesList.get(i).setPoster(b);
        }
        mAdapter = new MovieAdapter(getActivity(), MoviesList);
        movieGridView.setAdapter(mAdapter);
        progressBar.setVisibility(View.GONE);
        recommendationFragmentView.setVisibility(View.VISIBLE);
    }

    public class RecommendationAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
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

    /*AsyncTask to download posters from urls passed as a list in background*/
    private class DownloadImageFromInternet extends AsyncTask<String, Void, List<Bitmap>> {
        protected List<Bitmap> doInBackground(String... urls) {
            List<Bitmap> bitmaps = new ArrayList<Bitmap>();
            //String imageURL = urls[0];
            Bitmap bimage = null;
            for (String url : urls) {
                try {
                    InputStream in = new java.net.URL(url).openStream();
                    bimage = BitmapFactory.decodeStream(in);

                } catch (Exception e) {
                    Log.e("Error Message", e.getMessage());
                    e.printStackTrace();
                }
                bitmaps.add(bimage);
            }
            return bitmaps;
        }
        protected void onPostExecute(List<Bitmap> result) {
            sendMoviePoster(result);
        }
    }
}
