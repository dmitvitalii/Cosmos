package me.dmitvitalii.cosmos.data;

import android.util.Log;

import com.google.gson.Gson;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImageDownloader implements Downloader<ResponseBody> {

    @SuppressWarnings("unused")
    private static final String TAG = ImageDownloader.class.getSimpleName();
    private static final int URL = 0;
    private NasaClient mClient;

    public ImageDownloader() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NasaClient.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();

        mClient = retrofit.create(NasaClient.class);
    }

    @Override
    public void download(Callback<ResponseBody> callback, String... params) {
        Call<ResponseBody> call = mClient.rawImage(params[URL], NasaClient.API_KEY);
        Log.d(TAG, "download: " + call.request().url().toString());
        call.enqueue(callback);
    }
}
