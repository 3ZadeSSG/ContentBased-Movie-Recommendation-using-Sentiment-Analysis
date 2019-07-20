package com.example.majorproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FragmentManager.OnBackStackChangedListener {
    //TextView to show user Email Address
    TextView userEmailDisplay;

    //Firebase Authentication object to be used to check or logout current user
    FirebaseAuth firebaseAuth;

    //Each fragments which can be accessed form navigation bar
    Fragment exploreFragment;
    Fragment sentimentFragment;
    Fragment analyzerFragment;
    Fragment ratingFragment;
    Fragment recommendationFragment;

    /* Default method called by android os when a new view is created*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize each layout variables
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        userEmailDisplay = headerView.findViewById(R.id.textViewUserEmailDisplay);

        //Check if user is logged in, if not then kill the activity and redirect to registration screen
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            Intent signupActivity = new Intent(MainActivity.this, Signup.class);
            startActivity(signupActivity);
        } else {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            userEmailDisplay.setText(user.getEmail());
        }

        //if there is not saved fragment then begin with Movie Rating search fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new MovieRatingSearchFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_movie_rating_search);
        }
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        //Initialize each Fragment objects in advance to be called whenever navigation bar is used to switch between them
        exploreFragment = new ExploreFragment();
        sentimentFragment = new SentimentPredictionFragment();
        analyzerFragment = new TwitterAnalyzerFragment();
        ratingFragment = new MovieRatingSearchFragment();
        recommendationFragment = new RecommendationFragment();

    }

    /*When back button is pressed perform drawer closing task if it is open
     * Otherwise default back button task*/
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*Method the replace the current fragment with another one form list,
    if another fragment is not already there then create one
    * */
    private void replaceFragment(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        Fragment nextFragment = fragmentManager.findFragmentByTag(tag);

        Log.d("Fragment", "f detached: " + currentFragment.toString());
        transaction.detach(currentFragment);

        if (nextFragment == null) {
            nextFragment = createFragment(tag);
            transaction.add(R.id.fragment_container, nextFragment, tag);
        } else {
            Log.d("Fragment", "f attach: " + nextFragment.toString());
            transaction.attach(nextFragment);
        }
        transaction.commit();
    }

    /*Method to create fragment with its associated tag*/
    private Fragment createFragment(String tag) {
        Fragment result = null;
        switch (tag) {
            case "explore":
                result = new ExploreFragment();
                break;
            case "sentiment":
                result = new SentimentPredictionFragment();
                break;
            case "analyzer":
                result = new TwitterAnalyzerFragment();
                break;
            case "rating":
                result = new MovieRatingSearchFragment();
                break;
            case "recommendation":
                result = new RecommendationFragment();
                break;

        }
        return result;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            //if exit button is pressed then cross check the action via showing alert dialog
            case R.id.nav_exit: {
                new AlertDialog.Builder(this)
                        .setTitle("Please conform")
                        .setMessage("Do you really want to exit?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //perform exit
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_HOME);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", null).show();
            }
            break;
            case R.id.nav_logout: {
                //if logout button is pressed then cross check the action via showing alert dialog
                new AlertDialog.Builder(this)
                        .setTitle("Please conform")
                        .setMessage("Do you really want to logout?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                //perform logout activity here
                                firebaseAuth.signOut();
                                finish();
                                Intent signupScreen = new Intent(MainActivity.this, Signup.class);
                                startActivity(signupScreen);
                            }
                        })
                        .setNegativeButton("No", null).show();
            }
            break;
            case R.id.nav_sentiment_prediction: {
                //Replace current fragment with Sentiment Analysis on Text Fragment
                replaceFragment("sentiment");
            }
            break;
            case R.id.nav_movie_rating_search: {
                //Replace current fragment with Movie Rating Search Fragment
                replaceFragment("rating");
            }
            break;
            case R.id.nav_twitter_analyzer: {
                //Replace current fragment with Twitter Analyzer Fragment
                replaceFragment("analyzer");
            }
            break;
            case R.id.nav_explore: {
                //Replace current fragment with Explore Fragment to show trending movies
                replaceFragment("explore");
            }
            break;
            case R.id.nav_recommendation: {
                //Replace current fragment with Recommendation Fragment to show recommended movies
                replaceFragment("recommendation");
            }
            break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*On back button pressed form another child activity, show the fragment in resumed state*/
    @Override
    public void onBackStackChanged() {
        try {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            fragment.onResume();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //Set Action bar title to the passed string
    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
