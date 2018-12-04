package com.mv.Adapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Model.Report;
import com.mv.R;

import java.util.List;
import java.util.Map;

public class ReportsListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private CustomTabsClient mClient;
    private List<String> mListDataHeader;
    private Map<String, List<Report>> mListDataChild;

    private static final String EXTRA_CUSTOM_TABS_TOOLBAR_COLOR = "android.support.customtabs.extra.TOOLBAR_COLOR";
    private static final String PACKAGE_NAME = "com.android.chrome";

    public ReportsListAdapter(Context context, List<String> headerList,
                              Map<String, List<Report>> childDataList) {

        this.mContext = context;
        this.mListDataHeader = headerList;
        this.mListDataChild = childDataList;

        CustomTabsServiceConnection service = new CustomTabsServiceConnection() {

            @Override
            public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
                mClient = client;
                mClient.warmup(0);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mClient = null;
            }
        };

        CustomTabsClient.bindCustomTabsService(mContext, PACKAGE_NAME, service);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.mListDataChild.get(this.mListDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflaterLayout = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflaterLayout != null ? inflaterLayout.inflate(R.layout.each_report_item,
                    null) : null;
        }

        if (convertView != null) {
            Report report = (Report) getChild(groupPosition, childPosition);
            RelativeLayout layoutMain = convertView.findViewById(R.id.layoutMain);
            layoutMain.setOnClickListener(v -> startWebView(report.getTableauLinkC()));

            TextView txtReporterName = convertView.findViewById(R.id.txtReporterName);
            txtReporterName.setText(report.getReportNameC());

            TextView txtReportLink = convertView.findViewById(R.id.txtReportLink);
            txtReportLink.setText(report.getTableauLinkC());
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.mListDataChild.get(this.mListDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mListDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.mListDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater layoutInflater =
                    (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater != null ? layoutInflater.inflate(R.layout.list_group,
                    null) : null;
        }

        if (convertView != null) {
            ImageView imgGroup = convertView.findViewById(R.id.imgGroup);
            if (isExpanded) {
                imgGroup.setImageResource(R.drawable.downarrow);
            } else {
                imgGroup.setImageResource(R.drawable.rightarrow);
            }

            String headerTitle = (String) getGroup(groupPosition);
            TextView txtName = convertView.findViewById(R.id.txtName);
            txtName.setText(headerTitle);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
        }
    }

    private void startWebView(String url) {
        Uri uri =  Uri.parse(url);
        if (uri == null) {
            return;
        }

        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
        customTabsIntent.intent.setData(uri);
        customTabsIntent.intent.putExtra(EXTRA_CUSTOM_TABS_TOOLBAR_COLOR,
                mContext.getResources().getColor(R.color.colorPrimary));

        PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(
                customTabsIntent.intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : resolveInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            if (TextUtils.equals(packageName, PACKAGE_NAME)) {
                customTabsIntent.intent.setPackage(PACKAGE_NAME);
            }
        }

        customTabsIntent.launchUrl(mContext, uri);
    }
}