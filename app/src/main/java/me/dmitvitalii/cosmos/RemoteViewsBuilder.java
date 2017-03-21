package me.dmitvitalii.cosmos;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

/**
 * @author Vitalii Dmitriev
 * @since 21.03.2017
 */
public class RemoteViewsBuilder {

    private Context mContext;
    private int mLayoutId;
    private PendingIntent mPendingIntent;
    private int mViewId;
    private int mColor;
    private Bitmap mBitmap;

    private RemoteViewsBuilder(Context context) {
        mContext = context;
    }

    private RemoteViewsBuilder() { /* NOP */ }

    public static RemoteViewsBuilder with(Context context) {
        return new RemoteViewsBuilder(context);
    }

    public RemoteViewsBuilder layout(int layoutId) {
        mLayoutId = layoutId;
        return this;
    }

    public RemoteViewsBuilder view(int viewId) {
        mViewId = viewId;
        return this;
    }

    public RemoteViewsBuilder action(String action) {
        Intent intent = new Intent(mContext, EpdWidget.class).setAction(action);
        mPendingIntent = PendingIntent.getBroadcast(
                mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        return this;
    }

    public RemoteViewsBuilder textColor(int color) {
        mColor = color;
        return this;
    }

    public RemoteViewsBuilder bitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        return this;
    }

    public RemoteViews build() {
        RemoteViews widget = new RemoteViews(mContext.getPackageName(), mLayoutId);
        widget.setOnClickPendingIntent(mViewId, mPendingIntent);
        if (null != mBitmap) {
            widget.setImageViewBitmap(mViewId, mBitmap);
        } else if (0 != mColor) {
            widget.setTextColor(mViewId, mColor);
        }
        return widget;
    }
}
