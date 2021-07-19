package com.su.workbox.ui.system;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.su.workbox.R;
import com.su.workbox.shell.CommandResult;
import com.su.workbox.shell.ShellUtil;
import com.su.workbox.ui.base.BaseFragment;
import com.su.workbox.widget.SimpleTextWatcher;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ShellFragment extends BaseFragment implements View.OnClickListener, Toolbar.OnMenuItemClickListener {

    public static final String TAG = ShellFragment.class.getSimpleName();
    private List<String> mCmdList = new ArrayList<>();
    private EditText mEditText;
    private TextView mTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.workbox_fragment_shell, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View runView = view.findViewById(R.id.run);
        runView.setOnClickListener(this);
        view.findViewById(R.id.clear).setOnClickListener(this);
        mEditText = view.findViewById(R.id.commands_view);
        mTextView = view.findViewById(R.id.output_view);
        mEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                runView.setEnabled(s.length() > 0);
            }
        });
    }

    @Override
    public void setTitle(Toolbar toolbar) {
        toolbar.setTitle("Shell");
        toolbar.inflateMenu(R.menu.workbox_list_menu);
        toolbar.setOnMenuItemClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.run) {
            run();
        } else if (id == R.id.clear) {
            mEditText.setText("");
            mTextView.setText("");
        }
    }

    private void run() {
        String cmd = mEditText.getText().toString();
        if (!mCmdList.contains(cmd)) {
            mCmdList.add(cmd);
        }
        CommandResult commandResult = ShellUtil.shellExec(cmd);
        if (commandResult == null) {
            mTextView.setText("commandResult is null");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Exit Code: ")
                .append(commandResult.getExitCode())
                .append("\n\n")
                .append("OUTPUT: ")
                .append("\n\n")
                .append(commandResult.getLinesString());
        mTextView.setText(sb);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.list) {
            showResultDialog();
        }
        return true;
    }

    private void showResultDialog() {
        ShellAdapter adapter = new ShellAdapter(mCmdList, mEditText, mTextView);
        RecyclerView recyclerView = new RecyclerView(mActivity);
        LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        new AlertDialog.Builder(mActivity)
                .setView(recyclerView)
                .setPositiveButton(R.string.workbox_confirm, null)
                .setNeutralButton(R.string.workbox_clear, (dialog, which) -> mCmdList.clear())
                .show();
    }
}
