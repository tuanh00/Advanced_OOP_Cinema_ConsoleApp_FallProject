package MediaContent.Advertisement;

import Enums.Language;

public class EnglishAdvertisementBuilder implements IAdvertisementBuilder{
    Advertisement advertisement = new Advertisement();
    public EnglishAdvertisementBuilder(){}
    @Override
    public void setTitle(String var1) {
        this.advertisement.setTitle(var1);
    }

    @Override
    public void setDuration(int var2) {
        this.advertisement.setDuration(var2);
    }

    @Override
    public void setAgeRestriction(int var3) {
        this.advertisement.setAgeRestriction(var3);
    }

    @Override
    public void setSubtitles() {
        this.advertisement.setSubtitles(Language.EN);
    }

    @Override
    public Advertisement getAdvertisement() {
        return this.advertisement;
    }

    @Override
    public void reset() {
        this.advertisement = new Advertisement();
    }
}
