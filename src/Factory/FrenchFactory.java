package Factory;

import Enums.Language;
import MediaContent.Advertisement.Advertisement;
import MediaContent.Advertisement.AdvertisementMaker;
import MediaContent.Advertisement.FrenchAdvertisementBuilder;
import MediaContent.Advertisement.IAdvertisementBuilder;
import MediaContent.Movie.FrenchMovieBuilder;
import MediaContent.Movie.IMovieBuilder;
import MediaContent.Movie.Movie;
import MediaContent.Movie.MovieMaker;

public class FrenchFactory extends AbstractFactory{
    @Override
    public Movie createMovie(String title, int duration, String genre, int ageRestriction, double price) {
        IMovieBuilder frenchMovieBuilder = new FrenchMovieBuilder();
        MovieMaker movieMaker = new MovieMaker(frenchMovieBuilder);
        movieMaker.makeMovie(title, duration, genre, ageRestriction, price);
        return movieMaker.getMovie();
    }

    @Override
    public Advertisement createAdvertisement(String title, int duration, int ageRestriction) {
        IAdvertisementBuilder frenchAdvertisementBuilder = new FrenchAdvertisementBuilder();
        AdvertisementMaker advertisementMaker = new AdvertisementMaker(frenchAdvertisementBuilder);
        advertisementMaker.makeAdvertisement(title, duration, ageRestriction);
        return advertisementMaker.getAdvertisement();
    }
}
