package com.su.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.su.annotations.NoteComponent;
import com.su.annotations.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.su.sample.ObjectParameterActivity.EXTRA_KEY_PARAMETER_INT;
import static com.su.sample.ObjectParameterActivity.EXTRA_KEY_PARAMETER_LONG;
import static com.su.sample.ObjectParameterActivity.EXTRA_KEY_PARAMETER_OBJECT;
import static com.su.sample.ObjectParameterActivity.EXTRA_KEY_PARAMETER_OBJECTS;

/**
 * Created by su on 2018/1/25.
 */
@NoteComponent(description = "页面传参测试",
        type = "activity",
        parameters = {@Parameter(parameterName = EXTRA_KEY_PARAMETER_OBJECT, parameterClass = ObjectParameter.class, parameterRequired = false),
                @Parameter(parameterName = EXTRA_KEY_PARAMETER_OBJECTS, parameterClass = ObjectParameter[].class, parameterRequired = false),
                @Parameter(parameterName = EXTRA_KEY_PARAMETER_INT, parameterClass = int.class),
                @Parameter(parameterName = EXTRA_KEY_PARAMETER_LONG, parameterClass = long.class, parameterRequired = false)})
public class ObjectParameterActivity extends BaseAppCompatActivity {

    public static final String EXTRA_KEY_PARAMETER_BITMAP = "bitmap";
    public static final String EXTRA_KEY_PARAMETER_OBJECT = "object";
    public static final String EXTRA_KEY_PARAMETER_OBJECTS = "objects";
    public static final String EXTRA_KEY_PARAMETER_INT = "int";
    public static final String EXTRA_KEY_PARAMETER_LONG = "long";
    public static final String EXTRA_KEY_PARAMETER_INT_LIST = "int_list";
    private List<com.su.sample.Parameter> mData = new ArrayList<>();
    private BaseAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters_test);
        ListView listView = findViewById(R.id.list_view);
        mAdapter = new ParametersAdapter(this);
        listView.setAdapter(mAdapter);
        initParameters();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("页面传参测试");
    }

    private void initParameters() {
        Intent intent = getIntent();
        try {
            if (intent.getExtras() == null || !intent.getExtras().containsKey(EXTRA_KEY_PARAMETER_INT)) {
                Toast.makeText(this, EXTRA_KEY_PARAMETER_INT + "为必传参数", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            Bundle extras = intent.getExtras();
            if (extras == null || extras.isEmpty()) {
                return;
            }
            Set<String> keySet = extras.keySet();
            for (String key : keySet) {
                mData.add(makeParameters(key, extras.get(key)));
            }
            mAdapter.notifyDataSetChanged();
        } catch (RuntimeException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private com.su.sample.Parameter makeParameters(@NonNull String parameterName, @Nullable Object object) {
        com.su.sample.Parameter parameter = new com.su.sample.Parameter();
        parameter.setParameterName(parameterName);
        if (object == null) {
            return parameter;
        }
        if (object.getClass().isArray()) {
            Object[] objects = (Object[]) object;
            parameter.setParameter(Arrays.toString(objects));
        } else {
            parameter.setParameter(object.toString());
        }
        parameter.setParameterClassName(object.getClass().getCanonicalName());
        return parameter;
    }

    private class ParametersAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        ParametersAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_activity_parameter, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            com.su.sample.Parameter item = mData.get(position);
            holder.keyView.setText(item.getParameterName());
            holder.valueView.setText(item.getParameter());
            holder.valueClassView.setText(item.getParameterClassName());
            return convertView;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }
    }

    private static class ViewHolder {
        private TextView keyView;
        private TextView valueView;
        private TextView valueClassView;

        ViewHolder(View convertView) {
            keyView = convertView.findViewById(R.id.key);
            valueView = convertView.findViewById(R.id.value);
            valueClassView = convertView.findViewById(R.id.value_class);
        }
    }
}
