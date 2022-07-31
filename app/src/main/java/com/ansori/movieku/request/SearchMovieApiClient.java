package com.ansori.movieku.request;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ansori.movieku.models.MovieModel;
import com.ansori.movieku.response.PopularMovieResponses;
import com.ansori.movieku.response.SearchMovieResponses;
import com.ansori.movieku.utils.AppExecutor;
import com.ansori.movieku.utils.Credentials;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

public class SearchMovieApiClient {
    private static SearchMovieApiClient instance;
    public static SearchMovieApiClient getInstance() {
        if (instance == null) {
            instance = new SearchMovieApiClient();
        }
        return instance;
    }

    // live data
    private final MutableLiveData<List<MovieModel>> searchMovieLiveData;

    // global variable for runnable
    private SearchRunnable searchRunnable;

    private SearchMovieApiClient() {
        searchMovieLiveData = new MutableLiveData<>();
    }

    public LiveData<List<MovieModel>> getSearchMovie() {
        return searchMovieLiveData;
    }

    public void getSearchMovie(String query,int page) {
        if (searchRunnable != null) {
            searchRunnable = null;
        }

        searchRunnable = new SearchRunnable(query,page);

        final Future handler = AppExecutor.getInstance().getNetworkIO().submit(searchRunnable);

        AppExecutor.getInstance().getNetworkIO().schedule(() -> {
            // canceling retrofit call
            handler.cancel(true);
        },3000, TimeUnit.MILLISECONDS);
    }

    // retrieve data from rest api using runnable
    private class SearchRunnable implements Runnable {

        private String query;
        private final int page;
        boolean cancleRequest;

        public SearchRunnable(String query, int page) {
            this.query = query;
            this.page = page;
            cancleRequest = false;
        }

        @Override
        public void run() {
            // getting request
            try {
                Response response = searchMovie(query,page).execute();

                if (cancleRequest) {
                    return;
                }

                if (response.isSuccessful()) {
                    if (response.code() == 200) {
                        assert response.body() != null;
                        List<MovieModel> searchMovieList = new ArrayList<>(((SearchMovieResponses) response.body()).getMovies());

                        if (page == 1) {
                            // send data to live data
                            // post value -> using banckground thread
                            // set value
                            searchMovieLiveData.postValue(searchMovieList);
                        } else {
                            List<MovieModel> currentMovie = searchMovieLiveData.getValue();
                            assert currentMovie != null;
                            currentMovie.addAll(searchMovieList);
                            searchMovieLiveData.postValue(currentMovie);
                        }
                    } else {
                        assert response.errorBody() != null;
                        System.out.println(response.errorBody().string());
                        searchMovieLiveData.postValue(null);
                    }
                } else {
                    System.out.println("request isnt successful");
                }
            } catch (IOException e) {
                System.out.println(e);
                searchMovieLiveData.postValue(null);
            }
        }

        // method getPopularMovie
        private Call<SearchMovieResponses> searchMovie(String query,int page) {
            return ApiService.getMovieApi().searchMovie(Credentials.KEY,query,page);
        }
    }
}
