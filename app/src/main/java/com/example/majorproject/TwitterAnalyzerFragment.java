package com.example.majorproject;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class TwitterAnalyzerFragment extends Fragment implements View.OnClickListener {
    Button buttonTwitterQuery;
    String address;
    ProgressBar progressBar;
    LinearLayout details;
    EditText inputBox;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_twitter_analyzer, container, false);

        details = view.findViewById(R.id.details);
        progressBar = view.findViewById(R.id.spin_kit);
        DoubleBounce myProgressBar = new DoubleBounce();
        progressBar.setIndeterminateDrawable(myProgressBar);

        inputBox = view.findViewById(R.id.editTextTwitterQuery);
        buttonTwitterQuery = view.findViewById(R.id.buttonTwitterAnalyzer);
        buttonTwitterQuery.setOnClickListener(this);
        return view;
    }

    /*If fragment is resumed from a paused state call super and set the title to current "Twitter Analyzer"
     */
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity())
                .setActionBarTitle("Twitter Analyzer");
    }

    /*For each click on views, call the necessary functions*/
    @Override
    public void onClick(View view) {
        if (view == buttonTwitterQuery) {
            String[] input = inputBox.getText().toString().split(" ");
            String query = "";
            for (int i = 0; i < input.length - 1; i++) {
                query += input[i] + "+";
            }
            query += input[input.length - 1];
            // API address + SearchString is final API function call address
            address = "https://major-project-final-246818.appspot.com/analyze/";

            //Call background thread to analyze thread
            TwitterAnalyzerFragment.TwitterAsyncTask task = new TwitterAnalyzerFragment.TwitterAsyncTask();
            String url = address;
            url = url + query;
            task.execute(url);
        }
    }

    /*Method to check if network is available*/
    public boolean isNetworkAvailable() {
        try {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            return mNetworkInfo != null;

        } catch (NullPointerException e) {
            return false;

        }
    }

    /*Perform retrieve of score from result if there is any else show error*/
    public void doActions(String result) {
        if (!result.equals("Error")) {
            try {
                Intent resultActivity = new Intent(getActivity(), TwitterAnalyzerResult.class);
                resultActivity.putExtra("response", result);
                startActivity(resultActivity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showAlertDialog("Error", "Server Error!");
            //hide progress bar and show the fragment again
            progressBar.setVisibility(View.INVISIBLE);
            details.setVisibility(View.VISIBLE);
        }
    }

    /*Alert dialog to show the warning, errors and warning messages*/
    public void showAlertDialog(String TITLE, String MESSAGE) {
        new AlertDialog.Builder(getActivity())
                .setTitle(TITLE)
                .setMessage(MESSAGE)
                .setNegativeButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /*Inherited AsyncTask class to peform network request in background without affecting OS thread*/
    public class TwitterAsyncTask extends AsyncTask<String, Void, String> {
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
                    //set timeouts for maximum wait before thread disconnects form server
                    requestConnection.setReadTimeout(2000000);
                    requestConnection.setConnectTimeout(1000000);
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
