package com.su.workbox.ui.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by su on 17-12-25.
 */

public class IntentActionFragment extends IntentBaseInfoFragment {

    private static final String TAG = IntentActionFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ActionAdapter mActionAdapter;
    private String mAction;
    private List<Action> mCloneActionList = new ArrayList<>();

    public IntentActionFragment() {
        type = TYPE_ACTION;
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
        mAction = mIntentData.getAction();
        List<Action> systemActionList = getSystemActionList();
        initActionStates(systemActionList);

        mActionAdapter = new ActionAdapter(mCloneActionList);
        mRecyclerView.setAdapter(mActionAdapter);
    }

    private void initActionStates(@NonNull List<Action> systemActions) {
        mCloneActionList.clear();
        if (TextUtils.isEmpty(mAction)) {
            mCloneActionList.addAll(systemActions);
            return;
        }
        boolean inSystem = false;
        for (Action action : systemActions) {
            if (TextUtils.equals(action.value, mAction)) {
                action.checked = true;
                inSystem = true;
                mCloneActionList.add(action);
                systemActions.remove(action);
                break;
            }
        }
        if (!inSystem) {
            mCloneActionList.add(new Action(mAction, mAction));
        }
        mCloneActionList.addAll(systemActions);
    }

    private static List<Action> getSystemActionList() {
        List<Action> list = new ArrayList<>();
        Class<Intent> clazz = Intent.class;
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                if (!field.getName().startsWith("ACTION_")) {
                    continue;
                }
                String value = (String) field.get(null);
                if (value == null) {
                    continue;
                }
                list.add(new Action(field.getName(), value));
            }
        } catch (IllegalAccessException e) {
            Log.w(TAG, e);
        }
        return list;
    }

    void showAddDialog() {
        EditText inputView = new EditText(mActivity);
        inputView.setMaxLines(1);
        new AlertDialog.Builder(mActivity)
                .setTitle("Custom Action")
                .setView(inputView)
                .setPositiveButton(R.string.workbox_confirm, (dialog, which) -> {
                    String value = inputView.getText().toString().trim();
                    if (TextUtils.isEmpty(value)) {
                        new ToastBuilder("请输入action").show();
                        return;
                    }
                    Action action = new Action(value, value);
                    action.custom = true;
                    action.checked = true;
                    mCloneActionList.add(0, action);
                    mActionAdapter.notifyItemInserted(0);
                    mRecyclerView.scrollToPosition(0);
                })
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    public void collectIntentData(Intent intent, IntentData intentData) {
        List<Action> actions = mActionAdapter.getData();
        for (Action action : actions) {
            if (action.checked) {
                intent.setAction(action.value);
                intentData.setAction(intent.getAction());
                return;
            }
        }
    }

    private static class ActionAdapter extends BaseRecyclerAdapter<Action> {
        private static final int TYPE_SYSTEM = 0;
        private static final int TYPE_CUSTOM = 1;

        private ActionAdapter(@NonNull List<Action> data) {
            super(data);
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_intent_action;
        }

        @Override
        public int getItemType(int position) {
            return getData().get(position).custom ? TYPE_CUSTOM : TYPE_SYSTEM;
        }

        @Override
        protected void bindData(@NonNull final BaseViewHolder holder, final int position, int itemType) {
            final Action action = getData().get(position);
            TextView actionView = holder.getView(R.id.action);
            final RadioButton radioButton = holder.getView(R.id.radio);
            actionView.setText(action.name);
            radioButton.setVisibility(View.VISIBLE);
            radioButton.setChecked(action.checked);
            holder.itemView.setOnClickListener(v -> {
                boolean result = !radioButton.isChecked();
                if (result) {
                    List<Action> data = getData();
                    int size = data.size();
                    for (int i = 0; i < size; i++) {
                        Action a = data.get(i);
                        if (a.checked) {
                            a.checked = false;
                            notifyItemChanged(i);
                            break;
                        }
                    }
                }

                radioButton.setChecked(result);
                action.checked = result;
            });
        }
    }

    private static class Action {
        private String name;
        private String value;
        private boolean custom;
        private boolean checked;

        private Action(@NonNull String name, String value) {
            this.name = name;
            this.value = value;
        }

        @NonNull
        @Override
        public String toString() {
            return "Action{" +
                    "name='" + name + '\'' +
                    ", value='" + value + '\'' +
                    ", custom=" + custom +
                    ", checked=" + checked +
                    '}';
        }
    }

    public static IntentActionFragment newInstance(IntentData intentData) {
        Bundle args = new Bundle();
        args.putParcelable("intentData", intentData);
        IntentActionFragment fragment = new IntentActionFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
