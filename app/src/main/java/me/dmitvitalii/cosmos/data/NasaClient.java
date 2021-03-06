package me.dmitvitalii.cosmos.data;

import java.util.List;

import me.dmitvitalii.cosmos.data.entities.Apod;
import me.dmitvitalii.cosmos.data.entities.Earth;
import me.dmitvitalii.cosmos.data.entities.MarsData;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * The {@link NasaClient} is a client to download data from {@code https://data.nasa.gov/}.
 *
 * @author Vitalii Dmitriev
 */
public interface NasaClient {

    String NASA_URL_FULL = "https://api.nasa.gov/planetary/apod?api_key=";
    String OTHER_KEY = "DEMO_KEY";

    String NASA_API_PARAM = "api_key=";
    String CURIOSITY = "curiosity";
    String OPPORTUNITY = "opportunity";
    String API = "api";
    String CONTENT_TYPE_JSON = "Content-Type: application/json";
    String CONTENT_TYPE_IMAGE = "Content-Type: image/png; charset=binary";
    String VERSION = "v1";
    String MARS = "mars-photos";
    String ROVERS = "rovers";
    String PHOTOS = "photos";
    String API_BASE_URL = "https://api.nasa.gov/";
    String EPIC_BASE_URL = "https://epic.gsfc.nasa.gov/";
    String PLANET = "planetary";
    String APOD = "apod";
    String NAVCAM = "NAVCAM";
    String ARCHIVE = "archive";
    String NATURAL = "natural";
    String ENHANCED = "enhanced";
    String API_KEY = "api_key";
    String PNG = "png";
    String JPG = "jpg";
    /**
     * Can be {@link #NATURAL} or {@link #ENHANCED}. Natural options looks better on EPD.
     */
    String EARTH_PHOTO_TYPE = NATURAL;

    @Headers(CONTENT_TYPE_JSON)
    @GET("/" + MARS + "/" + API + "/" + VERSION + "/" + ROVERS + "/{rover}/" + PHOTOS)
    Call<MarsData> marsRover(
            @Path("rover") String rover,
            @Query("sol") int sol,
            @Query("camera") String camera,
            @Query(API_KEY) String apiKey
    );

    @Headers(CONTENT_TYPE_JSON)
    @GET("/" + PLANET + "/" + APOD)
    Call<Apod> apod(
            @Query(API_KEY) String apiKey
    );

    @GET
    Call<ResponseBody> rawImage(
            @Url String url,
            @Query(API_KEY) String apiKey
    );


    @Headers(CONTENT_TYPE_JSON)
    @GET("/" + PLANET + "/" + APOD)
    Call<Apod> apod(
            @Query("date") String date,
            @Query(API_KEY) String apiKey
    );

    @Headers(CONTENT_TYPE_JSON)
    @GET("/" + API + "/" + EARTH_PHOTO_TYPE + "/{date}/")
    Call<List<Earth>> epicApi(@Path("date") String date);

    @GET("/" + ARCHIVE + "/" + EARTH_PHOTO_TYPE + "/{year}/{month}/{day}/{imageType}/{imagePath}")
    Call<ResponseBody> epicArchive(
            @Path("year") String year,
            @Path("month") String month,
            @Path("day") String day,
            @Path("imageType") String imageType,
            @Path("imagePath") String imagePath
    );
}
