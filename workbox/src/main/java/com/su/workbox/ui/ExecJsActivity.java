package com.su.workbox.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
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
import com.su.workbox.entity.JsFunction;
import com.su.workbox.entity.NoteJsFunction;
import com.su.workbox.entity.Parameter;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.widget.ToastBuilder;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by su on 17-10-25.
 */

public class ExecJsActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = ExecJsActivity.class.getSimpleName();
    private static final int CACHE_SIZE = 16;

    private LayoutInflater mInflater;
    private String mFilepath;
    private JsFunction mFunction;
    private NoteJsFunction mNoteJsFunction;
    private NoteJsFunction mFinalJsFunction;
    private String mScript;
    private String mResultString;

    private TabLayout mTabLayout;
    private ViewPager mPager;
    private EditText mLastEditTextView;
    private int mLastPosition;
    private BottomSheetBehavior mBehavior;
    private TextView mResultView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_activity_js_debug);
        mInflater = getLayoutInflater();
        Intent intent = getIntent();
        mFilepath = intent.getStringExtra("filepath");
        mFunction = intent.getParcelableExtra("function");

        if (URLUtil.isAssetUrl(mFilepath)) {
            String filepath = IOUtil.getAssetFilePath(mFilepath);
            mScript = IOUtil.readAssetsFile(this, filepath);
        } else if (URLUtil.isFileUrl(mFilepath)) {
            try {
                File file = new File(new URI(mFilepath));
                String filepath = file.getAbsolutePath();
                mScript = IOUtil.readFile(filepath);
            } catch (URISyntaxException e) {
                Log.w(TAG, e);
            }
        }
        if (TextUtils.isEmpty(mScript)) {
            new ToastBuilder("请检查文件: " + mFilepath).setDuration(Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String data = IOUtil.readAssetsFile(this, "generated/js.json");
        if (!TextUtils.isEmpty(data)) {
            List<NoteJsFunction> list = JSON.parseArray(data, NoteJsFunction.class);
            for (NoteJsFunction noteJsFunction : list) {
                if (isMatch(noteJsFunction)) {
                    mNoteJsFunction = noteJsFunction;
                }
            }
        }

        List<String> realParams = mFunction.getParameters();
        int paramsSize = realParams.size();
        if (mNoteJsFunction == null && !realParams.isEmpty()) {
            new ToastBuilder("assets/generated/js.json缺少此函数相关信息").setDuration(Toast.LENGTH_LONG).show();
        }
        if (mNoteJsFunction == null) {
            mNoteJsFunction = new NoteJsFunction();
        }

        TextView descView = findViewById(R.id.desc);
        String desc = mNoteJsFunction.getDescription();
        descView.setText(desc);
        descView.setVisibility(TextUtils.isEmpty(desc) ? View.GONE : View.VISIBLE);
        List<Parameter> parameters = mNoteJsFunction.getParameters();
        if (parameters.size() < paramsSize) {
            List<Parameter> newParams = new ArrayList<>(paramsSize);
            for (int i = 0; i < paramsSize - parameters.size(); i++) {
                newParams.add(new Parameter());
            }
            mNoteJsFunction.setParameters(newParams);
        }
        mFinalJsFunction = mNoteJsFunction.clone();

        initViews();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("方法" + mFunction.getName());
    }

    @Override
    public int menuRes() {
        return R.menu.workbox_file_menu;
    }

    private void initViews() {
        mTabLayout = findViewById(R.id.tab_layout);
        if (mFunction.getParameters().isEmpty()) {
            findViewById(R.id.parameters_layout).setVisibility(View.GONE);
            mTabLayout.setVisibility(View.GONE);
        } else {
            mPager = findViewById(R.id.pager);
            mPager.setOffscreenPageLimit(CACHE_SIZE);
            mPager.setAdapter(new ParametersPagerAdapter());
            mTabLayout.setVisibility(View.VISIBLE);
            mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mPager));
            List<String> parameters = mFunction.getParameters();
            for (String parameter : parameters) {
                mTabLayout.addTab(makeTab(parameter));
            }
            mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    mTabLayout.getTabAt(position).select();
                    if (mLastEditTextView != null) {
                        mFinalJsFunction.getParameters().get(mLastPosition).setParameter(mLastEditTextView.getText().toString());
                    }
                    mLastPosition = position;
                    mLastEditTextView = (EditText) mPager.getChildAt(position);
                }
            });
        }
        findViewById(R.id.call).setOnClickListener(this);
        mResultView = findViewById(R.id.result);
        mBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheet));
        findViewById(R.id.close).setOnClickListener(this);
    }

    private TabLayout.Tab makeTab(String title) {
        TabLayout.Tab tab = mTabLayout.newTab();
        tab.setText(title);
        return tab;
    }

    //文件相同, 函数名相同
    private boolean isMatch(NoteJsFunction noteJsFunction) {
        return TextUtils.equals(mFilepath, noteJsFunction.getJsFilepath().getFilepath())
                && TextUtils.equals(mFunction.getName(), noteJsFunction.getName());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.call) {
            if (!mFunction.getParameters().isEmpty()) {
                int current = mPager.getCurrentItem();
                EditText currentParameterView = getCurrentParameterView();
                mFinalJsFunction.getParameters().get(current).setParameter(currentParameterView.getText().toString());
            }
            exec();
        } else if (id == R.id.close) {
            mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    public void exec() {
        Context rhino = Context.enter();
        rhino.setOptimizationLevel(-1);
        Scriptable scope = rhino.initStandardObjects();
        try {
            rhino.evaluateString(scope, mScript, "app", 0, null);
            Function function = (Function) scope.get(mFunction.getName(), scope);
            List<Parameter> parameters = mFinalJsFunction.getParameters();
            int size = parameters.size();
            Object[] objectParameters = new Object[size];
            for (int i = 0; i < size; i++) {
                Parameter parameter = parameters.get(i);
                Class<?> clazz = parameter.getParameterClass();
                objectParameters[i] = JSON.parseObject(parameter.getParameter(), clazz);
            }
            Object result = function.call(rhino, scope, scope, objectParameters);
            String resultString = JSON.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat);
            Object resultObject = JSON.parseObject(resultString, mFinalJsFunction.getResultClass());
            mResultString = "Java Class: " + resultObject.getClass().getName() + "\n\n" + resultString;
            Log.w(TAG, "resultObject: " + resultObject);
        } catch (RhinoException e) {
            mResultString = e.getScriptStackTrace();
            Log.w(TAG, "functionName: " + mFunction.getName(), e);
        } finally {
            Context.exit();
            mResultView.setText(mResultString);
            mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            AppHelper.hideSoftInputFromWindow(getWindow());
        }
    }

    //格式化当前EditText中的参数
    public void format(@NonNull MenuItem item) {
        EditText currentParameterView = getCurrentParameterView();
        String origin = currentParameterView.getText().toString();
        String formatParameter = prettyFormat(origin);
        if (formatParameter == null) {
            new ToastBuilder("参数不是json,或者json格式错误").show();
        } else {
            currentParameterView.setText(formatParameter);
        }
    }

    //重置当前EditText中的参数
    public void reset(@NonNull MenuItem item) {
        int current = mPager.getCurrentItem();
        EditText currentParameterView = getCurrentParameterView();
        Parameter parameter = mNoteJsFunction.getParameters().get(current);
        Parameter finalParameter = mFinalJsFunction.getParameters().get(current);
        finalParameter.setParameter(parameter.getParameter());
        finalParameter.setParameterClassName(parameter.getParameterClassName());
        finalParameter.setParameterName(parameter.getParameterName());
        finalParameter.setParameterRequired(parameter.isParameterRequired());
        String formatParameter = prettyFormat(parameter.getParameter());
        if (formatParameter == null) {
            currentParameterView.setText(parameter.getParameter());
        } else {
            currentParameterView.setText(formatParameter);
        }
    }

    private String prettyFormat(@Nullable String parameter) {
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
                Log.w(TAG, "parameter: " + parameter, e);
            }
        }
        return null;
    }

    private EditText getCurrentParameterView() {
        int current = mPager.getCurrentItem();
        return (EditText) mPager.getChildAt(current);
    }

    private class ParametersPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mFunction.getParameters().size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return object == view;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFunction.getParameters().get(position);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            EditText view = (EditText) mInflater.inflate(R.layout.workbox_tab_js_debug, container, false);
            if (mLastEditTextView == null && position == 0) {
                mLastEditTextView = view;
            }
            Parameter origin = mNoteJsFunction.getParameters().get(position);
            String formatParameter = prettyFormat(origin.getParameter());
            if (formatParameter == null) {
                view.setText(origin.getParameter());
            } else {
                view.setText(formatParameter);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    @Override
    public void onBackPressed() {
        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
