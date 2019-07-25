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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class ExploreFragment extends Fragment {

    //Your APIKEY to peform request
    String APIKEY="";

    //API address to get trending movies
    String trendingMoviesURL = "https://api.themoviedb.org/3/movie/popular?api_key=APIKEY&language=en-US&page=1";

    //API address to get poster of size 200px width
    String posterHeaderURL = "https://image.tmdb.org/t/p/w200";

    //List to store Movie objects which contain details about each movie
    ArrayList<Movie> MoviesList;

    //Used to inherit ArrayAdapter of Android
    MovieAdapter mAdapter;

    //GridView to show the movies in grid based layout
    GridView movieGridView;

    //to store the bitmap posters downloaded form internet
    ArrayList<Bitmap> posterList;

    //Fragment Layout container containing gridview to show the result
    LinearLayout exploreFragmentView;

    //Textview to show warning and error messages received form server
    TextView exploreFragmentWarning;

    //progressbar to show loading icon when network request is made
    ProgressBar progressBar;

    /* Default method called by android os when a new view is created*/
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        //Initialize all variables
        movieGridView = view.findViewById(R.id.listViewMovies);
        exploreFragmentView = view.findViewById(R.id.linearLayoutExploreFragment);
        exploreFragmentWarning = view.findViewById(R.id.textViewExploreStatusWarning);
        MoviesList = new ArrayList<>();
        posterList = new ArrayList<>();
        progressBar = view.findViewById(R.id.spin_kit);
        DoubleBounce myProgressBar = new DoubleBounce();
        progressBar.setIndeterminateDrawable(myProgressBar);

        //execute network request to get the movies and their posters form internet
        IMDBAsyncTask task = new IMDBAsyncTask();
        task.execute(trendingMoviesURL);

        /*add action listener to grid view, on clicking the poster, new activity ExploreMovieDetails
        will be launched to show detailed view of movie
         */
        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Movie currentMovie = mAdapter.getItem(i);
                Intent movieDetailScreen = new Intent(getActivity(), ExploreMovieDetails.class);
                Bitmap tempPoster = currentMovie.getPoster();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                tempPoster.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                //add all necessary information about the movie to be passed on the ExploreMovieDetails Activity
                movieDetailScreen.putExtra("moviePoster", byteArray);
                movieDetailScreen.putExtra("movieTitle", currentMovie.getTitle());
                movieDetailScreen.putExtra("movieOriginalTitle", currentMovie.getOriginalTitle());
                movieDetailScreen.putExtra("string_overview", currentMovie.getOverview());
                movieDetailScreen.putExtra("movieReleaseDate", currentMovie.getReleaseDate());
                startActivity(movieDetailScreen);
            }
        });

        /*
        Pull down to refresh function, if movies are already loaded then no network will be initiated
        Otherwise initiate network request
        * */
        final SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (MoviesList.size() == 0) {
                    //if movie list is empty then call method to try download data
                    refreshData();
                    pullToRefresh.setRefreshing(false);
                } else {
                    //If movies are already loaded then show the toast message
                    Toast.makeText(getActivity(), "Already Loaded with latest data",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        return view;
    }

    /*Method to perform downloading of data when refresh action is triggered
     * */
    public void refreshData() {
        IMDBAsyncTask task = new IMDBAsyncTask();
        task.execute(trendingMoviesURL);
    }

    /*If fragment is resumed from a paused state call super and set the title to current "Explore"
     * */
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity())
                .setActionBarTitle("Explore");
    }

    /*Method to check if device is connected to internet
     * */
    public boolean isNetworkAvailable() {
        try {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            return mNetworkInfo != null;

        } catch (NullPointerException e) {
            return false;
        }
    }

    /*
    Show alert dialog function, to be used by function when no internet connection is found or any other errors
    */
    public void showAlertDialog(String TITLE, String MESSAGE) {
        new AlertDialog.Builder(getActivity())
                .setTitle(TITLE)
                .setMessage(MESSAGE)
                .setNegativeButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /*If result is not Error then initiate the moviePoster downloader and download movie posters on list,
    and then call the UI update.
    If error then show alert dialog about connection error
    */
    public void doActions(String result) {
        JSONObject root = null;
        if (result.equals("Error")) {
            showAlertDialog("Connection Error", "No data connection found!");
        } else {
            //if result is not empty then try processing the JSON response and retrieve details about movies
            try {
                root = new JSONObject(result);
                String totalPages = root.getString("total_pages");
                Toast.makeText(getActivity(), totalPages,
                        Toast.LENGTH_LONG).show();
                JSONArray resultsArray = root.getJSONArray("results");
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
                    movie_original_title = details.getString("original_title");
                    language = details.getString("original_language");
                    overview = details.getString("overview");
                    release_date = details.getString("release_date");
                    poster_url = posterHeaderURL + details.getString("poster_path");

                    //Call class constructor of Movie class with necessary details and add new Movie to List
                    MoviesList.add(new Movie(movie_title, movie_original_title, language, overview, release_date, poster_url));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //Perform Poster Reterival from the urls received form response
        performPosterRetrival();
    }

    /*Get all poster urls in a string of array and pass it as argument of ImageDownloader  Async Task,
   to download the posters
    */
    public void performPosterRetrival() {
        String[] posterUrls = new String[MoviesList.size()];
        for (int i = 0; i < MoviesList.size(); i++) {
            posterUrls[i] = (MoviesList.get(i).getPosterUrl());
        }
        DownloadImageFromInternet imageDownload = new DownloadImageFromInternet();
        imageDownload.execute(posterUrls);

    }

    /*Set the movie poster to downloaded bitmap image, since we are showing only thumbnail
    on ImageView it is much better to scale down resolution to save space.
    * */
    public void sendMoviePoster(List<Bitmap> result) {
        for (int i = 0; i < result.size(); i++) {
            Bitmap b = result.get(i);
            //Trim the dimension of image to half, since we only need to show a thumbnail
            b = Bitmap.createScaledBitmap(b, (b.getWidth() / 2), (b.getHeight() / 2), false);
            MoviesList.get(i).setPoster(b);
        }
        mAdapter = new MovieAdapter(getActivity(), MoviesList);
        movieGridView.setAdapter(mAdapter);

        //hide the warning textview and progress bar and show the fragment containing movies
        exploreFragmentWarning.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        exploreFragmentView.setVisibility(View.VISIBLE);
    }

    /*
     * Inherited AsyncTask to download the movie details form server and
     * pass the received string to process as a JSON response to reterive the information
     */
    public class IMDBAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        /**
         * If network is available then process request otherwise return Error as result
         */
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
                    e.printStackTrace();
                }
            }
            return "Error";
        }

        //call doActions function to perform update on UI
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

    /*===============================================================================
    Async Task to download the posters form array of urls, passed as string array.
    * */
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

        //call sendMoviePoster to finally load the posters and show the results
        protected void onPostExecute(List<Bitmap> result) {
            sendMoviePoster(result);
        }
    }
}
