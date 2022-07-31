package com.ansori.movieku.request;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ansori.movieku.models.MovieModel;
import com.ansori.movieku.response.PopularMovieResponses;
import com.ansori.movieku.utils.AppExecutor;
import com.ansori.movieku.utils.Credentials;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

public class PopularMovieApiClient {
    private static PopularMovieApiClient instance;
    public static PopularMovieApiClient getInstance() {
        if (instance == null) {
            instance = new PopularMovieApiClient();
        }
        return instance;
    }

    // live data
    private final MutableLiveData<List<MovieModel>> popularMovieLiveData;

    // global variable for runnable
    private PopularRunnable popularRunnable;

    private PopularMovieApiClient() {
        popularMovieLiveData = new MutableLiveData<>();
    }

    public LiveData<List<MovieModel>> getPopularMovie() {
        return popularMovieLiveData;
    }

    public void getPopularMovie(int page) {
        if (popularRunnable != null) {
            popularRunnable = null;
        }

        popularRunnable = new PopularRunnable(page);

        final Future handler = AppExecutor.getInstance().getNetworkIO().submit(popularRunnable);

        AppExecutor.getInstance().getNetworkIO().schedule(() -> {
            // canceling retrofit call
            handler.cancel(true);
        },3000, TimeUnit.MILLISECONDS);
    }

    // retrieve data from rest api using runnable
    private class PopularRunnable implements Runnable {

        private final int page;
        boolean cancleRequest;

        public PopularRunnable(int page) {
            this.page = page;
            cancleRequest = false;
        }

        @Override
        public void run() {
            // getting request
            try {
                Response response = getPopularMovie(page).execute();

                if (cancleRequest) {
                    return;
                }

                if (response.isSuccessful()) {
                    if (response.code() == 200) {
                        assert response.body() != null;
                        List<MovieModel> popularMovieList = new ArrayList<>(((PopularMovieResponses) response.body()).getMovies());

                        if (page == 1) {
                            // send data to live data
                            // post value -> using banckground thread
                            // set value
                            popularMovieLiveData.postValue(popularMovieList);
                        } else {
                            List<MovieModel> currentMovie = popularMovieLiveData.getValue();
                            assert currentMovie != null;
                            currentMovie.addAll(popularMovieList);
                            popularMovieLiveData.postValue(currentMovie);
                        }
                    } else {
                        assert response.errorBody() != null;
                        System.out.println(response.errorBody().string());
                        popularMovieLiveData.postValue(null);
                    }
                } else {
                    System.out.println("request isnt successful");
                }
            } catch (IOException e) {
                System.out.println(e);
                popularMovieLiveData.postValue(null);
            }
        }

        // method getPopularMovie
        private Call<PopularMovieResponses> getPopularMovie(int page) {
            return ApiService.getMovieApi().getPopularMovie(Credentials.KEY,page);
        }
    }
//    // live data
//    private MutableLiveData<List<MovieModel>> popularMovieLiveData;
//
//    private static MovieApiClient instance;
//
//    // global retrieve runnable variable
//    RetrievePopularMovieRunnable retrievePopularMovieRunnable;
//
//    public static MovieApiClient getInstance(){
//        if(instance == null){
//            instance = new MovieApiClient();
//        } return instance;
//    }
//
//    // 1. method to call trough classes
//    public void getPopolarMovies(int page){
//        if (retrievePopularMovieRunnable != null) {
//            retrievePopularMovieRunnable = null;
//        }
//
//        retrievePopularMovieRunnable = new RetrievePopularMovieRunnable(page);
//
//        final Future handler = AppExecutor.getInstance().getNetworkIO().submit(retrievePopularMovieRunnable);
////        AppExecutor.getInstance().getNetworkIO().schedule((Runnable) () -> {
////
////        })
//
//        // calling retrofit call
//
//    }
//
//    // retrieve data from REST API by runnable
//    private class RetrievePopularMovieRunnable implements Runnable{
//
//        private final int page;
//        private final boolean cancleRequest;
//
//        private RetrievePopularMovieRunnable(int page) {
//            this.page = page;
//            cancleRequest = false;
//        }
//
//        @Override
//        public void run() {
//            // getting response object
//            try{
//                Response popularMovieResponse = getPopularMovies(page).execute();
//
//                if(cancleRequest){
//                    return;
//                }
//
//                if(popularMovieResponse.code() == 200) {
//                    assert popularMovieResponse.body() != null;
//                    List<MovieModel> popularList = new ArrayList<>(((MovieResponses)popularMovieResponse.body()).getMovies());
//
//                    if (page == 1) {
//                        // sending data to live data
//                        popularMovieLiveData.postValue(popularList);
//                    } else {
//                        List<MovieModel> currentPopularMovies = popularMovieLiveData.getValue();
//                        assert currentPopularMovies != null;
//                        currentPopularMovies.addAll(popularList);
//                        popularMovieLiveData.postValue(currentPopularMovies);
//                    }
//                } else {
//                    assert popularMovieResponse.errorBody() != null;
//                    String error = popularMovieResponse.errorBody().string();
//                    Log.v("tag","Error "+error);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                popularMovieLiveData.postValue(null);
//            }
//        }
//
//        // method get popular movie
//        private Call<MovieResponses> getPopularMovies(int page){
//            return ApiService.getMovieApi().getPopularMovie(Credentials.KEY,page);
//        }
//    }
}
