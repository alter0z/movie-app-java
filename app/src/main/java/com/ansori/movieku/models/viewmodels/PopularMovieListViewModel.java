package com.ansori.movieku.models.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.ansori.movieku.models.MovieModel;
import com.ansori.movieku.repositories.PopularMovieRepository;

import java.util.List;

public class PopularMovieListViewModel extends ViewModel {
    private PopularMovieRepository popularMovieRepository;

    public PopularMovieListViewModel() {
        popularMovieRepository = PopularMovieRepository.getInstance();
    }

    public LiveData<List<MovieModel>> getPopularMovie() {
        return popularMovieRepository.getPopularMovie();
    }

    public void getPopularMovie(int page) {
        popularMovieRepository.getPopularMovie(page);
    }

    // next page
    public void popularMovieNextPage() {
        popularMovieRepository.popularMovieNextPage();
    }
}
