package com.su.workbox.ui.system;

import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.su.workbox.R;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;

import java.util.List;

public class ShellAdapter extends BaseRecyclerAdapter<String> {

    private final EditText mCmdView;
    private final TextView mOutputView;

    ShellAdapter(List<String> data, EditText cmdView, TextView outputView) {
        super(data);
        mCmdView = cmdView;
        mOutputView = outputView;
    }

    @Override
    public int getLayoutId(int itemType) {
        return R.layout.workbox_item_cmd;
    }

    @Override
    protected void bindData(@NonNull BaseRecyclerAdapter.BaseViewHolder holder, int position, int itemType) {
        String cmd = getData().get(position);
        TextView cmdView = holder.getView(R.id.cmd);
        cmdView.setText(cmd);
        holder.itemView.setOnClickListener(v -> {
            mCmdView.setText(cmd);
            mCmdView.setSelection(mCmdView.getText().length());
            mOutputView.setText("");
        });
    }
}
