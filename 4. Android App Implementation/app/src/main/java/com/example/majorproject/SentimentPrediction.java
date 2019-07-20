package com.example.majorproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

/*Sentiment Prediction activity to call the Server on typed text*/
public class SentimentPrediction extends AppCompatActivity {
    String sentiment_response;
    TextView result;
    CircleDisplay cd;
    float score;

    /*By default method called by Android when new view gets created*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentiment_prediction);
        cd = findViewById(R.id.circleDisplay);
        Intent i = getIntent();

        //Parse the response received from the parent activity(Sentiment Prediction Fragment)
        sentiment_response = i.getStringExtra("response");
        result = findViewById(R.id.textViewResult);

        //call method to show result
        showResult();
    }

    /*Show the prediction result with a Circular animation on a scale of 1-100*/
    void showResult() {
        try {
            JSONObject root = new JSONObject(sentiment_response);
            String s = null;
            s = root.getString("result ");
            Log.v("Result SSSS:", "\n\t\t\t====================" + s);
            String[] temp = s.split(" ");
            score = Float.parseFloat(temp[0]);
            result.setText(s);
            showCircle();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void showCircle() {
        cd.setAnimDuration(3000);
        cd.setValueWidthPercent(55f);
        cd.setTextSize(36f);
        score = score * 100;
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
