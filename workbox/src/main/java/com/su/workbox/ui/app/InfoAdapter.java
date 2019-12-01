package com.su.workbox.ui.app;

import android.content.Context;
import android.content.res.Resources;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.utils.UiHelper;

import java.util.List;

/**
 * Created by su on 19-12-1.
 */
public class InfoAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private Resources mResources;
    private LayoutInflater mInflater;
    private List<List<Pair<String, CharSequence>>> mData;

    public InfoAdapter(Context context, List<List<Pair<String, CharSequence>>> data) {
        this.mContext = context;
        mResources = mContext.getResources();
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = data;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        convertView = new FrameLayout(mContext);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, UiHelper.dp2px(1));
        convertView.setLayoutParams(lp);
        convertView.setBackgroundColor(mResources.getColor(R.color.workbox_disabled_text));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Pair<String, CharSequence> pair = mData.get(groupPosition).get(childPosition);
        ItemViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.workbox_item_app_info, parent, false);
            viewHolder = new ItemViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ItemViewHolder) convertView.getTag();
        }
        viewHolder.keyView.setText(pair.first);
        viewHolder.valueView.setText(pair.second);
        return convertView;
    }

    @Override
    public int getGroupCount() {
        return mData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mData.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mData.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mData.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private static class ItemViewHolder {
        private TextView keyView;
        private TextView valueView;

        ItemViewHolder(View view) {
            keyView = view.findViewById(R.id.key);
            valueView = view.findViewById(R.id.value);
        }
    }
}
