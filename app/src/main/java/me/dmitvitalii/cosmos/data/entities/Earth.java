package me.dmitvitalii.cosmos.data.entities;

/**
 * @author Vitalii Dmitriev
 * @since 22.03.2017
 */
public class Earth {
    public static final byte YEAR = 0;
    public static final byte MONTH = 1;
    public static final byte DAY = 2;
    private static final String DELIMITER = "-";
    public String image;
    public String date;
    public String caption;

    @Override
    public String toString() {
        return "Earth{" +
                "image='" + image + '\'' +
                ", date='" + date + '\'' +
                ", caption='" + caption + '\'' +
                '}';
    }

    public String getYear() {
        return date.split(DELIMITER)[YEAR];
    }

    public String getMonth() {
        return date.split(DELIMITER)[MONTH];
    }

    public String getDay() {
        return date.split(DELIMITER)[DAY].split(" ")[0];
    }
}
