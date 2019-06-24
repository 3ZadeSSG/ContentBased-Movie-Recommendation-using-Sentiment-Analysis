package com.example.majorproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class ExploreMovieDetails extends AppCompatActivity implements View.OnClickListener {
    TextView textViewOverview;
    TextView textViewTitle;
    TextView textViewOriginalTitle;
    TextView textViewReleaseDate;
    ImageView imageViewPoster;
    String movieName;
    Button buttonLaunchGoogle;
    Button buttonLike,buttonDislike;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_movie_details);

        textViewTitle=findViewById(R.id.textViewMovieTitle);
        textViewOriginalTitle=findViewById(R.id.textViewMovieOriginalTitle);
        textViewReleaseDate=findViewById(R.id.textViewReleaseDate);
        textViewOverview=findViewById(R.id.textViewOverview);
        imageViewPoster=findViewById(R.id.imageViewPoster);
        buttonLaunchGoogle=findViewById(R.id.buttonLaunchInternet);
        buttonLike=findViewById(R.id.buttonLike);
        buttonDislike=findViewById(R.id.buttonDislike);

        Bundle extras = getIntent().getExtras();
        byte[] byteArray = extras.getByteArray("moviePoster");
        Bitmap moviePoster = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        imageViewPoster.setImageBitmap(moviePoster);
        textViewOverview.setText(extras.getString("string_overview"));
        movieName=extras.getString("movieTitle");
        textViewTitle.setText(movieName);
        textViewOriginalTitle.setText(extras.getString("movieOriginalTitle"));
        textViewReleaseDate.setText(extras.getString("movieReleaseDate"));

        buttonLaunchGoogle.setOnClickListener(this);
        buttonLike.setOnClickListener(this);
        buttonDislike.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view==buttonLaunchGoogle){
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
        if(view==buttonLike){
            Toast.makeText(ExploreMovieDetails.this, "You liked this movie",
                    Toast.LENGTH_LONG).show();
        }
        if(view==buttonDislike){
            Toast.makeText(ExploreMovieDetails.this, "You disliked this movie",
                    Toast.LENGTH_LONG).show();
        }

    }
}