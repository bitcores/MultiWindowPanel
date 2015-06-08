package net.bitcores.multiwindowpanel.Provider;

import net.bitcores.multiwindowpanel.Config.MultiWindowPanelConfig;
import net.bitcores.multiwindowpanel.R;
import net.bitcores.multiwindowpanel.Service.MultiWindowPanelService;

import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager;
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailProvider;
import com.samsung.android.sdk.multiwindow.SMultiWindowActivity;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.RemoteViews;


/**
 * Created by bitcores on 2015-06-01.
 */
public class MultiWindowPanelProvider extends SlookCocktailProvider {
    public static final String LAUNCH_ACTION = "net.bitcores.multiwindowpanel.LAUNCH_ACTION";
    public static final String EXTRA_ITEM = "net.bitcores.multiwindowpanel.EXTRA_ITEM";
    public static final String PACKAGE_NAME = "net.bitcores.multiwindowpanel.PACKAGE_NAME";
    public static final String CLASS_NAME = "net.bitcores.multiwindowpanel.CLASS_NAME";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(LAUNCH_ACTION)) {
            PackageManager pm = context.getPackageManager();
            String packageName = intent.getStringExtra(PACKAGE_NAME);
            String className = intent.getStringExtra(CLASS_NAME);

            Intent mIntent = new Intent(Intent.ACTION_MAIN);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mIntent.setComponent(new ComponentName(packageName, className));

            SMultiWindowActivity.makeMultiWindowIntent(mIntent, 0.6f);

            if (mIntent.resolveActivity(pm) != null) {
                context.startActivity(mIntent);
            }
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, SlookCocktailManager cocktailBarManager, int[] cocktailIds) {
        RemoteViews layout = new RemoteViews(context.getPackageName(), R.layout.multipanel_layout);

        Intent configIntent = new Intent(context, MultiWindowPanelConfig.class);
        configIntent.setData(Uri.parse(configIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent configPending = PendingIntent.getActivity(context, 999922, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        layout.setOnClickPendingIntent(R.id.multiSettingLogo, configPending);

        Intent intent = new Intent(context, MultiWindowPanelService.class);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        layout.setRemoteAdapter(R.id.multiListView, intent);

        Intent launchIntent = new Intent(context, MultiWindowPanelProvider.class);
        launchIntent.setAction(MultiWindowPanelProvider.LAUNCH_ACTION);
        launchIntent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent launchPending = PendingIntent.getBroadcast(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        layout.setPendingIntentTemplate(R.id.multiListView, launchPending);

        layout.setEmptyView(R.id.multiListView, R.id.emptyView);

        for (int i = 0; i < cocktailIds.length; i++) {
            cocktailBarManager.updateCocktail(cocktailIds[i], layout);
        }

        super.onUpdate(context, cocktailBarManager, cocktailIds);
    }

}
