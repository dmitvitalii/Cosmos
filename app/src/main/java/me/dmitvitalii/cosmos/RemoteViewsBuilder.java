package me.dmitvitalii.cosmos;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.RemoteViews;

import java.lang.ref.SoftReference;

/**
 * @author Vitalii Dmitriev
 * @since 21.03.2017
 */
public class RemoteViewsBuilder {

    private SoftReference<Context> mContextReference;
    private int mLayoutId;
    private PendingIntent mIntent;
    private int mViewId;
    private int mColor;
    private Bitmap mBitmap;
    private int mVisibility = View.VISIBLE;

    private RemoteViewsBuilder(Context context) {
        mContextReference = new SoftReference<>(context);
    }

    public static RemoteViewsBuilder with(Context context) {
        return new RemoteViewsBuilder(context);
    }

    public int getViewId() {
        return mViewId;
    }

    public RemoteViewsBuilder layout(int layoutId) {
        mLayoutId = layoutId;
        return this;
    }

    public RemoteViewsBuilder view(int viewId) {
        mViewId = viewId;
        return this;
    }

    public RemoteViewsBuilder visible(boolean visible) {
        mVisibility = visible ? View.VISIBLE : View.GONE;
        return this;
    }

    public RemoteViewsBuilder action(String action) {
        Context ctx = mContextReference.get();
        Intent intent = new Intent(ctx, CosmoService.class).setAction(action);
        mIntent = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
        Context ctx = mContextReference.get();
        RemoteViews widget = new RemoteViews(ctx.getPackageName(), mLayoutId);
        widget.setViewVisibility(mViewId, mVisibility);
        widget.setOnClickPendingIntent(mViewId, mIntent);
        if (null != mBitmap) {
            widget.setImageViewBitmap(mViewId, mBitmap);
        } else if (0 != mColor) {
            widget.setTextColor(mViewId, mColor);
        }
        return widget;
    }
}
