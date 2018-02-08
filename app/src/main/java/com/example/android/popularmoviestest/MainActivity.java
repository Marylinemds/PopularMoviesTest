package com.example.android.popularmoviestest;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.android.popularmoviestest.Utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MovieAdapter.ListItemClickHandler{

    RecyclerView mMoviesList;
    MovieAdapter movieAdapter;

    List<Movie> movies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMoviesList = (RecyclerView) findViewById(R.id.rv_images);
        GridLayoutManager layoutManager;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager = new GridLayoutManager(this, 2);
        } else {
            layoutManager = new GridLayoutManager(this, 4);
        }

        mMoviesList.setLayoutManager(layoutManager);
        movieAdapter = new MovieAdapter(this);
        mMoviesList.setAdapter(movieAdapter);

        URL SearchUrl = NetworkUtils.buildUrl();
        new TheMovieAsyncTask().execute(SearchUrl);

    }


    public class TheMovieAsyncTask extends AsyncTask<URL, Void, String> {


        @Override
        protected String doInBackground(URL... params) {

            URL url = params[0];
            String movieData = null;
            try {
                movieData = NetworkUtils.getResponseFromHttpUrl(url);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return movieData;

        }

        @Override
        protected void onPostExecute(String jsonData) {


            System.out.println("JSON " + jsonData);
            if (jsonData != null) {
                try {

                    JSONObject obj = new JSONObject(jsonData);
                    JSONArray results = obj.getJSONArray("results");


                        //iterate through JSON object and set fields to strings
                        for (int i = 0; i < results.length(); i++) {

                            Movie movie;

                            JSONObject resultsData = results.getJSONObject(i);

                            String title = resultsData.getString("original_title");
                            String overview = resultsData.getString("overview");
                            String releaseDate = resultsData.getString("release_date");
                            String id = resultsData.getString("id");
                            String posterPath = resultsData.getString("poster_path").replace("\\Tasks", "");
                            String backdropPath = resultsData.getString("backdrop_path").replace("\\Tasks", "");

                            movie = new Movie();
                            movie.setTitle(title);
                            movie.setOverview(overview);
                            movie.setReleaseDate(releaseDate);
                            movie.setId(id);
                            movie.setPosterPath(posterPath);
                            movie.setBackdropPath(backdropPath);

                            movies.add(movie);

                            movieAdapter.setMovies(movies);
                            movieAdapter.notifyDataSetChanged();
                        }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }}



    @Override
    public void onClick(Movie movie) {



        Context context = MainActivity.this;
        Class destinationActivity = ChildActivity.class;
        Intent startChildActivityIntent = new Intent(context, destinationActivity);
        startChildActivityIntent.putExtra("MyClass", movie);
        startActivity(startChildActivityIntent);



    }
}
