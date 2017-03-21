package me.dmitvitalii.cosmos;

/**
 * @author Vitalii Dmitriev
 * @since 21.03.2017
 */
public interface Actions {

    String KEY_ACTION = "action";
    String KEY_URL = "url";

    String EPIC = "epic";
    String APOD = "apod";
    String MARS = "mars";
    String NEXT = "next";
    String MORE = "more";

    int EPIC_ID = 0;
    int APOD_ID = 1;
    int MARS_ID = 2;
    int NEXT_ID = 3;
    int MORE_ID = 4;

    String[] ACTIONS = new String[]{
            EPIC, APOD, MARS, NEXT, MORE
    };
}
