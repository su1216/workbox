package com.su.workbox.ui.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.entity.NoteComponentEntity;
import com.su.workbox.entity.Parameter;
import com.su.workbox.utils.ReflectUtil;
import com.su.workbox.widget.SimpleTextWatcher;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by su on 17-12-25.
 */

public class ComponentExtrasFragment extends Fragment {

    private static final String TAG = ComponentExtrasFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ParameterViewAdapter mParameterAdapter;
    private FragmentActivity mActivity;
    private NoteComponentEntity mNoteComponent;
    private NoteComponentEntity mFinalComponent;
    private static final int DEFAULT_ARRAY_SIZE = 2;

    static ComponentExtrasFragment newInstance(NoteComponentEntity noteComponent) {
        ComponentExtrasFragment fragment = new ComponentExtrasFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("note", noteComponent);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        Bundle bundle = getArguments();
        mNoteComponent = bundle.getParcelable("note");

        if (mNoteComponent == null) {
            mNoteComponent = new NoteComponentEntity();
        }
        mFinalComponent = new NoteComponentEntity(mNoteComponent);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.workbox_template_recycler_view, container, false);
        mParameterAdapter = new ParameterViewAdapter(mActivity, mNoteComponent.getParameters(), mFinalComponent.getParameters());
        mRecyclerView.setAdapter(mParameterAdapter);
        return mRecyclerView;
    }

    boolean format() {
        boolean ok = true;
        List<Parameter> parameters = mFinalComponent.getParameters();
        for (Parameter parameter : parameters) {
            String parameterValue = parameter.getParameter();
            if (TextUtils.isEmpty(parameterValue)) {
                continue;
            }
            if (ReflectUtil.isPrimitiveClass(parameter.getParameterClass())) {
                parameter.setParameter(primitiveFormat(parameterValue));
            } else {
                String formatParameter = jsonPrettyFormat(parameterValue);
                if (formatParameter == null) {
                    Toast.makeText(mActivity, parameter.getParameterName() + "值不是json,或者json格式错误", Toast.LENGTH_LONG).show();
                    ok = false;
                } else {
                    parameter.setParameter(formatParameter);
                }
            }
        }
        mParameterAdapter.notifyDataSetChanged();
        return ok;
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
        List<Parameter> parameters = mFinalComponent.getParameters();
        for (Parameter parameter : parameters) {
            String parameterValue = parameter.getParameter();
            if (parameter.isParameterRequired() && TextUtils.isEmpty(parameterValue)) {
                Toast.makeText(mActivity, parameter.getParameterName() + " 是必填参数", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    Intent makeIntent() {
        List<Parameter> parameters = mFinalComponent.getParameters();
        Intent intent = new Intent();
        for (Parameter parameter : parameters) {
            if (!TextUtils.isEmpty(parameter.getParameterName())) {
                Class clazz = parameter.getParameterClass();
                if (clazz.equals(ArrayList.class)) {
                    Log.e(TAG, "not supported class type: ArrayList");
                    continue;
                }
                try {
                    makeParameter(intent, clazz, parameter);
                } catch (RuntimeException e) {
                    Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        return intent;
    }

    @SuppressWarnings("unchecked")
    private void makeParameter(@NonNull Intent intent, @NonNull Class clazz, @NonNull Parameter parameter) {
        String parameterName = parameter.getParameterName();
        String parameterValue = parameter.getParameter();
        if (TextUtils.isEmpty(parameterValue)) {
            return;
        }
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
            if (clazz.isArray()) {
                Class<?> componentType = clazz.getComponentType();
                if (Parcelable.class.isAssignableFrom(componentType)) {
                    intent.putExtra(parameterName, (Parcelable[]) JSON.parseObject(parameterValue, clazz));
                } else if (Serializable.class.isAssignableFrom(componentType)) {
                    intent.putExtra(parameterName, (Serializable[]) JSON.parseObject(parameterValue, clazz));
                }
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

    private static class ParameterViewAdapter extends BaseRecyclerAdapter<Parameter> {

        private Context mContext;
        private int mNormalColor;
        private int mRequiredColor;
        private List<Parameter> mOrigin;

        private ParameterViewAdapter(@NonNull Context context, @NonNull List<Parameter> origin, @NonNull List<Parameter> data) {
            super(data);
            mContext = context;
            Resources resources = context.getResources();
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
            Parameter parameter = getData().get(position);
            Log.w(TAG, "parameter: " + parameter);
            TextView classNameView = holder.getView(R.id.class_name);
            final Class<?> clazz = parameter.getParameterClass();
            if (clazz.isArray()) {
                classNameView.setText(ReflectUtil.getFullClassName(clazz));
            } else {
                classNameView.setText(parameter.getParameterClass().getName());
            }
            ((TextView) holder.getView(R.id.field_name)).setText(parameter.getParameterName());
            TextView fieldRequiredView = holder.getView(R.id.field_required);
            String text;
            int color;
            if (parameter.isParameterRequired()) {
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
            Log.d(TAG, "parameter: " + parameter.getParameter() + " pos: " + position);
            valueView.setText(parameter.getParameter());
            textWatcher = new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    Parameter parameter = getData().get(position);
                    parameter.setParameter(s.toString());
                }
            };
            valueView.addTextChangedListener(textWatcher);
            valueView.setTag(textWatcher);
            setNewInstanceView(holder, parameter);
            holder.getView(R.id.reset).setOnClickListener(v -> {
                Parameter originParameter = mOrigin.get(position);
                String value = originParameter.getParameter();
                valueView.setText(value);
                valueView.setSelection(value.length());
            });
        }

        private void setNewInstanceView(@NonNull final BaseViewHolder holder, @NonNull Parameter parameter) {
            final Class<?> clazz = parameter.getParameterClass();
            TextView newInstanceView = holder.getView(R.id.new_instance);
            final EditText valueView = holder.getView(R.id.value);
            if (ReflectUtil.isPrimitiveClass(clazz)) {
                newInstanceView.setVisibility(View.GONE);
                return;
            }

            View.OnClickListener onClickListener = v -> {
                Object object;
                if (clazz.isArray()) {
                    Object[] objects = ReflectUtil.newArrayInstance(clazz, DEFAULT_ARRAY_SIZE);
                    Class<?> componentType = clazz.getComponentType();
                    if (objects != null && componentType != null) {
                        for (int i = 0; i < DEFAULT_ARRAY_SIZE; i++) {
                            objects[i] = ReflectUtil.newInstance(componentType);
                            if (i == 0) {
                                AppHelper.copyToClipboard(mContext, componentType.getCanonicalName(), objectToString(objects[i]));
                                Toast.makeText(mContext, "已将单个" + componentType.getCanonicalName() + "实体复制到粘贴板中", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    object = objects;
                } else {
                    object = ReflectUtil.newInstance(clazz);
                }
                if (object != null) {
                    String value = objectToString(object);
                    valueView.setText(value);
                    valueView.setSelection(value.length());
                }
            };
            if (clazz.isArray()) {
                final Class<?> componentType = clazz.getComponentType();
                if (componentType == null || ReflectUtil.hasDefaultConstructor(componentType)) {
                    newInstanceView.setOnClickListener(onClickListener);
                    newInstanceView.setVisibility(View.VISIBLE);
                } else {
                    newInstanceView.setVisibility(View.GONE);
                }
            } else if (ReflectUtil.hasDefaultConstructor(clazz)) {
                newInstanceView.setVisibility(View.VISIBLE);
                newInstanceView.setOnClickListener(onClickListener);
            } else {
                newInstanceView.setVisibility(View.GONE);
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
    }
}
