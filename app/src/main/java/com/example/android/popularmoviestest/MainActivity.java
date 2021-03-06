package com.example.android.popularmoviestest;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
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

import com.example.android.popularmoviestest.Utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity implements MovieAdapter.ListItemClickHandler{

    //Activity variables
    boolean searchMode;
    int pageCount;

    Handler mHandler;
    Toolbar toolbar;
    RecyclerView mMoviesList;
    MovieAdapter movieAdapter;
    String searchText;

    private EndlessRecyclerViewScrollListener scrollListener;

    List<Movie> movies = new ArrayList<>();
    List<Movie> filteredMovies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchMode = false;
        pageCount = 1;
        mHandler = new Handler(Looper.getMainLooper());

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

        // Retain an instance so that we can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {


            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list


                URL SearchUrl = NetworkUtils.buildUrlFromPage(page+1);
                new TheMovieAsyncTask().execute(SearchUrl);

            }

        };

        // Adds the scroll listener to RecyclerView
        mMoviesList.addOnScrollListener(scrollListener);

        URL SearchUrl = NetworkUtils.buildUrl();
        new TheMovieAsyncTask().execute(SearchUrl);


    }

    private class TheMovieAsyncTask extends AsyncTask<URL, Void, String> {

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

                        filteredMovies.clear();
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
                    }else{

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

                                    filteredMovies.add(movie);
                            }
                        }
                        //If we don't have at least one result, onLoadMore will not be triggered

                        if(filteredMovies.isEmpty()){

                            pageCount ++;

                            URL SearchUrl2 = NetworkUtils.buildUrlFromPage(pageCount);
                            new TheMovieAsyncTask().execute(SearchUrl2);
                        }
                        movieAdapter.setMovies(filteredMovies);
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
        searchView.setQueryHint(getString(R.string.query_hint));

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener()
        {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                //Load again the infos when coming back to the home page.

                if(!hasFocus) {
                    filteredMovies.clear();
                    movieAdapter.setMovies(movies);
                    movieAdapter.notifyDataSetChanged();
                    searchMode = false;

                }else{
                    searchMode = true;
                    movies.clear();
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                scrollListener.resetState();

                    filteredMovies.clear();
                    pageCount = 1;
                    searchText = newText;

                //The Handler allow the app not going crazy and duplicate the search results when typing too fast.
                mHandler.removeCallbacksAndMessages(null);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        URL SearchUrl = NetworkUtils.buildUrl();
                        new TheMovieAsyncTask().execute(SearchUrl);
                    }
                }, 1100);
                    return true;
                }
        });
        return true;
    }
}
