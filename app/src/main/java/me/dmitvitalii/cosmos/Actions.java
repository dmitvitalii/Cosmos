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
    String TAP  = "tap";

    String[] ACTIONS = new String[]{
            EPIC, APOD, MARS, NEXT, MORE, TAP
    };
}
