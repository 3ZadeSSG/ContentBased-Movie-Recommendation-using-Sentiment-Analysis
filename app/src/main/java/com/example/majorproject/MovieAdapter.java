package com.example.majorproject;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/*Inherited from ArrayAdapter to reduce memory overhead for layout which shows a list.
Works by recycling views to create new one when scrolling
* */
public class MovieAdapter extends ArrayAdapter<Movie> {
    public MovieAdapter(Activity context, ArrayList<Movie> arr) {
        super(context, 0, arr);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //check if there is an existing list item view called convertView
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.movie_list, parent, false);
        }
        Movie currentMovie = getItem(position);
        TextView movieTitle = listItemView.findViewById(R.id.textViewMovieTitle);
        ImageView moviePoster = listItemView.findViewById(R.id.imageViewPoster);
        movieTitle.setText(currentMovie.getTitle());
        moviePoster.setImageBitmap(currentMovie.getPoster());
        return listItemView;
    }
}
