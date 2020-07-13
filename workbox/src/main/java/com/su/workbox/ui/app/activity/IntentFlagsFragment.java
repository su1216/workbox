package com.su.workbox.ui.app.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by su on 17-12-25.
 */

public class IntentFlagsFragment extends IntentBaseInfoFragment {

    private static final String TAG = IntentFlagsFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private FlagAdapter mFlagAdapter;
    private List<Flag> mOriginFlagList = new ArrayList<>();
    private int mFlags;

    public IntentFlagsFragment() {
        type = TYPE_FLAGS;
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
    }

    @Override
    protected void initViews() {
        List<Flag> data = getFlagList();
        initFlagStates(data, mIntentData.getFlags());
        initOriginFlags(data);
        mFlagAdapter = new FlagAdapter(this, data);
        mRecyclerView.setAdapter(mFlagAdapter);
    }

    private static List<Flag> getFlagList() {
        List<Flag> list = new ArrayList<>();
        Class<Intent> clazz = Intent.class;
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                if (!field.getName().startsWith("FLAG_ACTIVITY_")) {
                    continue;
                }
                Integer value = (Integer) field.get(null);
                if (value == null) {
                    continue;
                }
                list.add(new Flag(field.getName(), value));
            }
        } catch (IllegalAccessException e) {
            Log.w(TAG, e);
        }
        return list;
    }

    private void initFlagStates(@NonNull List<Flag> data, int flags) {
        for (Flag flag : data) {
            if ((flag.value & flags) == flag.value) {
                flag.checked = true;
            }
        }
    }

    private void initOriginFlags(@NonNull List<Flag> data) {
        mOriginFlagList.clear();
        for (Flag flag : data) {
            if (flag.checked) {
                mOriginFlagList.add(new Flag(flag));
                mFlags |= flag.value;
            }
        }
    }

    public void collectIntentData(Intent intent, IntentData intentData) {
        intent.setFlags(mFlags);
        intentData.setFlags(mFlags);
    }

    private static class FlagAdapter extends BaseRecyclerAdapter<Flag> {

        private IntentFlagsFragment mFragment;

        private FlagAdapter(@NonNull IntentFlagsFragment fragment, @NonNull List<Flag> data) {
            super(data);
            mFragment = fragment;
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_intent_activity_flag;
        }

        @Override
        protected void bindData(@NonNull final BaseViewHolder holder, final int position, int itemType) {
            final Flag flag = getData().get(position);
            TextView flagNameView = holder.getView(R.id.flag_name);
            final CheckBox checkBox = holder.getView(R.id.check_box);
            flagNameView.setText(flag.name);
            checkBox.setChecked(flag.checked);
            checkBox.setOnClickListener(v -> {
                flag.checked = checkBox.isChecked();
                if (flag.checked) {
                    mFragment.mFlags |= flag.value;
                } else {
                    mFragment.mFlags = mFragment.mFlags & ~flag.value;
                }
                Log.d(TAG, "flags: " + mFragment.mFlags);
            });

            holder.itemView.setOnClickListener(v -> checkBox.performClick());
        }
    }

    private static class Flag {
        private String name;
        private int value;
        private boolean checked;

        private Flag(@NonNull String name, int value) {
            this.name = name;
            this.value = value;
        }

        private Flag(@NonNull Flag src) {
            name = src.name;
            value = src.value;
            checked = src.checked;
        }
    }

    public static IntentFlagsFragment newInstance(IntentData intentData) {
        Bundle args = new Bundle();
        args.putParcelable("intentData", intentData);
        IntentFlagsFragment fragment = new IntentFlagsFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
