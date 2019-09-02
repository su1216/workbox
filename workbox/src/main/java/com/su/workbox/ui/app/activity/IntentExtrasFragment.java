package com.su.workbox.ui.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.utils.ReflectUtil;
import com.su.workbox.widget.SimpleTextWatcher;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.ContextMenuRecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by su on 17-12-25.
 */

public class IntentExtrasFragment extends IntentBaseInfoFragment {

    private static final String TAG = IntentExtrasFragment.class.getSimpleName();
    private static final int DEFAULT_ARRAY_SIZE = 2;
    private RecyclerView mRecyclerView;
    private ParameterViewAdapter mParameterAdapter;
    private Resources mResources;
    private int mSelected;

    public IntentExtrasFragment() {
        type = TYPE_EXTRAS;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResources = getResources();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.workbox_template_recycler_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = view.findViewById(R.id.recycler_view);
        initViews();
        registerForContextMenu(mRecyclerView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterForContextMenu(mRecyclerView);
    }

    private static String primitiveFormat(@Nullable String parameter) {
        if (TextUtils.isEmpty(parameter)) {
            return parameter;
        }
        return parameter.trim();
    }

    private static String jsonPrettyFormat(@Nullable String parameter) {
        if (TextUtils.isEmpty(parameter)) {
            return parameter;
        }
        try {
            JSONObject jsonObject = JSON.parseObject(parameter, JSONObject.class);
            return JSON.toJSONString(jsonObject, true);
        } catch (JSONException e) {
            try {
                JSONArray jsonArray = JSON.parseObject(parameter, JSONArray.class);
                return JSON.toJSONString(jsonArray, true);
            } catch (JSONException arrayException) {
                //初始化页面的时候已经有json检查,此处不用再次toast
                Log.w(TAG, "parameter: " + parameter, arrayException);
            }
        }
        return null;
    }

    boolean checkRequired() {
        List<IntentExtra> extras = mCloneExtras.getExtraList();
        for (IntentExtra extra : extras) {
            String parameterValue = extra.getValue();
            if (extra.isRequired() && TextUtils.isEmpty(parameterValue)) {
                new ToastBuilder(extra.getName() + " 是必填参数").show();
                return false;
            }
        }
        return true;
    }

    public void collectIntentData(Intent intent, IntentData intentData) {
        List<IntentExtra> extras = mCloneExtras.getExtraList();
        intentData.setExtraList(extras);
        for (IntentExtra extra : extras) {
            if (!TextUtils.isEmpty(extra.getName())) {
                try {
                    makeParameter(intent, extra);
                } catch (RuntimeException e) {
                    new ToastBuilder(e.getMessage()).show();
                } catch (ClassNotFoundException e) {
                    new ToastBuilder(e.getMessage()).show();
                }
            }
        }
    }

    @Override
    protected void initViews() {
        mParameterAdapter = new ParameterViewAdapter(this, mIntentData.getExtraList(), mCloneExtras.getExtraList());
        mRecyclerView.setAdapter(mParameterAdapter);
    }

    void showAddDialog() {
        showTypeDialog();
    }

    private void showTypeDialog() {
        new AlertDialog.Builder(mActivity)
                .setTitle("类型")
                .setSingleChoiceItems(R.array.workbox_intent_extra_types, 0, (dialog, which) -> mSelected = which)
                .setPositiveButton(R.string.workbox_confirm, (dialog, which) -> {
                    try {
                        String className = mResources.getStringArray(R.array.workbox_intent_extra_type_class_names)[mSelected];
                        Class<?> clazz;
                        IntentExtra extra = new IntentExtra();
                        int _index = className.indexOf("_");
                        if (_index >= 0) {
                            clazz = ReflectUtil.forName(className.substring(0, _index));
                            extra.setListClassName(className.substring(_index + 1));
                        } else if (className.startsWith("[")) {
                            clazz = ReflectUtil.forName(className.substring(2, className.length() - 1));
                            extra.setArrayClassName(className);
                        } else {
                            clazz = ReflectUtil.forName(className);
                        }
                        extra.setValueClassName(clazz.getName());
                        extra.setRequired(false);
                        showKeyDialog(extra);
                    } catch (ClassNotFoundException e) {
                        Log.w(TAG, e);
                    }
                })
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    private void showKeyDialog(IntentExtra extra) {
        final EditText inputView = new EditText(mActivity);
        new AlertDialog.Builder(mActivity)
                .setTitle("key")
                .setView(inputView)
                .setPositiveButton(R.string.workbox_confirm, (dialog, which) -> {
                    String key = inputView.getText().toString();
                    if (TextUtils.isEmpty(key)) {
                        new ToastBuilder("key不可以为空").show();
                        showKeyDialog(extra);
                        return;
                    }
                    extra.setName(key);
                    newInstance(extra);
                    List<IntentExtra> data = mParameterAdapter.getData();
                    data.add(0, extra);
                    mParameterAdapter.notifyItemInserted(0);
                })
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    private void makeParameter(@NonNull Intent intent, @NonNull IntentExtra extra) throws ClassNotFoundException {
        String parameterName = extra.getName();
        String parameterValue = extra.getValue();
        if (TextUtils.isEmpty(parameterValue)) {
            return;
        }

        final boolean isArray = !TextUtils.isEmpty(extra.getArrayClassName());
        final boolean isList = !TextUtils.isEmpty(extra.getListClassName());
        if (isArray) {
            Class<?> arrayClass = Class.forName("[L" + extra.getValueClassName() + ";");
            Class<?> componentType = Class.forName(extra.getValueClassName());
            if (Parcelable.class.isAssignableFrom(componentType)) {
                intent.putExtra(parameterName, (Parcelable[]) JSON.parseObject(parameterValue, arrayClass));
            } else if (Serializable.class.isAssignableFrom(componentType)) {
                intent.putExtra(parameterName, (Serializable[]) JSON.parseObject(parameterValue, arrayClass));
            }
        } else if (isList) {
            Class<?> elementClass = Class.forName(extra.getListClassName());
            if (elementClass == Integer.class) {
                intent.putIntegerArrayListExtra(parameterName, new ArrayList<>(JSON.parseArray(parameterValue, Integer.class)));
            } else if (elementClass == String.class) {
                intent.putStringArrayListExtra(parameterName, new ArrayList<>(JSON.parseArray(parameterValue, String.class)));
            }
        } else {
            Class<?> clazz = Class.forName(extra.getValueClassName());
            if (clazz.equals(int.class) || clazz.equals(Integer.class)) {
                intent.putExtra(parameterName, Integer.parseInt(parameterValue));
            } else if (clazz.equals(short.class) || clazz.equals(Short.class)) {
                intent.putExtra(parameterName, Short.parseShort(parameterValue));
            } else if (clazz.equals(long.class) || clazz.equals(Long.class)) {
                intent.putExtra(parameterName, Long.parseLong(parameterValue));
            } else if (clazz.equals(double.class) || clazz.equals(Double.class)) {
                intent.putExtra(parameterName, Double.parseDouble(parameterValue));
            } else if (clazz.equals(float.class) || clazz.equals(Float.class)) {
                intent.putExtra(parameterName, Float.parseFloat(parameterValue));
            } else if (clazz.equals(byte.class) || clazz.equals(Byte.class)) {
                intent.putExtra(parameterName, Byte.parseByte(parameterValue));
            } else if (clazz.equals(char.class) || clazz.equals(Character.class)) {
                intent.putExtra(parameterName, parameterValue.charAt(0));
            } else if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
                intent.putExtra(parameterName, Boolean.parseBoolean(parameterValue));
            } else if (clazz.equals(CharSequence.class)) {
                intent.putExtra(parameterName, (CharSequence) parameterValue);
            } else if (clazz.equals(String.class)) {
                intent.putExtra(parameterName, parameterValue);
            } else if (clazz.equals(int[].class) || clazz.equals(Integer[].class)) {
                intent.putExtra(parameterName, JSON.parseObject(parameterValue, int[].class));
            } else if (clazz.equals(short[].class) || clazz.equals(Short[].class)) {
                intent.putExtra(parameterName, JSON.parseObject(parameterValue, short[].class));
            } else if (clazz.equals(long[].class) || clazz.equals(Long[].class)) {
                intent.putExtra(parameterName, JSON.parseObject(parameterValue, long[].class));
            } else if (clazz.equals(double[].class) || clazz.equals(Double[].class)) {
                intent.putExtra(parameterName, JSON.parseObject(parameterValue, double[].class));
            } else if (clazz.equals(float[].class) || clazz.equals(Float[].class)) {
                intent.putExtra(parameterName, JSON.parseObject(parameterValue, float[].class));
            } else if (clazz.equals(byte[].class) || clazz.equals(Byte[].class)) {
                intent.putExtra(parameterName, JSON.parseObject(parameterValue, byte[].class));
            } else if (clazz.equals(char[].class) || clazz.equals(Character[].class)) {
                intent.putExtra(parameterName, JSON.parseObject(parameterValue, char[].class));
            } else if (clazz.equals(boolean[].class) || clazz.equals(Boolean[].class)) {
                intent.putExtra(parameterName, JSON.parseObject(parameterValue, boolean[].class));
            } else if (clazz.equals(CharSequence[].class)) {
                intent.putExtra(parameterName, JSON.parseObject(parameterValue, CharSequence[].class));
            } else if (clazz.equals(String[].class)) {
                intent.putExtra(parameterName, JSON.parseObject(parameterValue, String[].class));
            } else {
                if (Parcelable.class.isAssignableFrom(clazz)) {
                    intent.putExtra(parameterName, (Parcelable) JSON.parseObject(parameterValue, clazz));
                } else if (Serializable.class.isAssignableFrom(clazz)) {
                    intent.putExtra(parameterName, (Serializable) JSON.parseObject(parameterValue, clazz));
                } else {
                    intent.putExtra(parameterName, parameterValue);
                }
            }
        }
    }

    private static class ParameterViewAdapter extends BaseRecyclerAdapter<IntentExtra> {

        private IntentExtrasFragment mFragment;
        private int mNormalColor;
        private int mRequiredColor;
        private List<IntentExtra> mOrigin;

        private ParameterViewAdapter(@NonNull IntentExtrasFragment fragment, @NonNull List<IntentExtra> origin, @NonNull List<IntentExtra> data) {
            super(data);
            mFragment = fragment;
            Resources resources = fragment.getResources();
            mRequiredColor = resources.getColor(R.color.workbox_error_hint);
            mNormalColor = resources.getColor(R.color.workbox_second_text);
            mOrigin = origin;
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_debug_activity_paramter;
        }

        @Override
        protected void bindData(@NonNull final BaseViewHolder holder, final int position, int itemType) {
            IntentExtra extra = getData().get(position);
            Log.w(TAG, "extra: " + extra);
            TextView classNameView = holder.getView(R.id.class_name);
            TextView elementClassView = holder.getView(R.id.element_class);
            classNameView.setText(extra.getClass(extra.getValueClassName()).getName());
            String arrayClassName = extra.getArrayClassName();
            String listClassName = extra.getListClassName();
            String elementClass = TextUtils.isEmpty(arrayClassName) ? listClassName : arrayClassName;
            if (TextUtils.isEmpty(elementClass)) {
                elementClassView.setVisibility(View.GONE);
            } else {
                elementClassView.setText(elementClass);
                elementClassView.setVisibility(View.VISIBLE);
            }
            ((TextView) holder.getView(R.id.field_name)).setText(extra.getName());
            TextView fieldRequiredView = holder.getView(R.id.field_required);
            String text;
            int color;
            if (extra.isRequired()) {
                text = " (required)";
                color = mRequiredColor;
            } else {
                text = " (optional)";
                color = mNormalColor;
            }
            Spannable scoreSpannable = new SpannableString(text);
            scoreSpannable.setSpan(new ForegroundColorSpan(color),
                    2,
                    text.length() - 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            fieldRequiredView.setText(scoreSpannable);
            final EditText valueView = holder.getView(R.id.value);
            TextWatcher textWatcher = (TextWatcher) valueView.getTag();
            if (textWatcher != null) {
                valueView.removeTextChangedListener(textWatcher);
            }
            Log.d(TAG, "extra: " + extra.getValue() + " pos: " + position);
            String value = extra.getValue();
            valueView.setText(value);
            textWatcher = new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    extra.setValue(s.toString());
                }
            };
            valueView.addTextChangedListener(textWatcher);
            valueView.setTag(textWatcher);
            if (!TextUtils.isEmpty(value)) {
                valueView.setSelection(value.length());
            }
            View topLayout = holder.getView(R.id.top_layout);
            topLayout.setOnClickListener(v -> {
                mFragment.mRecyclerView.showContextMenuForChild(topLayout);
            });
        }
    }

    private String objectToString(Object object) {
        return JSON.toJSONString(object,
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullBooleanAsFalse,
                SerializerFeature.WriteNullNumberAsZero,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.PrettyFormat);
    }

    private void reset(@NonNull MenuItem item, int position) {
        IntentExtra extra = mParameterAdapter.getData().get(position);
        String name = extra.getName();
        IntentExtra origin = findActivityExtraByName(name);
        if (origin == null) {
            return;
        }
        extra.setValue(origin.getValue());
        mParameterAdapter.notifyItemChanged(position);
    }

    private void format(@NonNull MenuItem item, int position) {
        IntentExtra extra = mParameterAdapter.getData().get(position);
        String value = extra.getValue();
        if (TextUtils.isEmpty(value)) {
            return;
        }
        if (ReflectUtil.isPrimitiveClass(extra.getClass(extra.getValueClassName()))) {
            extra.setValue(primitiveFormat(value));
        } else {
            String formattedValue = jsonPrettyFormat(value);
            if (formattedValue == null) {
                new ToastBuilder(extra.getName() + "值不是json,或者json格式错误").show();
            } else {
                extra.setValue(formattedValue);
            }
        }
        mParameterAdapter.notifyItemChanged(position);
    }

    private void instance(@NonNull MenuItem item, int position) {
        IntentExtra extra = mParameterAdapter.getData().get(position);
        newInstance(extra);
        mParameterAdapter.notifyItemChanged(position);
    }

    private void newInstance(IntentExtra extra) {
        final Class<?> clazz = extra.getClass(extra.getValueClassName());
        final boolean isArray = !TextUtils.isEmpty(extra.getArrayClassName());
        final boolean isList = !TextUtils.isEmpty(extra.getListClassName());

        Object object = null;
        if (isArray) {
            try {
                Class<?> arrayClass = Class.forName(extra.getArrayClassName().substring(0, 2) + clazz.getName() + ";");
                Object[] objects = ReflectUtil.newArrayInstance(arrayClass, DEFAULT_ARRAY_SIZE);
                if (objects != null) {
                    for (int i = 0; i < DEFAULT_ARRAY_SIZE; i++) {
                        objects[i] = ReflectUtil.newInstance(clazz);
                        if (i == 0) {
                            AppHelper.copyToClipboard(mActivity, clazz.getCanonicalName(), objectToString(objects[i]));
                            new ToastBuilder("已将单个" + clazz.getSimpleName() + "实体复制到粘贴板中").show();
                        }
                    }
                }
                object = objects;
            } catch (ClassNotFoundException e) {
                Log.w(TAG, e);
            }
        } else if (isList) {
            ArrayList<Object> list = new ArrayList<>();
            try {
                Class<?> elementClass = Class.forName(extra.getListClassName());
                if (elementClass == Integer.class) {
                    list.add(0);
                    list.add(1);
                } else if (elementClass == String.class) {
                    list.add("first");
                    list.add("second");
                }
                object = list;
            } catch (ClassNotFoundException e) {
                Log.w(TAG, e);
            }
        } else {
            object = ReflectUtil.newInstance(clazz);
        }
        if (object != null) {
            String value = objectToString(object);
            extra.setValue(value);
        }
    }

    private void required(@NonNull MenuItem item, int position) {
        IntentExtra extra = mParameterAdapter.getData().get(position);
        extra.setRequired(!extra.isRequired());
        mParameterAdapter.notifyItemChanged(position);
    }

    private void delete(@NonNull MenuItem item, int position) {
        mParameterAdapter.getData().remove(position);
        mParameterAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ContextMenuRecyclerView.RecyclerViewContextMenuInfo info = (ContextMenuRecyclerView.RecyclerViewContextMenuInfo) menuInfo;
        if (info == null || info.originalView instanceof EditText) {
            return;
        }

        IntentExtra extra = mParameterAdapter.getData().get(info.position);
        menu.add(0, 0, 0, "reset");
        menu.add(0, 1, 1, "format");
        menu.add(0, 2, 2, "instance");
        if (extra.isRequired()) {
            menu.add(0, 3, 3, "optional");
        } else {
            menu.add(0, 3, 3, "required");
        }
        menu.add(0, 4, 4, "delete");

        final Class<?> clazz = extra.getClass(extra.getValueClassName());
        if (!ReflectUtil.hasDefaultConstructor(clazz) || ReflectUtil.isPrimitiveClass(clazz)) {
            menu.findItem(2).setVisible(false);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuRecyclerView.RecyclerViewContextMenuInfo info = (ContextMenuRecyclerView.RecyclerViewContextMenuInfo) item.getMenuInfo();
        int menuId = item.getItemId();
        int position = info.position;
        if (menuId == 0) {
            reset(item, position);
        } else if (menuId == 1) {
            format(item, position);
        } else if (menuId == 2) {
            instance(item, position);
        } else if (menuId == 3) {
            required(item, position);
        } else if (menuId == 4) {
            delete(item, position);
        } else {
            return super.onContextItemSelected(item);
        }
        return true;
    }

    public static IntentExtrasFragment newInstance(IntentData intentData) {
        Bundle args = new Bundle();
        args.putParcelable("intentData", intentData);
        IntentExtrasFragment fragment = new IntentExtrasFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
