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
import android.view.WindowManager;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.entity.NoteComponentEntity;
import com.su.workbox.ui.base.BaseFragment;
import com.su.workbox.utils.ReflectUtil;
import com.su.workbox.utils.UiHelper;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;

import java.lang.reflect.Array;
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
        add("label", info.loadLabel(mActivity.getPackageManager()).toString());
        add("enabled", String.valueOf(info.enabled));
        add("exported", String.valueOf(info.exported));
        add("processName", info.processName);
        add("taskAffinity", info.taskAffinity);
        add("permission", info.permission);
        List<Field> launchModeFields = ReflectUtil.getFieldsWithPrefix(ActivityInfo.class, "LAUNCH_");
        add("launchMode", ReflectUtil.getSingleMatchedFlag(ActivityInfo.class, launchModeFields, info.launchMode).first);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<Field> documentLaunchModeFields = ReflectUtil.getFieldsWithPrefix(ActivityInfo.class, "DOCUMENT_LAUNCH_");
            add("documentLaunchMode", ReflectUtil.getSingleMatchedFlag(ActivityInfo.class, documentLaunchModeFields, info.documentLaunchMode).first);
        }
        add("parentActivityName", info.parentActivityName);
        if (info.theme > 0) {
            add("theme", UiHelper.getThemeName(info.theme));
        }
        List<Field> screenOrientationFields = ReflectUtil.getFieldsWithPrefix(ActivityInfo.class, "SCREEN_ORIENTATION_");
        add("screenOrientation", ReflectUtil.getSingleMatchedFlag(ActivityInfo.class, screenOrientationFields, info.screenOrientation).first);
        add("softInputMode", makeMultipleChoiceStringResult(WindowManager.LayoutParams.class, "SOFT_INPUT_", info.softInputMode));
        add("configChanges", makeMultipleChoiceStringResult(ActivityInfo.class, "CONFIG_", info.configChanges));
        // allowTaskReparenting/alwaysRetainTaskState/autoRemoveFromRecents
        // allowEmbedded/clearTaskOnLaunch/excludeFromRecents/finishOnTaskLaunch
        // hardwareAccelerated/immersive/multiprocess/supportsPictureInPicture
        // noHistory/relinquishTaskIdentity/stateNotNeeded
        add("flags",makeMultipleChoiceStringResult(ActivityInfo.class, "FLAG_", info.flags));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            add("directBootAware", info.directBootAware);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            List<Field> colorModeFields = ReflectUtil.getFieldsWithPrefix(ActivityInfo.class, "COLOR_MODE_");
            add("colorMode", ReflectUtil.getSingleMatchedFlag(ActivityInfo.class, colorModeFields, info.colorMode).first);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            add("maxRecents", info.maxRecents);
            List<Field> persistableModeFields = ReflectUtil.getFieldsWithPrefix(ActivityInfo.class, "PERSIST_");
            add("persistableMode", ReflectUtil.getSingleMatchedFlag(ActivityInfo.class, persistableModeFields, info.persistableMode).first);
        }
        if (info.uiOptions > 0) {
            List<Field> uiOptionsFields = ReflectUtil.getFieldsWithPrefix(ActivityInfo.class, "UIOPTION_");
            add("uiOptions", ReflectUtil.getSingleMatchedFlag(ActivityInfo.class, uiOptionsFields, info.uiOptions).first);
        }
//        android:lockTaskMode=["normal" | "never" | "if_whitelisted" | "always"] //hide
//        android:maxAspectRatio="float" //hide
//        android:resizeableActivity=["true" | "false"] //UnsupportedAppUsage
//        android:showForAllUsers=["true" | "false"] //UnsupportedAppUsage
    }

    private void makeServiceInfoList() {
        ServiceInfo info = (ServiceInfo) mComponentInfo;
//        add(new Pair<>("description", mDesc));
        add("label", info.loadLabel(mActivity.getPackageManager()).toString());
        add("enabled", String.valueOf(info.enabled));
        add("exported", String.valueOf(info.exported));
        add("processName", info.processName);
        add("permission", info.permission);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            add("directBootAware", info.directBootAware);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add("foregroundServiceType", makeMultipleChoiceStringResult(ServiceInfo.class, "FOREGROUND_SERVICE_TYPE_", info.getForegroundServiceType()));
        }
        add("isolatedProcess", makeMultipleChoiceStringResult(ServiceInfo.class, "FLAG_", info.flags));
    }

    private void makeProviderInfoList() {
        ProviderInfo info = (ProviderInfo) mComponentInfo;
//        add(new Pair<>("description", mDesc));
        add("label", info.loadLabel(mActivity.getPackageManager()).toString());
        add("enabled", info.enabled);
        add("exported", info.exported);
        add("processName", info.processName);
        add("authority", info.authority);
        add("initOrder", info.initOrder);
        add("multiprocess", info.multiprocess);
        add("readPermission", info.readPermission);
        add("writePermission", info.writePermission);
        add("pathPermissions", info.pathPermissions);
        add("uriPermissionPatterns", info.uriPermissionPatterns);
        add("grantUriPermissions", info.grantUriPermissions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add("forceUriPermissions", info.forceUriPermissions);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            add("directBootAware", info.directBootAware);
        }
        add("flags", makeMultipleChoiceStringResult(ProviderInfo.class, "FLAG_", info.flags));
    }

    private void makeReceiverInfoList() {
        ActivityInfo info = (ActivityInfo) mComponentInfo;
//        add(new Pair<>("description", mDesc));
        add("label", info.loadLabel(mActivity.getPackageManager()).toString());
        add("enabled", String.valueOf(info.enabled));
        add("exported", String.valueOf(info.exported));
        add("processName", info.processName);
        add("permission", info.permission);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            add("directBootAware", info.directBootAware);
        }
        // flags/taskAffinity/configChanges activity属性
    }

    private boolean add(String first, Object second) {
        if (second == null) {
            return false;
        }
        if (second instanceof String) {
            String secondString = (String) second;
            if (TextUtils.isEmpty(secondString)) {
                return false;
            }
            return mList.add(new Pair<>(first, (String) second));
        } else if (second.getClass().isArray()) {
            int length = Array.getLength(second);
            Object[] objects = new Object[length];
            for (int i = 0; i < length; i ++) {
                objects[i] = Array.get(second, i);
            }
            return mList.add(new Pair<>(first, Arrays.toString(objects)));
        } else {
            return mList.add(new Pair<>(first, second.toString()));
        }
    }

    private String makeMultipleChoiceStringResult(Class<?> searchIn, String prefix, int flags) {
        List<Field> flagsFields = ReflectUtil.getFieldsWithPrefix(searchIn, prefix);
        List<Pair<String, String>> resultList = ReflectUtil.getMatchedFlags(ServiceInfo.class, flagsFields, flags);
        StringBuilder flagDesc = new StringBuilder(64);
        for (Pair<String, String> flag : resultList) {
            flagDesc.append(flag.first + ", ");
        }
        if (!resultList.isEmpty()) {
            flagDesc.delete(flagDesc.length() - 2, flagDesc.length());
        }
        return flagDesc.toString();
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
