package MediaContent.Advertisement;

public class AdvertisementMaker {
    private IAdvertisementBuilder iAdvertisementBuilder;
    public AdvertisementMaker(IAdvertisementBuilder iAdvertisementBuilder){
        this.iAdvertisementBuilder = iAdvertisementBuilder;
    }
    public void makeAdvertisement(String title, int duration, int ageRestriction) {
        this.iAdvertisementBuilder.setTitle(title);
        this.iAdvertisementBuilder.setDuration(duration);
        this.iAdvertisementBuilder.setAgeRestriction(ageRestriction);
        this.iAdvertisementBuilder.setSubtitles();
    }
    public Advertisement getAdvertisement(){
        Advertisement advertisement = this.iAdvertisementBuilder.getAdvertisement();
        this.iAdvertisementBuilder.reset();
        return advertisement;
    }

}
