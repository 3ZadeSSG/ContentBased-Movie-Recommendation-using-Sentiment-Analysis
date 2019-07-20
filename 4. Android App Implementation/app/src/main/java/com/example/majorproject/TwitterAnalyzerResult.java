package com.example.majorproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class TwitterAnalyzerResult extends AppCompatActivity {
    //Holders for values and layouts to view result of twitter analysis
    String twitter_analyzer_response;
    TextView result;
    CircleDisplay cd;
    float score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_analyzer_result);

        cd = findViewById(R.id.circleDisplay);
        Intent i = getIntent();

        //Get the result from parent activity(Twitter analyzer fragement)
        twitter_analyzer_response = i.getStringExtra("response");
        result = findViewById(R.id.textViewResultTwitterAnalyzer);
        showResult();
    }

    void showResult() {
        try {
            JSONObject root = new JSONObject(twitter_analyzer_response);
            String s = null;
            s = root.getString("result ");
            score = Float.parseFloat(s);
            String temp = "Positivity : " + s + " %";
            result.setText(temp);
            showCircle();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void showCircle() {
        cd.setAnimDuration(3000);
        cd.setValueWidthPercent(55f);
        cd.setTextSize(36f);
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
