package MediaContent;

import Enums.Language;

public abstract class MediaContent {
    /*
      "CREATE TABLE IF NOT EXISTS Movies (" +
                        "movie_id TEXT PRIMARY KEY," +
                        "title TEXT NOT NULL," +
                        "duration INTEGER NOT NULL," +
                        "genre TEXT NOT NULL," +
                        "age_restriction INTEGER DEFAULT 0," +
                        "price REAL NOT NULL," +
                        "subtitles TEXT);",

                // Advertisements table
                "CREATE TABLE IF NOT EXISTS Advertisements (" +
                        "ad_id TEXT PRIMARY KEY," +
                        "title TEXT NOT NULL," +
                        "duration INTEGER NOT NULL," +
                        "age_restriction INTEGER DEFAULT 0," +
                        "subtitles TEXT);",
    * */
    protected String id;
    protected String title;
    protected int duration;
    protected int ageRestriction;
    protected Language subtitles;
    public MediaContent(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getAgeRestriction() {
        return ageRestriction;
    }

    public void setAgeRestriction(int ageRestriction) {
        this.ageRestriction = ageRestriction;
    }

    public Language getSubtitles() {
        return subtitles;
    }

    public void setSubtitles(Language subtitles) {
        this.subtitles = subtitles;
    }
    public abstract void display();
}
