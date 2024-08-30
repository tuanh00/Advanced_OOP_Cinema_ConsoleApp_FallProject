package MediaContent.Advertisement;

import MediaContent.MediaContent;

public class Advertisement extends MediaContent {

    private static int advertisementCounter = 1000;
    public Advertisement(){this.setId();}
    public void setId(){
        this.id = "AD" + advertisementCounter;
        ++advertisementCounter;
    }
    @Override
    public void display() {
        System.out.println("Advertisement ID: " + this.id);
        System.out.println("Content URL: " + this.title);
        System.out.println("Duration: " + this.duration + " seconds");
        System.out.println("Age Restriction: " + this.ageRestriction + "+");
        System.out.println("Subtitles: " + this.subtitles);
    }
}
