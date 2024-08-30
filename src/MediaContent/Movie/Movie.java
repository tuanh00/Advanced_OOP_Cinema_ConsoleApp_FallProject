package MediaContent.Movie;

import MediaContent.MediaContent;

public class Movie extends MediaContent {
    private static int movieCounter = 1000;
    private String genre;
    private double price;
    public Movie(){this.setId();}
    public void setId(){
        this.id = "MV" + movieCounter;
        ++movieCounter;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public void display() {
        System.out.println("Movie ID: " + this.id);
        System.out.println("Title: " + this.title);
        System.out.println("Duration: " + this.duration + " minutes");
        System.out.println("Genre: " + this.genre);
        System.out.println("Age Restriction: " + this.ageRestriction + "+");
        System.out.println("Price: $" + this.price);
        System.out.println("Subtitles: " + this.subtitles);
    }
}
