package Factory;

import MediaContent.Advertisement.Advertisement;
import MediaContent.Advertisement.AdvertisementMaker;
import MediaContent.Advertisement.EnglishAdvertisementBuilder;
import MediaContent.Advertisement.IAdvertisementBuilder;
import MediaContent.Movie.EnglishMovieBuilder;
import MediaContent.Movie.IMovieBuilder;
import MediaContent.Movie.Movie;
import MediaContent.Movie.MovieMaker;

public class EnglishFactory extends AbstractFactory{
    @Override
    public Movie createMovie(String title, int duration, String genre, int ageRestriction, double price) {
        IMovieBuilder englishMovieBuilder = new EnglishMovieBuilder();
        MovieMaker movieMaker = new MovieMaker(englishMovieBuilder);
        movieMaker.makeMovie(title, duration, genre, ageRestriction, price);
        return movieMaker.getMovie();
    }

    @Override
    public Advertisement createAdvertisement(String title, int duration, int ageRestriction) {
        IAdvertisementBuilder englishAdvertisementBuilder = new EnglishAdvertisementBuilder();
        AdvertisementMaker advertisementMaker = new AdvertisementMaker(englishAdvertisementBuilder);
        advertisementMaker.makeAdvertisement(title, duration, ageRestriction);
        return advertisementMaker.getAdvertisement();
    }
}
