package MediaContent.Movie;

public class MovieMaker {
    private IMovieBuilder iMovieBuilder;
    public MovieMaker(IMovieBuilder iMovieBuilder){this.iMovieBuilder = iMovieBuilder;}

    public void makeMovie(String title, int duration, String genre, int ageRestriction, double price){
        this.iMovieBuilder.setTitle(title);
        this.iMovieBuilder.setDuration(duration);
        this.iMovieBuilder.setGenre(genre);
        this.iMovieBuilder.setAgeRestriction(ageRestriction);
        this.iMovieBuilder.setPrice(price);
        this.iMovieBuilder.setSubtitles();
    }
    public Movie getMovie(){
        Movie movie = this.iMovieBuilder.getMovie();
        this.iMovieBuilder.reset();
        return movie;
    }
}
