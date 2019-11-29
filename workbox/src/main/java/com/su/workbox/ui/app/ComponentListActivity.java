package com.su.workbox.ui.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.su.workbox.R;
import com.su.workbox.entity.NoteComponentEntity;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.ui.app.activity.IntentInfoActivity;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.utils.SearchableHelper;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.RecyclerItemClickListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by su on 17-5-31.
 * 调试功能列表 - 组件信息 - 四大组件列表
 */
public class ComponentListActivity extends BaseAppCompatActivity implements RecyclerItemClickListener.OnItemClickListener, SearchView.OnQueryTextListener {
    private static final String TAG = ComponentListActivity.class.getSimpleName();
    public static final String TYPE_LAUNCHER = "launcher";
    public static final String TYPE_ACTIVITY = "activity";
    public static final String TYPE_SERVICE = "service";
    public static final String TYPE_RECEIVER = "receiver";
    public static final String TYPE_PROVIDER = "provider";
    private String mType;
    private String mTitle;
    private String mPackageName;
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter<ComponentInfo> mAdapter;
    private List<ComponentInfo> mAllList = new ArrayList<>();
    private List<ComponentInfo> mNotedList = new ArrayList<>();
    private List<ComponentInfo> mUnnotedList = new ArrayList<>();
    private List<NoteComponentEntity> mNoteComponents = new ArrayList<>();
    private Map<String, NoteComponentEntity> mNoteComponentMap = new HashMap<>();

    private List<ComponentInfo> mFilterInfoList = new ArrayList<>();
    private List<Map<Integer, Integer>> mNameFilterColorIndexList = new ArrayList<>();
    private List<Map<Integer, Integer>> mDescFilterColorIndexList = new ArrayList<>();
    private SearchableHelper mSearchableHelper = new SearchableHelper();
    private int mDangerousColor;
    private int mNormalColor;

    public static Intent getLaunchIntent(@NonNull Context context, @NonNull String type) {
        Intent intent = new Intent(context, ComponentListActivity.class);
        intent.putExtra("type", type);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        Intent intent = getIntent();
        mType = intent.getStringExtra("type");
        PackageManager pm = this.getPackageManager();
        mPackageName = getPackageName();
        Resources resources = getResources();
        mDangerousColor = resources.getColor(R.color.workbox_error_hint);
        mNormalColor = resources.getColor(R.color.workbox_second_green);
        mRecyclerView = findViewById(R.id.recycler_view);
        try {
            PackageInfo info = pm.getPackageInfo(mPackageName, PackageManager.GET_ACTIVITIES
                    | PackageManager.GET_RECEIVERS
                    | PackageManager.GET_SERVICES
                    | PackageManager.GET_PROVIDERS
                    | PackageManager.GET_INTENT_FILTERS
                    | PackageManager.GET_META_DATA
                    | PackageManager.GET_DISABLED_COMPONENTS);
            initTitle();
            makeData();
            initList(info);
            rearrangeData(mNotedList);
            rearrangeData(mUnnotedList);
            View header = makeHeaderView(mAllList);
            mAdapter = new RecyclerViewAdapter<>(mFilterInfoList);
            mAdapter.setHeaderView(header);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, this));
            filter("");
        } catch (PackageManager.NameNotFoundException e) {
            new ToastBuilder("包名错误： " + e.getMessage()).setDuration(Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void makeData() {
        BufferedReader reader = null;
        String str = null;
        StringBuilder buf = new StringBuilder();
        AssetManager manager = getAssets();
        try {
            reader = new BufferedReader(new InputStreamReader(manager.open("generated/components.json"), StandardCharsets.UTF_8));
            while ((str = reader.readLine()) != null) {
                buf.append(str);
            }
            str = buf.toString();
        } catch (IOException e) {
            Log.w(TAG, e);
        } finally {
            IOUtil.close(reader);
        }

        if (!TextUtils.isEmpty(str)) {
            List<NoteComponentEntity> list = JSON.parseArray(str, NoteComponentEntity.class);
            for (NoteComponentEntity noteComponent : list) {
                if (TextUtils.equals(mType, noteComponent.getType())) {
                    mNoteComponentMap.put(noteComponent.getClassName(), noteComponent);
                    mNoteComponents.add(noteComponent);
                }
            }
        }
    }

    private void initList(PackageInfo info) {
        List<? extends ComponentInfo> list;
        switch (mType) {
            case TYPE_LAUNCHER:
            case TYPE_ACTIVITY:
                list = info.activities == null ? new ArrayList<>() : Arrays.asList(info.activities);
                break;
            case TYPE_RECEIVER:
                list = info.receivers == null ? new ArrayList<>() : Arrays.asList(info.receivers);
                break;
            case TYPE_SERVICE:
                list = info.services == null ? new ArrayList<>() : Arrays.asList(info.services);
                break;
            case TYPE_PROVIDER:
                list = info.providers == null ? new ArrayList<>() : Arrays.asList(info.providers);
                break;
            default:
                list = new ArrayList<>();
                break;
        }

        for (ComponentInfo componentInfo : list) {
            String className = componentInfo.name;
            if (className.startsWith(GeneralInfoHelper.LIB_PACKAGE_NAME)) {
                continue;
            }
            boolean find = false;
            for (NoteComponentEntity component : mNoteComponents) {
                if (TextUtils.equals(className, component.getClassName())) {
                    find = true;
                    break;
                }
            }

            if (find) {
                mNotedList.add(componentInfo);
            } else {
                mUnnotedList.add(componentInfo);
            }
        }

        mAllList.addAll(mNotedList);
        mAllList.addAll(mUnnotedList);
    }

    private void initTitle() {
        switch (mType) {
            case TYPE_LAUNCHER:
            case TYPE_ACTIVITY:
                mTitle = "Activity列表";
                break;
            case TYPE_RECEIVER:
                mTitle = "BroadcastReceiver列表";
                break;
            case TYPE_SERVICE:
                mTitle = "Service列表";
                break;
            case TYPE_PROVIDER:
                mTitle = "Provider列表";
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(mTitle);
        mSearchableHelper.initSearchToolbar(mToolbar, this);
    }

    private void rearrangeData(@NonNull List<? extends ComponentInfo> list) {
        Collections.sort(list, (o1, o2) -> {
            if (o1.exported && o2.exported) {
                NoteComponentEntity o1Note = mNoteComponentMap.get(o1.name);
                NoteComponentEntity o2Note = mNoteComponentMap.get(o2.name);
                if (isDebug(o1Note) && !isDebug(o2Note)) {
                    return 1;
                } else if (!isDebug(o1Note) && isDebug(o2Note)) {
                    return -1;
                } else {
                    return o1.name.compareToIgnoreCase(o2.name);
                }
            } else {
                if (o1.exported) {
                    return -1;
                } else if (o2.exported) {
                    return 1;
                } else {
                    return o1.name.compareToIgnoreCase(o2.name);
                }
            }
        });
    }

    private static boolean isDebug(NoteComponentEntity noteComponent) {
        return noteComponent != null && "debug".equals(noteComponent.getBuildType());
    }

    private View makeHeaderView(@NonNull List<? extends ComponentInfo> list) {
        LayoutInflater inflater = getLayoutInflater();
        View header = inflater.inflate(R.layout.workbox_header_component_info, mRecyclerView, false);
        int total = list.size();
        int exportedSize = 0;
        int disabledSize = 0;
        for (ComponentInfo info : list) {
            if (info.exported) {
                exportedSize++;
            }
            if (!info.enabled) {
                disabledSize++;
            }
        }
        ((TextView) header.findViewById(R.id.total)).setText("total: " + total);
        ((TextView) header.findViewById(R.id.exported)).setText("exported: " + exportedSize);
        ((TextView) header.findViewById(R.id.disabled)).setText("disabled: " + disabledSize);
        return header;
    }

    @Override
    public void onItemClick(View view, int position) {
        if (position == 0) {
            return;
        }
        final ComponentInfo info = mAdapter.getData().get(position - 1);
        if (TextUtils.equals(mType, TYPE_LAUNCHER)) {
            Intent intent = new Intent(this, IntentInfoActivity.class);
            intent.putExtra("info", (Parcelable) info); //ComponentInfo子类都实现了Parcelable接口
            startActivity(intent);
            return;
        }
        Intent intent = new Intent(this, ComponentActivity.class);
        intent.putExtra("info", (Parcelable) info); //ComponentInfo子类都实现了Parcelable接口
        intent.putExtra("note", mNoteComponentMap.get(info.name));
        intent.putExtra("type", mType);
        startActivity(intent);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filter(newText);
        return false;
    }

    private void filter(String str) {
        mFilterInfoList.clear();
        mNameFilterColorIndexList.clear();
        mDescFilterColorIndexList.clear();
        if (TextUtils.isEmpty(str)) {
            mFilterInfoList.addAll(mAllList);
            mAdapter.notifyDataSetChanged();
            return;
        }
        for (ComponentInfo search : mAllList) {
            String name = search.name;
            String shortName = name;
            if (name.startsWith(mPackageName)) {
                shortName = name.substring(mPackageName.length() + 1);
            }

            boolean nameFind = false;
            boolean descFind = false;
            if (mSearchableHelper.find(str, shortName, mNameFilterColorIndexList)) {
                nameFind = true;
            }
            NoteComponentEntity noteComponent = mNoteComponentMap.get(name);
            if (noteComponent != null) {
                String description = noteComponent.getDescription();
                if (mSearchableHelper.find(str, description, mDescFilterColorIndexList)) {
                    descFind = true;
                }
            }

            if (nameFind && !descFind) {
                mDescFilterColorIndexList.add(new HashMap<>());
            } else if (!nameFind && descFind) {
                mNameFilterColorIndexList.add(new HashMap<>());
            }
            if (nameFind || descFind) {
                mFilterInfoList.add(search);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private class RecyclerViewAdapter<T extends ComponentInfo> extends BaseRecyclerAdapter<T> {

        private RecyclerViewAdapter(List<T> data) {
            super(data);
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_component_info;
        }

        @Override
        protected void bindData(@NonNull final BaseViewHolder holder, final int position, int itemType) {
            final ComponentInfo info = getData().get(position);
            String displayName = info.name;
            NoteComponentEntity noteComponent = mNoteComponentMap.get(displayName);
            boolean fullName = true;
            if (displayName.startsWith(mPackageName)) {
                displayName = displayName.substring(mPackageName.length() + 1);
                fullName = false;
            }

            TextView descView = holder.getView(R.id.desc);
            if (noteComponent == null || TextUtils.isEmpty(noteComponent.getDescription())) {
                descView.setVisibility(View.GONE);
            } else {
                descView.setVisibility(View.VISIBLE);
                descView.setText(noteComponent.getDescription());
            }
            TextView nameView = holder.getView(R.id.name);
            if (fullName) {
                nameView.setText(displayName);
            } else {
                nameView.setText("." + displayName);
            }
            if (!info.exported && info.enabled) {
                holder.getView(R.id.extra_layout).setVisibility(View.GONE);
            } else {
                holder.getView(R.id.extra_layout).setVisibility(View.VISIBLE);
                TextView exportedView = holder.getView(R.id.exported);
                if (info.exported) {
                    exportedView.setVisibility(View.VISIBLE);
                    if (isDebug(noteComponent)) {
                        exportedView.setTextColor(mNormalColor);
                    } else {
                        exportedView.setTextColor(mDangerousColor);
                    }
                } else {
                    exportedView.setVisibility(View.GONE);
                }
                holder.getView(R.id.disabled).setVisibility(info.enabled ? View.GONE : View.VISIBLE);
            }
            mSearchableHelper.refreshFilterColor(holder.getView(R.id.name), position, mNameFilterColorIndexList);
            mSearchableHelper.refreshFilterColor(descView, position, mDescFilterColorIndexList);
        }
    }

    @Override
    public int menuRes() {
        return R.menu.workbox_search_menu;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
