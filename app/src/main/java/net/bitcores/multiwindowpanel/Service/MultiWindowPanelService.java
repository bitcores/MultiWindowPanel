package net.bitcores.multiwindowpanel.Service;

import net.bitcores.multiwindowpanel.Config.MultiWindowPanelCommon;
import net.bitcores.multiwindowpanel.Provider.MultiWindowPanelProvider;
import net.bitcores.multiwindowpanel.R;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bitcores on 2015-06-01.
 */
public class MultiWindowPanelService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }

}

class ListRemoteViewsFactory implements MultiWindowPanelService.RemoteViewsFactory {
    private List<String> launcherList = null;
    private Context mContext;
    private PackageManager pm;
    private MultiWindowPanelCommon multiWindowCommon;

    public ListRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
    }

    public void onCreate() {
        multiWindowCommon = new MultiWindowPanelCommon();
        multiWindowCommon.initSettings(mContext);

        prepareItems();
    }

    public void prepareItems() {
        launcherList = new ArrayList<String>(MultiWindowPanelCommon.launcherItems);
        //  make list of icons to click
        pm = mContext.getPackageManager();
    }

    public void onDestroy() {

    }

    public int getCount() {
        return launcherList.size();
    }

    public RemoteViews getViewAt(int position) {
        if (position >= launcherList.size()) {
            return null;
        }

        String storeName = launcherList.get(position);

        String[] params = storeName.split(",");
        String packageName = params[0];
        String name = params[1];

        if (packageName == null) {
            return null;
        }

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.cocktail_image);

        ComponentName componentName = new ComponentName(packageName, name);
        Drawable appIcon = null;
        try {
            appIcon = pm.getActivityIcon(componentName);
        } catch (PackageManager.NameNotFoundException e) {
            // hurf
        }
        if (appIcon != null) {
            rv.setImageViewBitmap(R.id.cocktailImageView, ((BitmapDrawable) appIcon).getBitmap());
        }

        Bundle extras = new Bundle();
        extras.putInt(MultiWindowPanelProvider.EXTRA_ITEM, position);
        extras.putString(MultiWindowPanelProvider.PACKAGE_NAME, packageName);
        extras.putString(MultiWindowPanelProvider.CLASS_NAME, name);
        Intent fillInIntent = new Intent(Intent.ACTION_MAIN);
        fillInIntent.putExtras(extras);

        rv.setOnClickFillInIntent(R.id.cocktailImageView, fillInIntent);

        return rv;
    }

    public RemoteViews getLoadingView() {
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        prepareItems();
    }
}