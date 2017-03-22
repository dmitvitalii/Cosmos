package me.dmitvitalii.cosmos.data.entities;

/**
 * @author Vitalii Dmitriev
 */
public class Apod {
    public String date;
    public String title;
    public String explanation;
    public String url;
    public String hduri;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Apod apod = (Apod) o;

        if (!date.equals(apod.date)) return false;
        if (explanation != null ? !explanation.equals(apod.explanation) : apod.explanation != null)
            return false;
        if (!title.equals(apod.title)) return false;
        if (!hduri.equals(apod.hduri)) return false;
        return url.equals(apod.url);
    }

    @Override
    public int hashCode() {
        int result = date.hashCode();
        result = 31 * result + (explanation != null ? explanation.hashCode() : 0);
        result = 31 * result + title.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + hduri.hashCode();
        return result;
    }
}
