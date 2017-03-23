package me.dmitvitalii.cosmos.data;

import com.google.gson.Gson;

import java.util.Random;

import me.dmitvitalii.cosmos.data.entities.MarsData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Vitalii Dmitriev
 * @since 17.03.2017
 */
public class MarsDownloader implements Downloader<MarsData> {
    public static final int SOL = 0;
    public static final int CAMERA = 1;
    public static final int ROVER = 2;
    private static final String[] CAMERAS = new String[]{
            "navcam", "fhaz", "rhaz", "mahli"
    };
    private static final int FACTOR = 1000;
    private static final int MAX_SOL = 1635 - FACTOR;
    private Random mRandom = new Random();

    @Override
    public void download(Callback<MarsData> callback, String... params) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NasaClient.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();
        NasaClient client = retrofit.create(NasaClient.class);
        Call<MarsData> call = client.marsRover(
                NasaClient.CURIOSITY,
                FACTOR + mRandom.nextInt(MAX_SOL),
                // Random sol from 1000 to 1635.
                // TODO: remove hardcoded value and use 'max_sol' from json
                CAMERAS[mRandom.nextInt(CAMERAS.length)],
                NasaClient.OTHER_KEY
        );
        call.enqueue(callback);
    }
}
