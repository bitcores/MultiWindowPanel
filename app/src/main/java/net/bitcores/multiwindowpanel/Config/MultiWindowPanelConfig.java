package net.bitcores.multiwindowpanel.Config;

import net.bitcores.multiwindowpanel.R;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.look.Slook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by bitcores on 2015-06-01.
 */
public class MultiWindowPanelConfig extends Activity {
    private MultiWindowPanelCommon multiWindowPanelCommon;
    private static ListView previewListView;
    private GridView itemGridView;
    private CheckBox strictCheckBox;
    private String[] params = new String[1];

    private static Boolean mStrict = false;
    private static Boolean mSkipWarning = false;
    public static List<String> mLauncherItems = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        // i shouldnt need this i think because this only launches from the manage panels area but i will check it anyway
        Slook slook = new Slook();
        try {
            slook.initialize(this);
        } catch (SsdkUnsupportedException e) {
            return;
        }

        if (slook.isFeatureEnabled(Slook.COCKTAIL_BAR)) {
            // If device supports cocktail bar, you can set up the sub-window.
            setContentView(R.layout.multipanelconfig_layout);

            multiWindowPanelCommon = new MultiWindowPanelCommon();
            multiWindowPanelCommon.initSettings(MultiWindowPanelConfig.this);
            mStrict = MultiWindowPanelCommon.strict;
            mSkipWarning = MultiWindowPanelCommon.skipWarning;
            mLauncherItems.clear();
            mLauncherItems.addAll(MultiWindowPanelCommon.launcherItems);

            previewListView = (ListView)this.findViewById(R.id.previewListView);
            itemGridView = (GridView)this.findViewById(R.id.itemGridView);
            strictCheckBox = (CheckBox)this.findViewById(R.id.strictCheckBox);
            strictCheckBox.setChecked(mStrict);
            strictCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final CheckBox cb = (CheckBox)v;
                    Context context = MultiWindowPanelConfig.this;
                    if (mStrict && !mSkipWarning) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                        View dialogView = LayoutInflater.from(context).inflate(R.layout.warning_layout, (ViewGroup)findViewById(R.id.warningLayout), false);
                        final CheckBox skipCheckBox = (CheckBox)dialogView.findViewById(R.id.skipCheckBox);
                        dialogBuilder.setView(dialogView);

                        dialogBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (skipCheckBox.isChecked()) {
                                    mSkipWarning = true;
                                }
                                mStrict = cb.isChecked();
                                makeGridView();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cb.setChecked(true);
                            }
                        });

                        AlertDialog leaveAlertDialog = dialogBuilder.create();

                        leaveAlertDialog.show();
                    } else {
                        mStrict = cb.isChecked();
                        makeGridView();
                    }
                }
            });
            Button cancelButton = (Button)this.findViewById(R.id.cancelButton);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MultiWindowPanelConfig.this.finish();
                }
            });
            Button saveButton = (Button)this.findViewById(R.id.saveButton);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MultiWindowPanelCommon.strict = mStrict;
                    MultiWindowPanelCommon.skipWarning = mSkipWarning;
                    MultiWindowPanelCommon.launcherItems.clear();
                    MultiWindowPanelCommon.launcherItems.addAll(mLauncherItems);

                    multiWindowPanelCommon.saveSettings(MultiWindowPanelConfig.this);

                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction("net.bitcores.multiwindowpanel.COCKTAIL_UPDATE");
                    sendBroadcast(broadcastIntent);

                    MultiWindowPanelConfig.this.finish();
                }
            });

            makeGridView();

            makeListView(MultiWindowPanelConfig.this);

        } else {
            // Normal device

        }

    }

    private void makeGridView() {
        List<ResolveInfo> mMultiWindowAppList = new ArrayList<ResolveInfo>();
        Set<String> packageList = new HashSet<String>();
        PackageManager pm = getApplicationContext().getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER | PackageManager.GET_META_DATA);

        for (ResolveInfo r : resolveInfos) {
            if (r.activityInfo != null) {
                boolean bUnSupportedMultiWindow = true;
                if (mStrict) {
                    if (r.activityInfo.applicationInfo.metaData != null) {
                        if (r.activityInfo.applicationInfo.metaData.getBoolean("com.sec.android.support.multiwindow")
                                || r.activityInfo.applicationInfo.metaData.getBoolean("com.samsung.android.sdk.multiwindow.enable")) {
                            bUnSupportedMultiWindow = false;
                            if (r.activityInfo.metaData != null) {
                                String activityWindowStyle = r.activityInfo.metaData.getString("com.sec.android.multiwindow.activity.STYLE");
                                if (activityWindowStyle != null) {
                                    ArrayList<String> activityWindowStyles = new ArrayList<String>(Arrays.asList(activityWindowStyle.split("\\|")));
                                    if (!activityWindowStyles.isEmpty()) {
                                        if (activityWindowStyles.contains("fullscreenOnly")) {
                                            bUnSupportedMultiWindow = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    bUnSupportedMultiWindow = false;
                }

                if (!bUnSupportedMultiWindow) {
                    mMultiWindowAppList.add(r);
                    ComponentInfo selectAppInfo = r.activityInfo != null ? r.activityInfo : r.serviceInfo;
                    String packageName = selectAppInfo.packageName;
                    String name = selectAppInfo.name;
                    packageList.add(packageName+","+name);
                }
            }
        }

        GridViewAdapter adapter = new GridViewAdapter(this, mMultiWindowAppList);
        itemGridView.setAdapter(adapter);
    }

    public static void makeListView(Context context) {
        ListViewAdapter adapter = new ListViewAdapter(context);

        previewListView.setAdapter(adapter);
    }

    public static void checkList(Context context, Set<String> packageList) {
        String[] launcherList = mLauncherItems.toArray(new String[mLauncherItems.size()]);

        for (int i = 0; i < launcherList.length; i++) {
            if (!packageList.contains(launcherList[i])){
                mLauncherItems.remove(launcherList[i]);
            }
        }

        makeListView(context);
    }
}

class GridViewAdapter extends BaseAdapter {
    Context mContext;
    private List<ResolveInfo> appList = new ArrayList<ResolveInfo>();
    private Set<String> packageList = new HashSet<String>();

    public GridViewAdapter(Context context, List<ResolveInfo> list) {
        mContext = context;
        appList = list;
    }

    public int getCount() {
        return appList.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= appList.size()) {
            return null;
        }

        View v = View.inflate(mContext, R.layout.griditem_layout, null);
        ImageView iv = (ImageView)v.findViewById(R.id.gridImageView);
        TextView tv = (TextView)v.findViewById(R.id.gridTextView);
        final CheckBox cb = (CheckBox)v.findViewById(R.id.gridCheckBox);

        PackageManager pm = mContext.getApplicationContext().getPackageManager();

        ResolveInfo appInfo = appList.get(position);
        ComponentInfo selectAppInfo = appInfo.activityInfo != null ? appInfo.activityInfo : appInfo.serviceInfo;
        String packageName = selectAppInfo.packageName;
        String name = selectAppInfo.name;
        ComponentName componentName = new ComponentName(packageName, name);
        final String storeName = packageName + "," + name;
        packageList.add(storeName);
        String label = ((CharSequence)selectAppInfo.loadLabel(pm)).toString();

        Drawable appIcon = null;
        try {
            appIcon = pm.getActivityIcon(componentName);
        } catch (PackageManager.NameNotFoundException e) {
            // hurf
        }
        if (appIcon != null) {
            iv.setImageBitmap(((BitmapDrawable) appIcon).getBitmap());
        }
        tv.setText(label);

        if (MultiWindowPanelConfig.mLauncherItems.contains(storeName)) {
            cb.setChecked(true);
        }
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb.setChecked(!cb.isChecked());
            }
        });
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if(!MultiWindowPanelConfig.mLauncherItems.contains(storeName)) {
                        MultiWindowPanelConfig.mLauncherItems.add(storeName);
                    }
                } else {
                    MultiWindowPanelConfig.mLauncherItems.remove(storeName);
                }

                MultiWindowPanelConfig.makeListView(mContext);
            }
        });

        if (position == (appList.size() -1)) {
            MultiWindowPanelConfig.checkList(mContext, packageList);
        }

        return v;
    }
}

class ListViewAdapter extends BaseAdapter {
    Context mContext;
    private String[] appList = null;

    public ListViewAdapter(Context context) {
        mContext = context;
        appList = MultiWindowPanelConfig.mLauncherItems.toArray(new String[MultiWindowPanelConfig.mLauncherItems.size()]);
}

    public int getCount() {
        return appList.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= appList.length) {
            return null;
        }

        View v = View.inflate(mContext, R.layout.cocktail_image, null);
        ImageView iv = (ImageView)v.findViewById(R.id.cocktailImageView);

        PackageManager pm = mContext.getApplicationContext().getPackageManager();

        String storeName = appList[position];
        String[] names = storeName.split(",");

        Drawable appIcon = null;
        try {
            appIcon = pm.getApplicationIcon(names[0]);
        } catch (PackageManager.NameNotFoundException e) {
            // hurf
        }
        if (appIcon != null) {
            iv.setImageBitmap(((BitmapDrawable) appIcon).getBitmap());
        }

        return v;
    }
}