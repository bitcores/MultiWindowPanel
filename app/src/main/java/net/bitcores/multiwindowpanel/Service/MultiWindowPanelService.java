package net.bitcores.multiwindowpanel.Service;

import net.bitcores.multiwindowpanel.Config.MultiWindowPanelCommon;
import net.bitcores.multiwindowpanel.Provider.MultiWindowPanelProvider;
import net.bitcores.multiwindowpanel.R;

import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

    private List<ResolveInfo> mMultiWindowAppList = new ArrayList<ResolveInfo>();
    private List<String> launcherList = new ArrayList<String>();
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

        IntentFilter filter = new IntentFilter();
        filter.addAction("net.bitcores.multiwindowpanel.COCKTAIL_UPDATE");
        mContext.registerReceiver(updateReceiver, filter);
    }

    public void prepareItems() {
        launcherList = MultiWindowPanelCommon.launcherItems;
        //  make list of icons to click
        pm = mContext.getApplicationContext().getPackageManager();

        String[] appList = launcherList.toArray(new String[launcherList.size()]);
        for (int i = 0; i < appList.length; i++) {
            String[] names = appList[i].split(",");
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(names[0], names[1]));
            ResolveInfo app = pm.resolveActivity(intent, 0);
            mMultiWindowAppList.add(app);
        }
    }

    public void onDestroy() {
        mMultiWindowAppList.clear();
        mContext.unregisterReceiver(updateReceiver);
    }

    BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("net.bitcores.multiwindowpanel.COCKTAIL_UPDATE")) {
                mMultiWindowAppList.clear();
                prepareItems();

                ComponentName cocktail = new ComponentName(context, MultiWindowPanelProvider.class);
                SlookCocktailManager cocktailBarManager = SlookCocktailManager.getInstance(context);
                int[] cocktailIds = cocktailBarManager.getCocktailIds(cocktail);
                for (int i = 0; i < cocktailIds.length; i++) {
                    cocktailBarManager.notifyCocktailViewDataChanged(cocktailIds[i], R.id.multiListView);
                }
            }
        }
    };

    public int getCount() {
        return mMultiWindowAppList.size();
    }

    public RemoteViews getViewAt(int position) {
        if (position >= mMultiWindowAppList.size()) {
            return null;
        }
        ResolveInfo appInfo = mMultiWindowAppList.get(position);
        ComponentInfo selectAppInfo = appInfo.activityInfo != null ? appInfo.activityInfo : appInfo.serviceInfo;
        String packageName = selectAppInfo.packageName;
        String name = selectAppInfo.name;
        ComponentName componentName = new ComponentName(packageName, name);

        if (packageName == null) {
            return null;
        }

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.cocktail_image);
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
        //  maybe put something in here to show up while shit is loading
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

    }
}