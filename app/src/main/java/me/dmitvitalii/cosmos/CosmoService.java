package me.dmitvitalii.cosmos;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.PowerManager;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.yotadevices.sdk.Epd;
import com.yotadevices.sdk.EpdIntentCompat;

import java.util.List;
import java.util.Map;
import java.util.Random;

import me.dmitvitalii.cosmos.data.ApodDownloader;
import me.dmitvitalii.cosmos.data.Downloader;
import me.dmitvitalii.cosmos.data.EpicDownloader;
import me.dmitvitalii.cosmos.data.EpicImageDownloader;
import me.dmitvitalii.cosmos.data.ImageDownloader;
import me.dmitvitalii.cosmos.data.MarsDownloader;
import me.dmitvitalii.cosmos.data.NasaClient;
import me.dmitvitalii.cosmos.data.entities.Apod;
import me.dmitvitalii.cosmos.data.entities.Earth;
import me.dmitvitalii.cosmos.data.entities.MarsData;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private static final Map<String, Downloader> DOWNLOADERS = new ArrayMap<>();
    private static final Map<String, Callback> CALLBACKS = new ArrayMap<>();
    private static final String EPIC_IMAGE = "epici";
    private static final String IMAGE = "image";
    private static final int DELAY = 1000;
    private Random mRandom = new Random();
    private ComponentName mWidgetComponentName;
    private AppWidgetManager mWidgetManager;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
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
        DOWNLOADERS.put(IMAGE, new ImageDownloader());
        DOWNLOADERS.put(MARS, new MarsDownloader());

        CALLBACKS.put(EPIC, new EpicCallback());
        CALLBACKS.put(EPIC_IMAGE, new EpicImageCallback());
        CALLBACKS.put(APOD, new ApodCallback());
        CALLBACKS.put(IMAGE, new ImageCallback());
        CALLBACKS.put(MARS, new MarsCallback());
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
        acquireWakeLock();
        String action = null == intent ? EMPTY : intent.getAction();
        if (null == action) action = EMPTY;
        Log.d(TAG, "onHandleIntent: " + action);
        int[] ids = mWidgetManager.getAppWidgetIds(mWidgetComponentName);
        String prefAction;
        switch (action) {
            case EPIC:
            case MARS:
            case APOD:
                selectProject(action);
                updateButtons(action, ids);
            case NEXT:
                prefAction = PreferenceUtil.getChosen(this);
                setLoading(true, ids);
                loadImage(prefAction);
                break;
            case MORE:
                showDetails(intent);
                break;
            case TAP:
                showControls(ids, toggleShow());
                break;
            default:
                prefAction = PreferenceUtil.getChosen(this);
                updateAll(PreferenceUtil.isVisible(this), ids);
                setLoading(true, ids);
                loadImage(prefAction);
        }
        releaseWakeLock();
    }

    private void setLoading(boolean loading, int... ids) {
        RemoteViews widget = new RemoteViews(getPackageName(), R.layout.epd_layout_fullscreen);
        widget.setTextViewText(R.id.epd_full_bottom_start,
                getString(loading ? R.string.loading : R.string.next));
        updateWidgets(ids, widget);
    }

    private void loadImage(String prefAction) {
        DOWNLOADERS.get(prefAction).download(CALLBACKS.get(prefAction));
    }

    private void acquireWakeLock() {
        if (null == mPowerManager) {
            mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        }
        if (null == mWakeLock) {
            mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        }
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mWakeLock.isHeld()) mWakeLock.release();
            }
        }, DELAY);
    }

    private void showControls(int[] ids, boolean show) {
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
        // TODO: 3/23/17 make one builder for all views and one layout. Too many redundant builders.
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
                .textColor(PreferenceUtil.isChosen(this, MARS) ? Color.GRAY : Color.WHITE)
                .view(R.id.epd_full_top_middle);
        RemoteViewsBuilder right = RemoteViewsBuilder.with(this)
                .layout(layout)
                .action(APOD)
                .visible(buttonsVisible)
                .textColor(PreferenceUtil.isChosen(this, APOD) ? Color.GRAY : Color.WHITE)
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
        int layout = R.layout.epd_layout_fullscreen;
        RemoteViewsBuilder left = RemoteViewsBuilder.with(this)
                .layout(layout)
                .action(EPIC)
                .textColor(EPIC.equals(action) ? Color.GRAY : Color.WHITE)
                .view(R.id.epd_full_top_start);
        RemoteViewsBuilder middle = RemoteViewsBuilder.with(this)
                .layout(layout)
                .action(MARS)
                .textColor(MARS.equals(action) ? Color.GRAY : Color.WHITE)
                .view(R.id.epd_full_top_middle);
        RemoteViewsBuilder right = RemoteViewsBuilder.with(this)
                .layout(layout)
                .action(APOD)
                .textColor(APOD.equals(action) ? Color.GRAY : Color.WHITE)
                .view(R.id.epd_full_top_end);
        updateWidgets(ids, left, middle, right);
    }

    private void showDetails(Intent intent) {
        intent = new Intent(intent).setClass(this, DetailsActivity.class);
        EpdIntentCompat.addEpdFlags(intent, EpdIntentCompat.FLAG_ACTIVITY_START_ON_EPD_SCREEN);
        startActivity(intent);
    }

    private void updateImage(Bitmap bitmap, int... ids) {
        RemoteViews widget = new RemoteViews(getPackageName(), R.layout.epd_layout_fullscreen);
        Intent intent = new Intent(this, CosmoService.class).setAction(TAP);
        widget.setOnClickPendingIntent(R.id.epd_full_background,
                PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        widget.setImageViewBitmap(R.id.epd_full_background, bitmap);
        Epd.fullUpdate(widget, R.id.epd_full_background);
        updateWidgets(ids, widget);
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
            acquireWakeLock();
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
            releaseWakeLock();
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
            acquireWakeLock();
            ResponseBody body = response.body();
            if (null != body) {
                Bitmap bitmap = BitmapFactory.decodeStream(body.byteStream());
                int[] ids = mWidgetManager.getAppWidgetIds(mWidgetComponentName);
                if (null != bitmap) {
                    updateImage(bitmap, ids);
                    setLoading(false, ids);
                }
            } else {
                if (mAttempts > 0) {
                    call.enqueue(this);
                    --mAttempts;
                }
            }
            releaseWakeLock();
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Log.e(TAG, "onFailure: cannot load!", t);
        }
    }

    private class ApodCallback implements Callback<Apod> {
        @Override
        public void onResponse(Call<Apod> call, Response<Apod> response) {
            acquireWakeLock();
            Apod apod = response.body();
            if (null != apod) {
                DOWNLOADERS.get(IMAGE).download(CALLBACKS.get(IMAGE), apod.url);
            }
            releaseWakeLock();
        }

        @Override
        public void onFailure(Call<Apod> call, Throwable t) {

        }
    }

    private class ImageCallback implements Callback<ResponseBody> {

        private byte mAttempts = 5;

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            acquireWakeLock();
            ResponseBody body = response.body();
            if (null != body) {
                Bitmap bitmap = BitmapFactory.decodeStream(body.byteStream());
                int[] ids = mWidgetManager.getAppWidgetIds(mWidgetComponentName);
                if (null != bitmap) {
                    setLoading(false, ids);
                    updateImage(bitmap, ids);
                }
            } else {
                if (mAttempts > 0) {
                    call.enqueue(this);
                    --mAttempts;
                }
            }
            releaseWakeLock();
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Log.e(TAG, "onFailure: cannot load!", t);
        }
    }

    private class MarsCallback implements Callback<MarsData> {

        @Override
        public void onResponse(Call<MarsData> call, Response<MarsData> response) {
            acquireWakeLock();
            MarsData mars = response.body();
            if (null != mars) {
                DOWNLOADERS.get(IMAGE).download(
                        CALLBACKS.get(IMAGE),
                        mars.photos.get(mRandom.nextInt(mars.photos.size())).img_src // sorry.
                );
            }
            releaseWakeLock();
        }

        @Override
        public void onFailure(Call<MarsData> call, Throwable t) {

        }
    }
}