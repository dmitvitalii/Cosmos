package me.dmitvitalii.cosmos.data;

import android.util.Log;

import com.google.gson.Gson;

import me.dmitvitalii.cosmos.data.entities.Apod;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApodDownloader implements Downloader<Apod> {

    @SuppressWarnings("unused")
    private static final String TAG = ApodDownloader.class.getSimpleName();

    @Override
    public void download(Callback<Apod> callback, String... params) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NasaClient.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();

        NasaClient client = retrofit.create(NasaClient.class);
        Call<Apod> call = client.apod(NasaClient.OTHER_KEY);
        Log.d(TAG, "download: url " + call.request().url());
        call.enqueue(callback);
    }
}
