package me.dmitvitalii.cosmos;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Debug;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.yotadevices.sdk.EpdIntentCompat;

import java.util.List;
import java.util.Map;
import java.util.Random;

import me.dmitvitalii.cosmos.data.ApodDownloader;
import me.dmitvitalii.cosmos.data.ApodImageDownloader;
import me.dmitvitalii.cosmos.data.Downloader;
import me.dmitvitalii.cosmos.data.EpicDownloader;
import me.dmitvitalii.cosmos.data.EpicImageDownloader;
import me.dmitvitalii.cosmos.data.NasaClient;
import me.dmitvitalii.cosmos.data.entities.Apod;
import me.dmitvitalii.cosmos.data.entities.Earth;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;
import static me.dmitvitalii.cosmos.Actions.APOD;
import static me.dmitvitalii.cosmos.Actions.EPIC;
import static me.dmitvitalii.cosmos.Actions.MARS;
import static me.dmitvitalii.cosmos.Actions.MORE;
import static me.dmitvitalii.cosmos.Actions.NEXT;
import static me.dmitvitalii.cosmos.Actions.TAP;

public class CosmoService extends IntentService {

    @SuppressWarnings("unused")
    private static final String TAG = CosmoService.class.getSimpleName();
    private static final String EMPTY = "";
    private static final String EPIC_IMAGE = "jk.dfhasfrhjklsgjflgsfdjgjkls";
    private static final Map<String, Downloader> DOWNLOADERS = new ArrayMap<>();
    private static final Map<String, Callback> CALLBACKS = new ArrayMap<>();
    private static final String APOD_IMAGE = "asdfada";
    private Random mRandom = new Random();
    private ComponentName mWidgetComponentName;
    private AppWidgetManager mWidgetManager;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * Calls super(String) with the name of the worker thread, important only for debugging.
     */
    public CosmoService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        mWidgetComponentName = new ComponentName(getPackageName(), EpdWidget.class.getName());
        mWidgetManager = AppWidgetManager.getInstance(this);
        // TODO: 3/22/17 refactor.
        DOWNLOADERS.put(EPIC, new EpicDownloader());
        DOWNLOADERS.put(EPIC_IMAGE, new EpicImageDownloader());
        DOWNLOADERS.put(APOD, new ApodDownloader());
        DOWNLOADERS.put(APOD_IMAGE, new ApodImageDownloader());
//        DOWNLOADERS.put(MARS, );

        CALLBACKS.put(EPIC, new EpicCallback());
        CALLBACKS.put(EPIC_IMAGE, new EpicImageCallback());
        CALLBACKS.put(APOD, new ApodCallback());
        CALLBACKS.put(APOD_IMAGE, new ApodImageCallback());
    }

    private void updateWidgets(int[] ids, RemoteViewsBuilder... builders) {
        for (int id : ids) {
            for (RemoteViewsBuilder builder : builders) {
                mWidgetManager.updateAppWidget(id, builder.build());
            }
        }
    }

    private void updateWidgets(int[] ids, RemoteViews... views) {
        for (int id : ids) {
            for (RemoteViews v : views) {
                mWidgetManager.updateAppWidget(id, v);
            }
        }
    }

    @Override
    public void onHandleIntent(Intent intent) {
        String action = null == intent ? EMPTY : intent.getAction();
        if (null == action) action = EMPTY;
        Log.d(TAG, "onHandleIntent: " + action);
        int[] ids = mWidgetManager.getAppWidgetIds(mWidgetComponentName);
        String savedAction = action;
        String prefAction = PreferenceUtil.getChosen(this);
        switch (action) {
            case EPIC:
            case MARS:
            case APOD:
                selectProject(action);
                updateButtons(action, ids);
                savedAction = action;
            case NEXT:
                savedAction = isEmpty(savedAction) ? prefAction : savedAction;
                Log.d(TAG, "onHandleIntent: action " + savedAction + ", pref action " + prefAction);
                DOWNLOADERS.get(savedAction).download(CALLBACKS.get(savedAction));
                break;
            case MORE:
                showDetails(intent);
                break;
            case TAP:
                showControls(ids, toggleShow());
                break;
            default:
                Log.d(TAG, "onHandleIntent: action " + savedAction + ", pref action " + prefAction);
                updateAll(PreferenceUtil.isVisible(this), ids);
                savedAction = isEmpty(savedAction) ? PreferenceUtil.getChosen(this) : savedAction;
                DOWNLOADERS.get(savedAction).download(CALLBACKS.get(savedAction));
        }
    }

    private void showControls(int[] ids, boolean show) {
        // TODO: 3/22/17 ughh...
        RemoteViews widget = new RemoteViews(getPackageName(), R.layout.epd_layout_fullscreen);
        widget.setViewVisibility(R.id.epd_full_top_start, show ? View.VISIBLE : View.GONE);
        widget.setViewVisibility(R.id.epd_full_top_middle, show ? View.VISIBLE : View.GONE);
        widget.setViewVisibility(R.id.epd_full_top_end, show ? View.VISIBLE : View.GONE);
        widget.setViewVisibility(R.id.epd_full_bottom_start, show ? View.VISIBLE : View.GONE);
        widget.setViewVisibility(R.id.epd_full_bottom_end, show ? View.VISIBLE : View.GONE);
        updateWidgets(ids, widget);
    }

    private boolean toggleShow() {
        boolean result = !PreferenceUtil.isVisible(this);
        PreferenceUtil.setVisible(this, result);
        return result;
    }

    private void updateAll(boolean buttonsVisible, int[] ids) {
        int layout = R.layout.epd_layout_fullscreen;
        // TODO: 3/22/17 refactor, get rid of copypasted code, but don't overengineer.
        RemoteViewsBuilder left = RemoteViewsBuilder.with(this)
                .layout(layout)
                .visible(buttonsVisible)
                .action(EPIC)
                .textColor(PreferenceUtil.isChosen(this, EPIC) ? Color.GRAY : Color.WHITE)
                .view(R.id.epd_full_top_start);
        RemoteViewsBuilder middle = RemoteViewsBuilder.with(this)
                .layout(layout)
                .visible(buttonsVisible)
                .action(MARS)
                .textColor(PreferenceUtil.isChosen(this, APOD) ? Color.GRAY : Color.WHITE)
                .view(R.id.epd_full_top_middle);
        RemoteViewsBuilder right = RemoteViewsBuilder.with(this)
                .layout(layout)
                .action(APOD)
                .visible(buttonsVisible)
                .textColor(PreferenceUtil.isChosen(this, MARS) ? Color.GRAY : Color.WHITE)
                .view(R.id.epd_full_top_end);
        RemoteViewsBuilder bottomLeft = RemoteViewsBuilder.with(this)
                .layout(layout)
                .visible(buttonsVisible)
                .action(NEXT)
                .view(R.id.epd_full_bottom_start);
        RemoteViewsBuilder bottomRight = RemoteViewsBuilder.with(this)
                .layout(layout)
                .visible(buttonsVisible)
                .action(MORE)
                .view(R.id.epd_full_bottom_end);
        RemoteViewsBuilder image = RemoteViewsBuilder.with(this)
                .layout(layout)
                .action(TAP)
                .view(R.id.epd_full_background);
        updateWidgets(ids, left, middle, right, bottomLeft, bottomRight, image);
    }

    private void updateButtons(String action, int... ids) {
        RemoteViewsBuilder builder = RemoteViewsBuilder.with(this)
                .layout(R.layout.epd_layout_fullscreen)
                .action(action)
                .textColor(android.R.color.white)
                /*.bitmap(mDownloader.download(action))*/;
        for (int id : ids) {
            mWidgetManager.updateAppWidget(id, builder.build());
        }
    }

    private void showDetails(Intent intent) {
        intent = new Intent(intent).setClass(this, DetailsActivity.class);
        EpdIntentCompat.addEpdFlags(intent, EpdIntentCompat.FLAG_ACTIVITY_START_ON_EPD_SCREEN);
        startActivity(intent);
    }

    private void updateImage(Bitmap bitmap, int... ids) {
        RemoteViewsBuilder imageBuilder = RemoteViewsBuilder.with(this)
                .layout(R.layout.epd_layout_fullscreen)
                .action(TAP)
                .view(R.id.epd_full_background)
                .bitmap(bitmap);
        updateWidgets(ids, imageBuilder);
    }

    private void selectProject(String action) {
        PreferenceUtil.choose(this, action);
    }

    /*
     * Callbacks. Todo: too much callbacks!!! Refactor and remove them!
     */
    private class EpicCallback implements Callback<List<Earth>> {

        @Override
        public void onResponse(Call<List<Earth>> call, Response<List<Earth>> response) {
            List<Earth> result = response.body();
            int randomPicId = mRandom.nextInt(result.size());
            Log.d(TAG, "download: " + result.get(randomPicId).toString());
            Earth e = result.get(randomPicId);
            DOWNLOADERS.get(EPIC_IMAGE).download(
                    CALLBACKS.get(EPIC_IMAGE),
                    e.getYear(),
                    e.getMonth(),
                    e.getDay(),
                    NasaClient.JPG,
                    e.image
            );
        }

        @Override
        public void onFailure(Call<List<Earth>> call, Throwable t) {
            Log.e(TAG, "onFailure: cannot load!", t);
        }
    }

    private class EpicImageCallback implements Callback<ResponseBody> {

        private byte mAttempts = 5;

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            ResponseBody body = response.body();
            if (null != body) {
                Bitmap bitmap = BitmapFactory.decodeStream(body.byteStream());
                int[] ids = mWidgetManager.getAppWidgetIds(mWidgetComponentName);
                if (null != bitmap) {
                    updateImage(bitmap, ids);
                }
            } else {
                if (mAttempts > 0) {
                    call.enqueue(this);
                    --mAttempts;
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Log.e(TAG, "onFailure: cannot load!", t);
        }
    }

    private class ApodCallback implements Callback<Apod> {
        @Override
        public void onResponse(Call<Apod> call, Response<Apod> response) {
            Apod apod = response.body();
            if (null != apod) {
                DOWNLOADERS.get(APOD_IMAGE).download(CALLBACKS.get(APOD_IMAGE), apod.url);
            }
        }

        @Override
        public void onFailure(Call<Apod> call, Throwable t) {

        }
    }

    private class ApodImageCallback implements Callback<ResponseBody> {

        private byte mAttempts = 5;

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            ResponseBody body = response.body();
            if (null != body) {
                Bitmap bitmap = BitmapFactory.decodeStream(body.byteStream());
                int[] ids = mWidgetManager.getAppWidgetIds(mWidgetComponentName);
                if (null != bitmap) {
                    updateImage(bitmap, ids);
                }
            } else {
                if (mAttempts > 0) {
                    call.enqueue(this);
                    --mAttempts;
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Log.e(TAG, "onFailure: cannot load!", t);
        }
    }

}
