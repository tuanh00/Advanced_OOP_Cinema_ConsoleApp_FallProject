package MediaContent.Advertisement;

public interface IAdvertisementBuilder {
    void setTitle(String var1);
    void setDuration(int var2);
    void setAgeRestriction(int var3);
    void setSubtitles();
    Advertisement getAdvertisement();
    void reset();
}
