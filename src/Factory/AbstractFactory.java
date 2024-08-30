package Factory;

import Enums.Language;
import MediaContent.Advertisement.Advertisement;
import MediaContent.Movie.Movie;

public abstract class AbstractFactory {
    static FrenchFactory frenchFactory = new FrenchFactory();
    static EnglishFactory englishFactory = new EnglishFactory();

    public AbstractFactory(){}
    public static AbstractFactory factory(Language language){
        AbstractFactory factory = null;
        switch (language){
            case FR:
                factory = frenchFactory;
                break;
            case EN:
                factory = englishFactory;
                break;
        }

        return  factory;
    }

    public abstract Movie createMovie(String title, int duration, String genre, int ageRestriction, double price);
    public abstract Advertisement createAdvertisement(String title, int duration, int ageRestriction);
}
