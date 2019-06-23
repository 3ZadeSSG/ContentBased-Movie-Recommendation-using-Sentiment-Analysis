package com.example.majorproject;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;


public class MovieDetailsActivity extends AppCompatActivity {
    Bitmap moviePoster;
    ImageView posterImageView;
    TextView movieRating;
    TextView movieTitleTextView;
    CircleDisplay cd;
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
        movieTitleTextView.setText(extras.getString("movieTitle"));
        cd = (CircleDisplay) findViewById(R.id.circleDisplay);
        score = Float.parseFloat((tempRating.split("/"))[0]);
        showCircle();
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
}
