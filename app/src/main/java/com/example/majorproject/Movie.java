package com.example.majorproject;

import android.graphics.Bitmap;

/*Class to hold details about the movie, with method to retrieve each attribute of movie
 * */
public class Movie {
    String movie_title;
    String movie_original_title;
    String language;
    String overview;
    String release_date;
    String poster_url;
    Bitmap movie_poster;

    public Movie(String movie_title, String movie_original_title, String language, String overview, String release_date, String poster_url) {
        this.movie_title = movie_title;
        this.movie_original_title = movie_original_title;
        this.language = language;
        this.overview = overview;
        this.release_date = release_date;
        this.poster_url = poster_url;
    }

    public String getTitle() {
        return movie_title;
    }

    public String getOriginalTitle() {
        return movie_original_title;
    }

    public String getLanguage() {
        return language;
    }

    public String getReleaseDate() {
        return release_date;
    }

    public String getPosterUrl() {
        return poster_url;
    }

    public String getOverview() {
        return overview;
    }

    public Bitmap getPoster() {
        return movie_poster;
    }

    public void setPoster(Bitmap movie_poster) {
        this.movie_poster = movie_poster;
    }
}
