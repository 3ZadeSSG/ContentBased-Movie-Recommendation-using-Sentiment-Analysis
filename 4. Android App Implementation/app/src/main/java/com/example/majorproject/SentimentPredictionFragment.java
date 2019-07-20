package com.example.majorproject;

import android.content.Context;
import android.content.Intent;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

public class SentimentPredictionFragment extends Fragment implements View.OnClickListener {
    String address;
    EditText inputBox;
    ProgressBar progressBar;
    LinearLayout details;
    Button buttonSumbitQueryText;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_predict_sentiment, container, false);

        progressBar = view.findViewById(R.id.spin_kit);
        inputBox = view.findViewById(R.id.editTextQueryBox);
        details = view.findViewById(R.id.details);
        DoubleBounce myProgressBar = new DoubleBounce();
        progressBar.setIndeterminateDrawable(myProgressBar);
        buttonSumbitQueryText = view.findViewById(R.id.buttonSentimentQuery);
        buttonSumbitQueryText.setOnClickListener(this);
        return view;
    }

    /*If fragment is resumed from a paused state call super and set the title to current "Sentiment Prediction"
     */
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity())
                .setActionBarTitle("Sentiment Prediction");
    }


    @Override
    public void onClick(View view) {
        //if query button is touched then call the API function to perform the request
        if (view == buttonSumbitQueryText) {
            String[] input = inputBox.getText().toString().split(" ");
            String review = "";
            for (int i = 0; i < input.length - 1; i++) {
                review += input[i] + "+";
            }
            review += input[input.length - 1];

            //append the query string at the end of address
            address = "https://major-project-final-246818.appspot.com/sentiment/";

            SentimentAsyncTask task = new SentimentAsyncTask();
            String url = address;
            url = url + review;
            task.execute(url);
        }
    }

    /*Function to check if network is available*/
    public boolean isNetworkAvailable() {
        try {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            return mNetworkInfo != null;

        } catch (NullPointerException e) {
            return false;

        }
    }


    public void doActions(String result) {
        if (!result.equals("Error")) {
            //if there was no error then call the result activity to show prediction result
            try {
                Intent resultActivity = new Intent(getActivity(), SentimentPrediction.class);
                resultActivity.putExtra("response", result);
                startActivity(resultActivity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //Else show the server error on alert dialog
            showAlertDialog("Error", "Server Error!");
            progressBar.setVisibility(View.INVISIBLE);
            details.setVisibility(View.VISIBLE);
        }
    }

    /*Function to show the alert dialog, to be used for showing error or warnings*/
    public void showAlertDialog(String TITLE, String MESSAGE) {
        new AlertDialog.Builder(getActivity())
                .setTitle(TITLE)
                .setMessage(MESSAGE)
                .setNegativeButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /*Inherited from AsyncTask to perform background network request without affecting main OS thread*/
    public class SentimentAsyncTask extends AsyncTask<String, Void, String> {
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
                    InputStream inputStream;
                    HttpsURLConnection requestConnection = (HttpsURLConnection) url.openConnection();
                    requestConnection.setReadTimeout(200000);
                    requestConnection.setConnectTimeout(100000);
                    requestConnection.setRequestMethod("GET");
                    requestConnection.connect();
                    inputStream = requestConnection.getInputStream();
                    res = readFromStream(inputStream);
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
            progressBar.setVisibility(View.INVISIBLE);
            details.setVisibility(View.VISIBLE);
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
