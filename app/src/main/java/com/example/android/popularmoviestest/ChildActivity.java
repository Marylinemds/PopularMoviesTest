package com.example.android.popularmoviestest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by Maryline on 2/8/2018.
 */

public class ChildActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        Movie movie;

        ImageView backdropImage = (ImageView) findViewById(R.id.backdrop_image);
        ImageView moviePoster = (ImageView) findViewById(R.id.movie_poster);
        TextView movieTitle_tv = (TextView) findViewById(R.id.movie_title);
        TextView releaseDate_tv = (TextView) findViewById(R.id.release_date);
        TextView overview_tv = (TextView) findViewById(R.id.overview);

        Intent startChildActivityIntent = getIntent();

        if (startChildActivityIntent != null) {
            if (startChildActivityIntent.hasExtra("MyClass")) {
                movie = startChildActivityIntent.getParcelableExtra("MyClass");

                String backdropPath = movie.getBackdropPath();
                String posterPath = movie.getPosterPath();
                String movieTitle = movie.getTitle();
                String releaseDate = movie.getReleaseDate();
                String overview = movie.getOverview();

                Picasso.with(this).load("http://image.tmdb.org/t/p/" + "w600" + backdropPath).into(backdropImage);
                Picasso.with(this).load("http://image.tmdb.org/t/p/" + "w185" + posterPath).into(moviePoster);
                movieTitle_tv.setText(movieTitle);
                releaseDate_tv.setText(releaseDate.substring(0, 4));
                overview_tv.setText(overview);

            }
            }
        }

}
