package me.dmitvitalii.cosmos.data;

import android.text.format.DateFormat;
import android.util.Log;

import com.google.gson.Gson;

import java.util.Date;
import java.util.List;

import me.dmitvitalii.cosmos.data.entities.Earth;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EpicDownloader implements Downloader<List<Earth>> {

    @SuppressWarnings("unused")
    private static final String TAG = EpicDownloader.class.getSimpleName();
    private NasaClient mClient;

    public EpicDownloader() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NasaClient.EPIC_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();
        mClient = retrofit.create(NasaClient.class);
    }


    @Override
    public void download(Callback<List<Earth>> callback, String... params) {
        Call<List<Earth>> call =
                mClient.epicApi(DateFormat.format("yyyy-MM-dd", new Date()).toString());
        Log.d(TAG, "download: " + call.request().url().toString());
        call.enqueue(callback);

    }
}
