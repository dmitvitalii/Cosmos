package me.dmitvitalii.cosmos;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.util.ArrayMap;

import java.util.Map;

import static me.dmitvitalii.cosmos.Actions.ACTIONS;
import static me.dmitvitalii.cosmos.Actions.APOD;
import static me.dmitvitalii.cosmos.Actions.EPIC;
import static me.dmitvitalii.cosmos.Actions.MARS;
import static me.dmitvitalii.cosmos.Actions.MORE;
import static me.dmitvitalii.cosmos.Actions.NEXT;

public class CosmoService extends IntentService {

    @SuppressWarnings("unused")
    private static final String TAG = CosmoService.class.getSimpleName();
    private static final String EMPTY = "";
    private ComponentName mWidgetComponentName;
    private AppWidgetManager mWidgetManager;
    private Map<String, RemoteViewsBuilder> mBuilders = new ArrayMap<>();
    private Map<String, Integer> mViews = new ArrayMap<>();

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
        mWidgetComponentName = new ComponentName(getPackageName(), EpdWidget.class.getName());
        mWidgetManager = AppWidgetManager.getInstance(this);
        mViews.put(EPIC, R.id.epd_full_top_start);
        mViews.put(APOD, R.id.epd_full_top_middle);
        mViews.put(MARS, R.id.epd_full_top_end);
        mViews.put(NEXT, R.id.epd_full_bottom_start);
        mViews.put(MORE, R.id.epd_full_bottom_end);
    }

    private void updateWidgets(AppWidgetManager manager, String[] actions, int... ids) {
        for (int id : ids) {
            for (String action : actions) {
                RemoteViewsBuilder builder = mBuilders.get(action);
                if (null != builder) {
                    manager.updateAppWidget(id, builder.build());
                }
            }
        }
    }

    @Override
    public void onHandleIntent(Intent intent) {
        String action = null == intent ? EMPTY : intent.getAction();
        if (null == action) action = EMPTY;
        switch (action) {
            case EPIC:
            case APOD:
            case MARS:
                selectProject(action);
                break;
            case NEXT:
                updateImage(action);
                break;
            case MORE:
                showDetails(intent);
                break;
        }
        updateWidgets(mWidgetManager, ACTIONS, mWidgetManager.getAppWidgetIds(mWidgetComponentName));
    }

    private void showDetails(Intent intent) {
        startActivity(new Intent(intent).setClass(this, DetailsActivity.class));
    }

    private void updateImage(String action) {
        RemoteViewsBuilder builder = RemoteViewsBuilder.with(this)
                .layout(R.layout.epd_layout_fullscreen)
                .action(action)
                .view(R.id.epd_full_background)
                /*.bitmap(mDownloader.download(action))*/;
        mBuilders.put(action, builder);
    }

    private void selectProject(String action) {
        PreferenceUtil.choose(this, action);
        RemoteViewsBuilder builder = RemoteViewsBuilder.with(this)
                .layout(R.layout.epd_layout_fullscreen)
                .action(action)
                .textColor(android.R.color.white)
                /*.bitmap(mDownloader.download(action))*/;
        mBuilders.put(action, builder);
    }
}
