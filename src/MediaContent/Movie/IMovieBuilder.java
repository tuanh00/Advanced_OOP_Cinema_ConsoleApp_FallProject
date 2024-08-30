package MediaContent.Movie;

public interface IMovieBuilder {
 void setTitle(String var1);
 void setDuration(int var2);
 void setGenre(String var3);
 void setAgeRestriction(int var4);
 void setPrice(double var5);
 void setSubtitles();

 Movie getMovie();
 void reset();

}
