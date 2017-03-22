package me.dmitvitalii.cosmos.data;

import android.util.Log;

import com.google.gson.Gson;

import java.util.Random;

import me.dmitvitalii.cosmos.data.entities.Earth;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EpicImageDownloader implements Downloader<ResponseBody> {

    @SuppressWarnings("unused")
    private static final String TAG = EpicImageDownloader.class.getSimpleName();
    private static final int TYPE = 3;
    private static final int URL = 4;
    private Retrofit mRetrofit;
    private NasaClient mClient;

    public EpicImageDownloader() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(NasaClient.EPIC_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();

        mClient = mRetrofit.create(NasaClient.class);
    }

    @Override
    public void download(Callback<ResponseBody> callback, String... params) {
        Call<ResponseBody> superCall = mClient.epicArchive(
                params[Earth.YEAR],
                params[Earth.MONTH],
                params[Earth.DAY],
                params[TYPE],
                params[URL] + "." + params[TYPE]
        );
        Log.d(TAG, "download: superurl " + superCall.request().url().toString());
        superCall.enqueue(callback);
    }
}
