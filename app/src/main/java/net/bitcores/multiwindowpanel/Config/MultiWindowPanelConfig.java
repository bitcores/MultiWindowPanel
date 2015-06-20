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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bitcores on 2015-06-01.
 */
public class MultiWindowPanelConfig extends Activity {
    private MultiWindowPanelCommon multiWindowPanelCommon;
    private static ListView previewListView;
    private GridView itemGridView;
    private CheckBox strictCheckBox;

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
        List<String> packageList = new ArrayList<String>();
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
                    ComponentInfo selectAppInfo = r.activityInfo != null ? r.activityInfo : r.serviceInfo;
                    String packageName = selectAppInfo.packageName;
                    String name = selectAppInfo.name;

                    packageList.add(packageName+","+name);
                }
            }
        }

        GridViewAdapter adapter = new GridViewAdapter(this, packageList);
        itemGridView.setAdapter(adapter);
    }

    public static void makeListView(Context context) {
        ListViewAdapter adapter = new ListViewAdapter(context);

        previewListView.setAdapter(adapter);
    }

    public static void checkList(Context context, List<String> packageList) {
        String[] launcherList = mLauncherItems.toArray(new String[mLauncherItems.size()]);

        for (String aLauncherList : launcherList) {
            if (!packageList.contains(aLauncherList)) {
                mLauncherItems.remove(aLauncherList);
            }
        }

        makeListView(context);
    }
}

class GridViewAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> packageList = new ArrayList<String>();
    private LayoutInflater inflater;
    private PackageManager pm;

    public GridViewAdapter(Context context, List<String> list) {
        mContext = context;
        packageList = list;
        inflater = LayoutInflater.from(mContext);
        pm = mContext.getPackageManager();
    }

    public int getCount() {
        return packageList.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= packageList.size()) {
            return null;
        }

        View v;
        ViewHolder holder;
        final String storeName = packageList.get(position);

        if (convertView == null) {
            v = inflater.inflate(R.layout.griditem_layout, parent, false);
            holder = new ViewHolder();
            holder.iv = (ImageView)v.findViewById(R.id.gridImageView);
            holder.tv = (TextView)v.findViewById(R.id.gridTextView);
            holder.cb = (CheckBox)v.findViewById(R.id.gridCheckBox);
            v.setTag(holder);
        } else {
            v = convertView;
            holder = (ViewHolder)v.getTag();
        }

        String[] split = storeName.split(",");
        String packageName = split[0];
        String name = split[1];

        ComponentName componentName = new ComponentName(packageName, name);

        String label = "";
        try {
            label = pm.getActivityInfo(componentName, 0).loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        holder.tv.setText(label);

        Drawable appIcon = null;
        try {
            appIcon = pm.getActivityIcon(componentName);
        } catch (PackageManager.NameNotFoundException e) {
            // hurf
        }
        if (appIcon != null) {
            holder.iv.setImageBitmap(((BitmapDrawable) appIcon).getBitmap());
        }

        if (MultiWindowPanelConfig.mLauncherItems.contains(storeName)) {
            holder.cb.setChecked(true);
        } else {
            holder.cb.setChecked(false);
        }
        holder.cb.setTag(storeName);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox)v;

                if (cb.isChecked()) {
                    if(!MultiWindowPanelConfig.mLauncherItems.contains(storeName)) {
                        MultiWindowPanelConfig.mLauncherItems.add(storeName);
                    }
                } else {
                    MultiWindowPanelConfig.mLauncherItems.remove(storeName);
                }

                MultiWindowPanelConfig.makeListView(mContext);
            }
        };

        holder.cb.setOnClickListener(clickListener);

        if (position == (packageList.size() - 1)) {
            MultiWindowPanelConfig.checkList(mContext, packageList);
        }

        return v;
    }

    private class ViewHolder {
        public ImageView iv;
        public TextView tv;
        public CheckBox cb;
    }
}

class ListViewAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> appList = new ArrayList<String>();
    private LayoutInflater inflater;
    private PackageManager pm;

    public ListViewAdapter(Context context) {
        mContext = context;
        appList.addAll(MultiWindowPanelConfig.mLauncherItems);
        inflater = LayoutInflater.from(mContext);
        pm = mContext.getPackageManager();
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

        View v;
        ViewHolder holder;
        if (convertView == null) {
            v = inflater.inflate(R.layout.cocktail_image, parent, false);
            holder = new ViewHolder();
            holder.iv = (ImageView)v.findViewById(R.id.cocktailImageView);
            v.setTag(holder);
        } else {
            v = convertView;
            holder = (ViewHolder)v.getTag();
        }

        String storeName = appList.get(position);
        String[] params = storeName.split(",");

        ComponentName componentName = new ComponentName(params[0], params[1]);
        Drawable appIcon = null;
        try {
            appIcon = pm.getActivityIcon(componentName);
        } catch (PackageManager.NameNotFoundException e) {
            // hurf
        }
        if (appIcon != null) {
            holder.iv.setImageBitmap(((BitmapDrawable) appIcon).getBitmap());
        }

        return v;
    }

    private class ViewHolder {
        public ImageView iv;
    }
}