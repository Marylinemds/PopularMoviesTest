package com.example.android.popularmoviestest;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.android.popularmoviestest.Utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


import static android.R.id.list;

public class MainActivity extends AppCompatActivity implements MovieAdapter.ListItemClickHandler{

    RecyclerView mMoviesList;
    MovieAdapter movieAdapter;

    Toolbar toolbar;
    boolean searchMode;
    String searchText;
    int pageCount;

    private EndlessRecyclerViewScrollListener scrollListener;

    List<Movie> movies = new ArrayList<>();
    public List<Movie> HELPmovies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchMode = false;
        pageCount = 1;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {


            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list





                    URL SearchUrl = NetworkUtils.buildUrlFromPage(page);
                    new TheMovieAsyncTask().execute(SearchUrl);




                /*
                if (searchMode) {
                    searchText = searchText.toLowerCase();
                    List<Movie> newList = new ArrayList<>();


                    for (Movie movie : HELPmovies) {
                        String title = movie.getTitle().toLowerCase();

                        if (title.contains(searchText)) {
                            newList.add(movie);
                        }
                    }

                    movieAdapter.setFilter(newList);
                    scrollListener.resetState();
                }
                */
            }

        };

        // Adds the scroll listener to RecyclerView
        mMoviesList.addOnScrollListener(scrollListener);

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

                    if(!searchMode) {

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
                           // HELPmovies.add(movie);

                            movieAdapter.setMovies(movies);
                            movieAdapter.notifyDataSetChanged();

                        }
                    }else{

                        //movies.clear();
                        //HELPmovies.clear();
                        //iterate through JSON object and set fields to strings
                        for (int i = 0; i < results.length(); i++) {

                            Movie movie;

                            JSONObject resultsData = results.getJSONObject(i);

                            String title = resultsData.getString("original_title");

                            if (title.toLowerCase().contains(searchText)) {

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

                                //movies.add(movie);
                                HELPmovies.add(movie);
                            }

                        }
                        //If we don't have at leat one result, onLoadMore will not be triggered

                        if(HELPmovies.isEmpty()){

                            pageCount ++;

                            URL SearchUrl2 = NetworkUtils.buildUrlFromPage(pageCount);
                            new TheMovieAsyncTask().execute(SearchUrl2);
                        }

                        movieAdapter.setMovies(HELPmovies);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_item, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);


        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener()
        {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                //Load again the infos when coming back to the home page.
                HELPmovies.clear();

                if(!hasFocus) {
                    movieAdapter.setMovies(movies);
                    movieAdapter.notifyDataSetChanged();
                    searchMode = false;

                }


               // Toast.makeText(getApplicationContext(), String.valueOf(hasFocus),Toast.LENGTH_SHORT).show();
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //scrollListener.resetState();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //scrollListener.resetState();

                HELPmovies.clear();
                pageCount = 1;
                searchText = newText;
                searchMode = true;


                URL SearchUrl = NetworkUtils.buildUrl();
                new TheMovieAsyncTask().execute(SearchUrl);



/*
               if (HELPmovies.size() == 0 && newText.length()>0){
                    int i = 2;
                    URL SearchUrl2 = NetworkUtils.buildUrlFromPage(i);
                    new TheMovieAsyncTask().execute(SearchUrl2);

                }

*/



                //movies = new ArrayList<>(new LinkedHashSet<>(movies));

                /*newText = newText.toLowerCase();
                List<Movie> newList = new ArrayList<>();


                    for (Movie movie : HELPmovies) {
                        String title = movie.getTitle().toLowerCase();

                        if (title.contains(newText)) {
                            newList.add(movie);
                        }
                    }

                    movieAdapter.setFilter(newList);
                    scrollListener.resetState();



*/

                return true;
            }

        });

        return true;
    }
}
