package MediaContent.Movie;

import Enums.Language;

public class FrenchMovieBuilder implements IMovieBuilder{
    Movie movie = new Movie();
    public FrenchMovieBuilder(){}
    @Override
    public void setTitle(String var1) {
        this.movie.setTitle(var1);
    }

    @Override
    public void setDuration(int var2) {
        this.movie.setDuration(var2);
    }

    @Override
    public void setGenre(String var3) {
        this.movie.setGenre(var3);
    }

    @Override
    public void setAgeRestriction(int var4) {
        this.movie.setAgeRestriction(var4);
    }

    @Override
    public void setPrice(double var5) {
        this.movie.setPrice(var5);
    }

    @Override
    public void setSubtitles() {
        this.movie.setSubtitles(Language.FR);
    }

    @Override
    public Movie getMovie() {
        return this.movie;
    }

    @Override
    public void reset() {
        this.movie = new Movie();
    }
}
