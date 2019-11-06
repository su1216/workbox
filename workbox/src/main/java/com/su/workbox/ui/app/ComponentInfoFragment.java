package com.su.workbox.ui.app;

import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.entity.NoteComponentEntity;
import com.su.workbox.ui.base.BaseFragment;
import com.su.workbox.utils.ReflectUtil;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by su on 17-12-25.
 */

public class ComponentInfoFragment extends BaseFragment {

    private RecyclerView mRecyclerView;
    private String mType;
    private List<Pair<String, String>> mList = new ArrayList<>();
    private NoteComponentEntity mNoteComponent;
    private ComponentInfo mComponentInfo;
    private FragmentActivity mActivity;

    static ComponentInfoFragment newInstance(String type, NoteComponentEntity noteComponent, ComponentInfo componentInfo) {
        ComponentInfoFragment fragment = new ComponentInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        bundle.putParcelable("note", noteComponent);
        bundle.putParcelable("info", (Parcelable) componentInfo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        Bundle bundle = getArguments();
        mType = bundle.getString("type");
        mNoteComponent = bundle.getParcelable("note");
        mComponentInfo = bundle.getParcelable("info");
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.workbox_template_title_recycler_view, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        initData();
        View header = makeHeaderView();
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mList);
        adapter.setHeaderView(header);
        mRecyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setTitle(view.findViewById(R.id.id_toolbar));
    }

    private void setTitle(Toolbar toolbar) {
        switch (mType) {
            case "activity":
                toolbar.setTitle("Activity详情");
                break;
            case "service":
                toolbar.setTitle("Service详情");
                break;
            case "receiver":
                toolbar.setTitle("Receiver详情");
                break;
            case "provider":
                toolbar.setTitle("Provider详情");
                break;
            default:
                break;
        }
    }

    private View makeHeaderView() {
        LayoutInflater inflater = getLayoutInflater();
        View header = inflater.inflate(R.layout.workbox_header_activity_info, mRecyclerView, false);
        TextView descView = header.findViewById(R.id.desc);
        if (mNoteComponent == null || TextUtils.isEmpty(mNoteComponent.getDescription())) {
            descView.setVisibility(View.GONE);
        } else {
            descView.setVisibility(View.VISIBLE);
            descView.setText(mNoteComponent.getDescription());
        }
        ((TextView) header.findViewById(R.id.name)).setText(mComponentInfo.name);
        return header;
    }

    private void initData() {
        switch (mType) {
            case "activity":
                makeActivityInfoList();
                break;
            case "service":
                makeServiceInfoList();
                break;
            case "provider":
                makeProviderInfoList();
                break;
            case "receiver":
                makeReceiverInfoList();
                break;
            default:
                break;
        }
    }

    private void makeActivityInfoList() {
        ActivityInfo info = (ActivityInfo) mComponentInfo;
//        add(new Pair<>("description", mDesc));
//        add(new Pair<>("name", mActivityInfo.name));
        add(new Pair<>("label", info.loadLabel(mActivity.getPackageManager()).toString()));
        add(new Pair<>("enabled", String.valueOf(info.enabled)));
        add(new Pair<>("exported", String.valueOf(info.exported)));
        add(new Pair<>("processName", info.processName));
        add(new Pair<>("taskAffinity", info.taskAffinity));
        add(new Pair<>("permission", info.permission));
        List<Field> launchModeFields = ReflectUtil.getFieldsWithPrefix(ActivityInfo.class, "LAUNCH_");
        add(new Pair<>("launchMode", ReflectUtil.getSingleMatchedFlag(ActivityInfo.class, launchModeFields, info.launchMode).first));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<Field> documentLaunchModeFields = ReflectUtil.getFieldsWithPrefix(ActivityInfo.class, "DOCUMENT_LAUNCH_");
            add(new Pair<>("documentLaunchMode", ReflectUtil.getSingleMatchedFlag(ActivityInfo.class, documentLaunchModeFields, info.documentLaunchMode).first));
        }
        add(new Pair<>("parentActivityName", info.parentActivityName));
        List<Field> screenOrientationFields = ReflectUtil.getFieldsWithPrefix(ActivityInfo.class, "SCREEN_ORIENTATION_");
        add(new Pair<>("screenOrientation", ReflectUtil.getSingleMatchedFlag(ActivityInfo.class, screenOrientationFields, info.screenOrientation).first));
        //info.softInputMode
        List<Field> configChangesFields = ReflectUtil.getFieldsWithPrefix(ActivityInfo.class, "CONFIG_");
        List<Pair<String, String>> configChanges = ReflectUtil.getMatchedFlags(ActivityInfo.class, configChangesFields, info.configChanges);
        StringBuilder configChangeDesc = new StringBuilder();
        for (Pair<String, String> configChange : configChanges) {
            configChangeDesc.append(configChange.first + ", ");
        }
        if (!configChanges.isEmpty()) {
            configChangeDesc.delete(configChangeDesc.length() - 2, configChangeDesc.length());
        }
        add(new Pair<>("configChanges", configChangeDesc.toString()));

        List<Field> flagsFields = ReflectUtil.getFieldsWithPrefix(ActivityInfo.class, "FLAG_");
        List<Pair<String, String>> flags = ReflectUtil.getMatchedFlags(ActivityInfo.class, flagsFields, info.flags);
        StringBuilder flagDesc = new StringBuilder(64);
        for (Pair<String, String> flag : flags) {
            flagDesc.append(flag.first + ", ");
        }
        if (!flags.isEmpty()) {
            flagDesc.delete(flagDesc.length() - 2, flagDesc.length());
        }
        add(new Pair<>("flags", flagDesc.toString()));
    }

    private boolean add(Pair<String, String> pair) {
        if (!TextUtils.isEmpty(pair.second)) {
            return mList.add(pair);
        }
        return false;
    }

    private void makeServiceInfoList() {
        ServiceInfo info = (ServiceInfo) mComponentInfo;
//        add(new Pair<>("description", mDesc));
//        add(new Pair<>("name", mActivityInfo.name));
        add(new Pair<>("label", info.loadLabel(mActivity.getPackageManager()).toString()));
        add(new Pair<>("enabled", String.valueOf(info.enabled)));
        add(new Pair<>("exported", String.valueOf(info.exported)));
        add(new Pair<>("processName", info.processName));
        add(new Pair<>("permission", info.permission));
        List<Field> flagsFields = ReflectUtil.getFieldsWithPrefix(ServiceInfo.class, "FLAG_");
        List<Pair<String, String>> flags = ReflectUtil.getMatchedFlags(ServiceInfo.class, flagsFields, info.flags);
        StringBuilder flagDesc = new StringBuilder(64);
        for (Pair<String, String> flag : flags) {
            flagDesc.append(flag.first + ", ");
        }
        if (!flags.isEmpty()) {
            flagDesc.delete(flagDesc.length() - 2, flagDesc.length());
        }
        add(new Pair<>("flags", flagDesc.toString()));
    }

    private void makeProviderInfoList() {
        ProviderInfo info = (ProviderInfo) mComponentInfo;
//        add(new Pair<>("description", mDesc));
//        add(new Pair<>("name", mActivityInfo.name));
        add(new Pair<>("label", info.loadLabel(mActivity.getPackageManager()).toString()));
        add(new Pair<>("enabled", String.valueOf(info.enabled)));
        add(new Pair<>("exported", String.valueOf(info.exported)));
        add(new Pair<>("processName", info.processName));
        add(new Pair<>("authority", info.authority));
        add(new Pair<>("readPermission", info.readPermission));
        add(new Pair<>("writePermission", info.writePermission));
        add(new Pair<>("pathPermissions", Arrays.toString(info.pathPermissions)));
        add(new Pair<>("uriPermissionPatterns", Arrays.toString(info.uriPermissionPatterns)));
        add(new Pair<>("grantUriPermissions", String.valueOf(info.grantUriPermissions)));
        add(new Pair<>("initOrder", String.valueOf(info.initOrder)));

        List<Field> flagsFields = ReflectUtil.getFieldsWithPrefix(ProviderInfo.class, "FLAG_");
        List<Pair<String, String>> flags = ReflectUtil.getMatchedFlags(ProviderInfo.class, flagsFields, info.flags);
        StringBuilder flagDesc = new StringBuilder(64);
        for (Pair<String, String> flag : flags) {
            flagDesc.append(flag.first + ", ");
        }
        if (!flags.isEmpty()) {
            flagDesc.delete(flagDesc.length() - 2, flagDesc.length());
        }
        add(new Pair<>("flags", flagDesc.toString()));
    }

    private void makeReceiverInfoList() {
        ActivityInfo info = (ActivityInfo) mComponentInfo;
//        add(new Pair<>("description", mDesc));
//        add(new Pair<>("name", mActivityInfo.name));
        add(new Pair<>("label", info.loadLabel(mActivity.getPackageManager()).toString()));
        add(new Pair<>("enabled", String.valueOf(info.enabled)));
        add(new Pair<>("exported", String.valueOf(info.exported)));
        add(new Pair<>("processName", info.processName));
        add(new Pair<>("taskAffinity", info.taskAffinity));
        add(new Pair<>("permission", info.permission));
//        mActivityInfo.softInputMode
        List<Field> configChangesFields = ReflectUtil.getFieldsWithPrefix(ActivityInfo.class, "CONFIG_");
        List<Pair<String, String>> configChanges = ReflectUtil.getMatchedFlags(ActivityInfo.class, configChangesFields, info.configChanges);
        StringBuilder configChangeDesc = new StringBuilder();
        for (Pair<String, String> configChange : configChanges) {
            configChangeDesc.append(configChange.first + ", ");
        }
        if (!configChanges.isEmpty()) {
            configChangeDesc.delete(configChangeDesc.length() - 2, configChangeDesc.length());
        }
        add(new Pair<>("configChanges", configChangeDesc.toString()));

        List<Field> flagsFields = ReflectUtil.getFieldsWithPrefix(ActivityInfo.class, "FLAG_");
        List<Pair<String, String>> flags = ReflectUtil.getMatchedFlags(ActivityInfo.class, flagsFields, info.flags);
        StringBuilder flagDesc = new StringBuilder(64);
        for (Pair<String, String> flag : flags) {
            flagDesc.append(flag.first + ", ");
        }
        if (!flags.isEmpty()) {
            flagDesc.delete(flagDesc.length() - 2, flagDesc.length());
        }
        add(new Pair<>("flags", flagDesc.toString()));
    }

    private static class RecyclerViewAdapter extends BaseRecyclerAdapter<Pair<String, String>> {

        private RecyclerViewAdapter(List<Pair<String, String>> data) {
            super(data);
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_preference;
        }

        @Override
        protected void bindData(@NonNull final BaseViewHolder holder, final int position, int itemType) {
            Pair<String, String> pair = getData().get(position);
            ((TextView) holder.getView(R.id.name)).setText(pair.first);
            ((TextView) holder.getView(R.id.value)).setText(pair.second);
        }
    }
}
