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
    TextView userEmailDisplay;
    FirebaseAuth firebaseAuth;
    Fragment exploreFragment;
    Fragment sentimentFragment;
    Fragment analyzerFragment;
    Fragment ratingFragment;
    Fragment recommendationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            Intent signupActivity = new Intent(MainActivity.this, Signup.class);
            startActivity(signupActivity);
        } else {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            userEmailDisplay.setText(user.getEmail());
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new MovieRatingSearchFragment()).commit();
            Log.v("Fragment:", "\n\t\t\t====================" + "ON RESUME");
            navigationView.setCheckedItem(R.id.nav_movie_rating_search);
        }
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        exploreFragment=new ExploreFragment();
        sentimentFragment=new SentimentPredictionFragment();
        analyzerFragment=new TwitterAnalyzerFragment();
        ratingFragment=new MovieRatingSearchFragment();
        recommendationFragment=new RecommendationFragment();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
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
                result=new MovieRatingSearchFragment();
                break;
            case "recommendation":
                result=new RecommendationFragment();
                break;

        }
        Log.d("Fragment", "create: ===================" + result.toString());
        return result;
    }
    /*
    private void openFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);
        if (existingFragment != null) {
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
            fragmentTransaction.hide(currentFragment);
            fragmentTransaction.show(existingFragment);
        }
        else {
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
            fragmentTransaction.hide(currentFragment);
            fragmentTransaction.add(R.id.fragment_container, fragment, tag);
        }
        fragmentTransaction.commit();
    }*/
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();
        switch(id){
            case R.id.nav_exit:
            {
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
            case R.id.nav_logout:
            {
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
            case R.id.nav_sentiment_prediction:
            {
                //openFragment(sentimentFragment,"sentiment");
                replaceFragment("sentiment");
            }
            break;
            case R.id.nav_movie_rating_search:
            {
                //openFragment(ratingFragment,"rating");
                replaceFragment("rating");
            }
            break;
            case R.id.nav_twitter_analyzer:
            {
                //openFragment(analyzerFragment,"analyzer");
                replaceFragment("analyzer");
            }
            break;
            case R.id.nav_explore:
            {
                //openFragment(exploreFragment,"explore");
                replaceFragment("explore");
            }
            break;
            case R.id.nav_recommendation:
            {
                replaceFragment("recommendation");
            }
            break;
        }

        /*
        if (id == R.id.nav_exit) {
            //exit app
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

        } else if (id == R.id.nav_logout) {
            //logout current user
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
        } else if (id == R.id.nav_sentiment_prediction) {
            //launch sentiment search fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new SentimentPredictionFragment()).commit();

        } else if (id == R.id.nav_movie_rating_search) {
            //launch movie rating search fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new MovieRatingSearchFragment()).commit();

        } else if (id == R.id.nav_twitter_analyzer) {
            //launch twitter analyzer fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new TwitterAnalyzerFragment()).commit();

        } else if (id == R.id.nav_explore) {
            //launch explore fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ExploreFragment()).commit();
        }
        */
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackStackChanged() {

        try {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            fragment.onResume();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
