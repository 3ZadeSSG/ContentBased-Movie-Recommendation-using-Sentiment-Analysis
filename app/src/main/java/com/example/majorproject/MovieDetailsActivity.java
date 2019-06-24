package com.example.majorproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class MovieDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    Bitmap moviePoster;
    ImageView posterImageView;
    TextView movieRating;
    TextView movieTitleTextView;
    CircleDisplay cd;
    String movieTitle;
    float score;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Bundle extras = getIntent().getExtras();
        byte[] byteArray = extras.getByteArray("moviePoster");
        moviePoster = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        posterImageView=(ImageView)findViewById(R.id.image_view);
        posterImageView.setImageBitmap(moviePoster);
        movieRating=findViewById(R.id.textViewMovieRating);

        String tempRating=extras.getString("movieRating");
        movieRating.setText("Rating: "+tempRating);
        movieTitleTextView=findViewById(R.id.movieTitleTextView);
        movieTitle=extras.getString("movieTitle");
        movieTitleTextView.setText(movieTitle);
        cd = (CircleDisplay) findViewById(R.id.circleDisplay);
        score = Float.parseFloat((tempRating.split("/"))[0]);
        showCircle();
        posterImageView.setOnClickListener(this);
        movieTitleTextView.setOnClickListener(this);
    }
    void showCircle(){
        cd.setAnimDuration(3000);
        cd.setValueWidthPercent(55f);
        cd.setTextSize(16f);
        score=score*10;
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
        if(view==posterImageView){
            callGooogle();
        }
        if(view==movieTitleTextView){
            callGooogle();
        }
    }
    public void callGooogle(){
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
}
